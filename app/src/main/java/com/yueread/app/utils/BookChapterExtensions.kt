package com.yueread.app.utils

import com.yueread.app.data.entities.BookChapter

fun BookChapter.internString() {
    title = title.intern()
    bookUrl = bookUrl.intern()
}
