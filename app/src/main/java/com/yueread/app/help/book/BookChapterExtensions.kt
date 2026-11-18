@file:Suppress("unused")

package com.yueread.app.help.book

import com.yueread.app.data.entities.BookChapter
import com.yueread.app.help.RuleBigDataHelp.getDanmakuFile

fun BookChapter.getDanmaku(): Any? { //读取弹幕数据
    return variableMap["danmaku"] ?: getDanmakuFile(bookUrl, url)
}