package com.yueread.app.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.yueread.app.R
import com.yueread.app.constant.AppConst.appInfo
import com.yueread.app.constant.AppLog
import com.yueread.app.help.CrashHandler
import com.yueread.app.help.config.AppConfig
import com.yueread.app.help.coroutine.Coroutine
import com.yueread.app.help.update.AppUpdate
import com.yueread.app.ui.widget.dialog.TextDialog
import com.yueread.app.ui.widget.dialog.WaitDialog
import com.yueread.app.utils.FileDoc
import com.yueread.app.utils.compress.ZipUtils
import com.yueread.app.utils.createFileIfNotExist
import com.yueread.app.utils.createFolderIfNotExist
import com.yueread.app.utils.delete
import com.yueread.app.utils.externalCache
import com.yueread.app.utils.find
import com.yueread.app.utils.list
import com.yueread.app.utils.openInputStream
import com.yueread.app.utils.openOutputStream
import com.yueread.app.utils.openUrl
import com.yueread.app.utils.sendMail
import com.yueread.app.utils.sendToClip
import com.yueread.app.utils.showDialogFragment
import com.yueread.app.utils.toastOnUi
import kotlinx.coroutines.delay
import splitties.init.appCtx
import java.io.File

class AboutFragment : PreferenceFragmentCompat() {

    private val waitDialog by lazy {
        WaitDialog(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)
        findPreference<Preference>("update_log")?.summary =
            "${getString(R.string.version)} ${appInfo.versionName}"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "update_log" -> showMdFile(getString(R.string.update_log), "updateLog.md")
            "check_update" -> checkUpdate()
            "disclaimer" -> showMdFile(getString(R.string.disclaimer), "disclaimer.md")
            "privacyPolicy" -> showMdFile(getString(R.string.privacy_policy), "privacyPolicy.md")
            "crashLog" -> showDialogFragment<CrashLogsDialog>()
            "saveLog" -> saveLog()
            "createHeapDump" -> createHeapDump()
        }
        return super.onPreferenceTreeClick(preference)
    }

    @Suppress("SameParameterValue")
    private fun openUrl(@StringRes addressID: Int) {
        requireContext().openUrl(getString(addressID))
    }

    /**
     * 显示md文件
     */
    private fun showMdFile(title: String, fileName: String) {
        val mdText = String(requireContext().assets.open(fileName).readBytes())
        showDialogFragment(TextDialog(title, mdText, TextDialog.Mode.MD))
    }

    /**
     * 检测更新
     */
    private fun checkUpdate() {
        waitDialog.show()
        AppUpdate.giteeUpdate.run {
            check(lifecycleScope)
                .onSuccess {
                    showDialogFragment(
                        UpdateDialog(it)
                    )
                }.onError {
                    appCtx.toastOnUi("${getString(R.string.check_update)}\n${it.localizedMessage}")
                }.onFinally {
                    waitDialog.dismiss()
                }
        }
    }

    private fun saveLog() {
        Coroutine.async {
            val backupPath = AppConfig.backupPath ?: let {
                appCtx.toastOnUi("未设置备份目录")
                return@async
            }
            if (!AppConfig.recordLog) {
                appCtx.toastOnUi("未开启日志记录，请去其他设置里打开记录日志")
                delay(3000)
            }
            val doc = FileDoc.fromUri(Uri.parse(backupPath), true)
            copyLogs(doc)
            copyHeapDump(doc)
            appCtx.toastOnUi("已保存至备份目录")
        }.onError {
            AppLog.put("保存日志出错\n${it.localizedMessage}", it, true)
        }
    }

    private fun createHeapDump() {
        Coroutine.async {
            val backupPath = AppConfig.backupPath ?: let {
                appCtx.toastOnUi("未设置备份目录")
                return@async
            }
            if (!AppConfig.recordHeapDump) {
                appCtx.toastOnUi("未开启堆转储记录，请去其他设置里打开记录堆转储")
                delay(3000)
            }
            appCtx.toastOnUi("开始创建堆转储")
            System.gc()
            CrashHandler.doHeapDump(true)
            val doc = FileDoc.fromUri(Uri.parse(backupPath), true)
            if (!copyHeapDump(doc)) {
                appCtx.toastOnUi("未找到堆转储文件")
            } else {
                appCtx.toastOnUi("已保存至备份目录")
            }
        }.onError {
            AppLog.put("保存堆转储失败\n${it.localizedMessage}", it)
        }
    }

    private fun copyLogs(doc: FileDoc) {
        val cacheDir = appCtx.externalCache
        val logFiles = File(cacheDir, "logs")
        val crashFiles = File(cacheDir, "crash")
        val logcatFile = File(cacheDir, "logcat.txt")

        dumpLogcat(logcatFile)

        val zipFile = File(cacheDir, "logs.zip")
        ZipUtils.zipFiles(arrayListOf(logFiles, crashFiles, logcatFile), zipFile)

        doc.find("logs.zip")?.delete()

        zipFile.inputStream().use { input ->
            doc.createFileIfNotExist("logs.zip").openOutputStream().getOrNull()
                ?.use {
                    input.copyTo(it)
                }
        }
        zipFile.delete()
    }

    private fun copyHeapDump(doc: FileDoc): Boolean {
        val heapFile = FileDoc.fromFile(File(appCtx.externalCache, "heapDump")).list()
            ?.firstOrNull() ?: return false
        doc.find("heapDump")?.delete()
        val heapDumpDoc = doc.createFolderIfNotExist("heapDump")
        heapFile.openInputStream().getOrNull()?.use { input ->
            heapDumpDoc.createFileIfNotExist(heapFile.name).openOutputStream().getOrNull()
                ?.use {
                    input.copyTo(it)
                }
        }
        return true
    }

    private fun dumpLogcat(file: File) {
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            file.outputStream().use {
                process.inputStream.copyTo(it)
            }
        } catch (e: Exception) {
            AppLog.put("保存Logcat失败\n$e", e)
        }
    }

}