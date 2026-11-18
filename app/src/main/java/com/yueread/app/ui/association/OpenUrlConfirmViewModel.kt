package com.yueread.app.ui.association

import android.app.Application
import android.os.Bundle
import com.yueread.app.base.BaseViewModel
import com.yueread.app.constant.SourceType
import com.yueread.app.data.appDb
import com.yueread.app.help.source.SourceHelp

class OpenUrlConfirmViewModel(app: Application): BaseViewModel(app) {

    var uri = ""
    var mimeType: String? = null
    var sourceOrigin = ""
    var sourceName = ""
    var sourceType = SourceType.book

    fun initData(arguments: Bundle) {
        uri = arguments.getString("uri") ?: ""
        mimeType = arguments.getString("mimeType")
        sourceName = arguments.getString("sourceName") ?: ""
        sourceOrigin = arguments.getString("sourceOrigin") ?: ""
        sourceType = arguments.getInt("sourceType", SourceType.book)
    }

    fun disableSource(block: () -> Unit) {
        execute {
            SourceHelp.enableSource(sourceOrigin, sourceType, false)
        }.onSuccess {
            block.invoke()
        }
    }

    fun deleteSource(block: () -> Unit) {
        execute {
            SourceHelp.deleteSource(sourceOrigin, sourceType)
        }.onSuccess {
            block.invoke()
        }
    }

}
