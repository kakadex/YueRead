package com.yueread.app.ui.main.explore

import android.app.Application
import com.yueread.app.base.BaseViewModel
import com.yueread.app.data.appDb
import com.yueread.app.data.entities.BookSourcePart
import com.yueread.app.help.config.SourceConfig
import com.yueread.app.help.source.SourceHelp

class ExploreViewModel(application: Application) : BaseViewModel(application) {

    fun topSource(bookSource: BookSourcePart) {
        execute {
            val minXh = appDb.bookSourceDao.minOrder
            bookSource.customOrder = minXh - 1
            appDb.bookSourceDao.upOrder(bookSource)
        }
    }

    fun deleteSource(source: BookSourcePart) {
        execute {
            SourceHelp.deleteBookSource(source.bookSourceUrl)
        }
    }

}