package com.yueread.app.ui.book.import.remote

import android.app.Application
import com.yueread.app.base.BaseViewModel
import com.yueread.app.data.appDb
import com.yueread.app.data.entities.Server

class ServersViewModel(application: Application): BaseViewModel(application) {


    fun delete(server: Server) {
        execute {
            appDb.serverDao.delete(server)
        }
    }

}