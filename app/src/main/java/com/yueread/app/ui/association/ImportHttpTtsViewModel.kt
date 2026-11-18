package com.yueread.app.ui.association

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.yueread.app.R
import com.yueread.app.base.BaseViewModel
import com.yueread.app.constant.AppConst
import com.yueread.app.constant.AppLog
import com.yueread.app.data.appDb
import com.yueread.app.data.entities.HttpTTS
import com.yueread.app.exception.NoStackTraceException
import com.yueread.app.help.http.decompressed
import com.yueread.app.help.http.newCallResponseBody
import com.yueread.app.help.http.okHttpClient
import com.yueread.app.help.http.text
import com.yueread.app.utils.isAbsUrl
import com.yueread.app.utils.isJsonArray
import com.yueread.app.utils.isJsonObject
import com.yueread.app.utils.isUri
import com.yueread.app.utils.readText
import splitties.init.appCtx

class ImportHttpTtsViewModel(app: Application) : BaseViewModel(app) {

    val errorLiveData = MutableLiveData<String>()
    val successLiveData = MutableLiveData<Int>()

    val allSources = arrayListOf<HttpTTS>()
    val checkSources = arrayListOf<HttpTTS?>()
    val selectStatus = arrayListOf<Boolean>()

    val isSelectAll: Boolean
        get() {
            selectStatus.forEach {
                if (!it) {
                    return false
                }
            }
            return true
        }

    val selectCount: Int
        get() {
            var count = 0
            selectStatus.forEach {
                if (it) {
                    count++
                }
            }
            return count
        }

    fun importSelect(finally: () -> Unit) {
        execute {
            val selectSource = arrayListOf<HttpTTS>()
            selectStatus.forEachIndexed { index, b ->
                if (b) {
                    selectSource.add(allSources[index])
                }
            }
            appDb.httpTTSDao.insert(*selectSource.toTypedArray())
        }.onFinally {
            finally.invoke()
        }
    }

    fun importSource(text: String) {
        execute {
            importSourceAwait(text.trim())
        }.onError {
            errorLiveData.postValue("ImportError:${it.localizedMessage}")
            AppLog.put("ImportError:${it.localizedMessage}", it)
        }.onSuccess {
            comparisonSource()
        }
    }

    private suspend fun importSourceAwait(text: String) {
        when {
            text.isJsonObject() -> {
                HttpTTS.fromJson(text).getOrThrow().let {
                    allSources.add(it)
                }
            }

            text.isJsonArray() -> HttpTTS.fromJsonArray(text).getOrThrow().let { items ->
                allSources.addAll(items)
            }

            text.isAbsUrl() -> {
                importSourceUrl(text)
            }

            text.isUri() -> {
                importSourceAwait(text.toUri().readText(appCtx))
            }

            else -> throw NoStackTraceException(context.getString(R.string.wrong_format))
        }
    }

    private suspend fun importSourceUrl(url: String) {
        okHttpClient.newCallResponseBody {
            if (url.endsWith("#requestWithoutUA")) {
                url(url.substringBeforeLast("#requestWithoutUA"))
                header(AppConst.UA_NAME, "null")
            } else {
                url(url)
            }
        }.decompressed().text().let {
            importSourceAwait(it)
        }
    }

    private fun comparisonSource() {
        execute {
            allSources.forEach {
                val source = appDb.httpTTSDao.get(it.id)
                checkSources.add(source)
                selectStatus.add(source == null || source.lastUpdateTime < it.lastUpdateTime)
            }
            successLiveData.postValue(allSources.size)
        }
    }

}