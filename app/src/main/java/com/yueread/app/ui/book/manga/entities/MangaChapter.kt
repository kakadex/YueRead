package com.yueread.app.ui.book.manga.entities

import com.yueread.app.data.entities.BookChapter

data class MangaChapter(
    val chapter: BookChapter,
    val pages: List<BaseMangaPage>,
    val imageCount: Int
)
