package com.yueread.app.help.source

import com.yueread.app.constant.SourceType
import com.yueread.app.data.entities.BaseSource
import com.yueread.app.data.entities.BookSource
import com.yueread.app.data.entities.RssSource
import com.yueread.app.model.SharedJsScope
import org.mozilla.javascript.Scriptable
import kotlin.coroutines.CoroutineContext

fun BaseSource.getShareScope(coroutineContext: CoroutineContext? = null): Scriptable? {
    return SharedJsScope.getScope(jsLib, coroutineContext)
}

fun BaseSource.getSourceType(): Int {
    return when (this) {
        is BookSource -> SourceType.book
        is RssSource -> SourceType.rss
        else -> error("unknown source type: ${this::class.simpleName}.")
    }
}
