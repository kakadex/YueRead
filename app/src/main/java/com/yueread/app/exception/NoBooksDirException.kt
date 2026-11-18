package com.yueread.app.exception

import com.yueread.app.R
import splitties.init.appCtx

class NoBooksDirException: NoStackTraceException(appCtx.getString(R.string.no_books_dir))