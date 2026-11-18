package com.yueread.app.ui.book.toc


import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.yueread.app.R
import com.yueread.app.base.BaseViewModel
import com.yueread.app.constant.AppLog
import com.yueread.app.data.appDb
import com.yueread.app.data.entities.Book
import com.yueread.app.exception.NoStackTraceException
import com.yueread.app.model.ReadBook
import com.yueread.app.model.localBook.LocalBook
import com.yueread.app.utils.FileDoc
import com.yueread.app.utils.GSON
import com.yueread.app.utils.createFileIfNotExist
import com.yueread.app.utils.openOutputStream
import com.yueread.app.utils.toastOnUi
import com.yueread.app.utils.writeText

class TocViewModel(application: Application) : BaseViewModel(application) {
    var bookUrl: String = ""
    var bookData = MutableLiveData<Book>()
    var chapterListCallBack: ChapterListCallBack? = null
    var bookMarkCallBack: BookmarkCallBack? = null
    var searchKey: String? = null

    fun initBook(bookUrl: String) {
        this.bookUrl = bookUrl
        execute {
            appDb.bookDao.getBook(bookUrl)?.let {
                bookData.postValue(it)
            }
        }
    }

    fun upBookTocRule(book: Book, complete: (Throwable?) -> Unit) {
        execute {
            appDb.bookDao.update(book)
            LocalBook.getChapterList(book).let {
                appDb.bookChapterDao.delByBook(book.bookUrl)
                appDb.bookChapterDao.insert(*it.toTypedArray())
                appDb.bookDao.update(book)
                ReadBook.onChapterListUpdated(book)
                bookData.postValue(book)
            }
        }.onSuccess {
            complete.invoke(null)
        }.onError {
            complete.invoke(it)
        }
    }

    fun reverseToc(success: (book: Book) -> Unit) {
        execute {
            bookData.value?.apply {
                setReverseToc(!getReverseToc())
                val toc = appDb.bookChapterDao.getChapterList(bookUrl)
                val newToc = toc.reversed()
                newToc.forEachIndexed { index, bookChapter ->
                    bookChapter.index = index
                }
                appDb.bookChapterDao.insert(*newToc.toTypedArray())
            }
        }.onSuccess {
            it?.let(success)
        }
    }

    fun startChapterListSearch(newText: String?) {
        chapterListCallBack?.upChapterList(newText)
    }

    fun startBookmarkSearch(newText: String?) {
        bookMarkCallBack?.upBookmark(newText)
    }

    fun upChapterListAdapter() {
        chapterListCallBack?.upAdapter()
    }

    fun saveBookmark(treeUri: Uri) {
        execute {
            val book = bookData.value
                ?: throw NoStackTraceException(context.getString(R.string.no_book))
            val fileName = "bookmark-${book.name} ${book.author}.json"
            val doc = FileDoc.fromUri(treeUri, true)
            doc.createFileIfNotExist(fileName).writeText(
                GSON.toJson(
                    appDb.bookmarkDao.getByBook(book.name, book.author)
                )
            )
        }.onError {
            AppLog.put("导出失败\n${it.localizedMessage}", it, true)
        }.onSuccess {
            context.toastOnUi("导出成功")
        }
    }

    fun saveBookmarkMd(treeUri: Uri) {
        execute {
            val book = bookData.value
                ?: throw NoStackTraceException(context.getString(R.string.no_book))
            val fileName = "bookmark-${book.name} ${book.author}.md"
            val treeDoc = FileDoc.fromUri(treeUri, true)
            val fileDoc = treeDoc.createFileIfNotExist(fileName)
                .openOutputStream()
                .getOrThrow()
            fileDoc.use { outputStream ->
                outputStream.write("## ${book.name} ${book.author}\n\n".toByteArray())
                appDb.bookmarkDao.getByBook(book.name, book.author).forEach {
                    outputStream.write("#### ${it.chapterName}\n\n".toByteArray())
                    outputStream.write("###### 原文\n ${it.bookText}\n\n".toByteArray())
                    outputStream.write("###### 摘要\n ${it.content}\n\n".toByteArray())
                }
            }
        }.onError {
            AppLog.put("导出失败\n${it.localizedMessage}", it, true)
        }.onSuccess {
            context.toastOnUi("导出成功")
        }
    }

    interface ChapterListCallBack {
        fun upChapterList(searchKey: String?)

        fun clearDisplayTitle()

        fun upAdapter()
    }

    interface BookmarkCallBack {
        fun upBookmark(searchKey: String?)
    }
}