package com.yueread.app.model

import android.content.Context
import com.yueread.app.constant.IntentAction
import com.yueread.app.service.DownloadService
import com.yueread.app.utils.startService

object Download {


    fun start(context: Context, url: String, fileName: String) {
        context.startService<DownloadService> {
            action = IntentAction.start
            putExtra("url", url)
            putExtra("fileName", fileName)
        }
    }

}