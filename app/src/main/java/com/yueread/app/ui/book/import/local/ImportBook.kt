package com.yueread.app.ui.book.import.local

import com.yueread.app.model.localBook.LocalBook
import com.yueread.app.utils.FileDoc

data class ImportBook(
    val file: FileDoc,
    var isOnBookShelf: Boolean = !file.isDir && LocalBook.isOnBookShelf(file.name)
) {
    val name get() = file.name
    val isDir get() = file.isDir
    val size get() = file.size
    val lastModified get() = file.lastModified
}
