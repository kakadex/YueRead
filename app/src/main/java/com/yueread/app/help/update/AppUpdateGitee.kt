package com.yueread.app.help.update

import androidx.annotation.Keep
import com.yueread.app.constant.AppConst
import com.yueread.app.exception.NoStackTraceException
import com.yueread.app.help.config.AppConfig
import com.yueread.app.help.coroutine.Coroutine
import com.yueread.app.help.http.newCallResponse
import com.yueread.app.help.http.okHttpClient
import com.yueread.app.help.http.text
import com.yueread.app.utils.GSON
import com.yueread.app.utils.fromJsonArray
import com.yueread.app.utils.fromJsonObject
import kotlinx.coroutines.CoroutineScope

@Keep
@Suppress("unused")
object AppUpdateGitee : AppUpdate.AppUpdateInterface {

    private val checkVariant: AppVariant
        get() = when (AppConfig.updateToVariant) {
            "official_version" -> AppVariant.OFFICIAL
            "beta_release_version" -> AppVariant.BETA_RELEASE
            "beta_releaseA_version" -> AppVariant.BETA_RELEASEA
            "beta_releaseS_version" -> AppVariant.BETA_RELEASES
            else -> AppConst.appInfo.appVariant
        }

    private suspend fun getLatestRelease(): List<AppReleaseInfo> {
        val lastReleaseUrl = if (checkVariant.isBeta()) {
            "https://gitee.com/api/v5/repos/yueread/app/releases/latest"
        } else {
            "https://gitee.com/api/v5/repos/yueread/app/releases?page=1&per_page=3&direction=desc"
        }
        val res = okHttpClient.newCallResponse {
            url(lastReleaseUrl)
        }
        if (!res.isSuccessful) {
            throw NoStackTraceException("获取新版本出错(${res.code})")
        }
        val body = res.body.text()
        if (body.isBlank()) {
            throw NoStackTraceException("获取新版本出错")
        }
        if (!checkVariant.isBeta()) {
            return GSON.fromJsonArray<GiteeRelease>(body)
                .getOrElse {
                    throw NoStackTraceException("获取新版本出错 " + it.localizedMessage)
                }
                .first { !it.prerelease }
                .gitReleaseToAppReleaseInfo()
                .sortedByDescending { it.createdAt }
        }
        return GSON.fromJsonObject<GiteeRelease>(body)
            .getOrElse {
                throw NoStackTraceException("获取新版本出错 " + it.localizedMessage)
            }
            .gitReleaseToAppReleaseInfo()
            .sortedByDescending { it.createdAt }
    }

    override fun check(
        scope: CoroutineScope,
    ): Coroutine<AppUpdate.UpdateInfo> {
        return Coroutine.async(scope) {
            getLatestRelease()
                .filter {
                    if (AppConst.appInfo.appVariant == AppVariant.BETA_RELEASE) { //不切版本
                        it.appVariant == AppConst.appInfo.appVariant
                    } else {
                        it.appVariant == checkVariant
                    }
                }
                .firstOrNull { it.versionName > AppConst.appInfo.versionName }
                ?.let {
                    return@async AppUpdate.UpdateInfo(
                        it.versionName,
                        it.note,
                        it.downloadUrl,
                        it.name
                    )
                }
            throw NoStackTraceException("已是最新版本")
        }.timeout(10000)
    }
}
