package com.yueread.app.web

import android.graphics.Bitmap
import fi.iki.elonen.NanoHTTPD
import com.yueread.app.api.ReturnData
import com.yueread.app.api.controller.BookController
import com.yueread.app.api.controller.BookSourceController
import com.yueread.app.api.controller.ReplaceRuleController
import com.yueread.app.api.controller.RssSourceController
import com.yueread.app.help.config.AppConfig
import com.yueread.app.help.coroutine.Coroutine
import com.yueread.app.service.WebService
import com.yueread.app.utils.GSON
import com.yueread.app.utils.LogUtils
import com.yueread.app.utils.stackTraceStr
import com.yueread.app.web.utils.AssetsWeb
import kotlinx.coroutines.runBlocking
import okio.Pipe
import okio.buffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HttpServer(port: Int) : NanoHTTPD(port) {
    private val assetsWeb = AssetsWeb("web")

    override fun serve(session: IHTTPSession): Response {
        WebService.serve()
        var returnData: ReturnData? = null
        val ct = ContentType(session.headers["content-type"]).tryUTF8()
        session.headers["content-type"] = ct.contentTypeHeader
        var uri = session.uri

        val startAt = System.currentTimeMillis()
        LogUtils.d(TAG) {
            "${session.method.name} - $uri - ${session.queryParameterString} - Start($startAt)"
        }

        // 安全加固：API 请求必须携带正确访问令牌，防止同网段任意设备直接读写书源与正文。
        // 静态网页资源与 CORS 预检放行；令牌经 X-YueRead-Token 请求头或 token 查询参数传入。
        if (session.method != Method.OPTIONS && !isStaticAsset(uri) && !checkAccessToken(session)) {
            LogUtils.d(TAG) { "拒绝未授权请求: ${session.method.name} - $uri" }
            return newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                "application/json",
                "{\"isSuccess\":false,\"errorMsg\":\"unauthorized\"}"
            )
        }

        try {
            when (session.method) {
                Method.OPTIONS -> {
                    val response = newFixedLengthResponse("")
                    response.addHeader("Access-Control-Allow-Methods", "POST")
                    response.addHeader("Access-Control-Allow-Headers", "content-type")
                    response.addHeader("Access-Control-Allow-Origin", session.headers["origin"])
                    //response.addHeader("Access-Control-Max-Age", "3600");
                    return response
                }

                Method.POST -> {
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val postData = files["postData"]

                    returnData = runBlocking {
                        when (uri) {
                            "/saveBookSource" -> BookSourceController.saveSource(postData)
                            "/saveBookSources" -> BookSourceController.saveSources(postData)
                            "/deleteBookSources" -> BookSourceController.deleteSources(postData)
                            "/saveBook" -> BookController.saveBook(postData)
                            "/deleteBook" -> BookController.deleteBook(postData)
                            "/saveBookProgress" -> BookController.saveBookProgress(postData)
                            "/addLocalBook" -> BookController.addLocalBook(session.parameters, files)
                            "/saveReadConfig" -> BookController.saveWebReadConfig(postData)
                            "/saveRssSource" -> RssSourceController.saveSource(postData)
                            "/saveRssSources" -> RssSourceController.saveSources(postData)
                            "/deleteRssSources" -> RssSourceController.deleteSources(postData)
                            "/saveReplaceRule" -> ReplaceRuleController.saveRule(postData)
                            "/deleteReplaceRule" -> ReplaceRuleController.delete(postData)
                            "/testReplaceRule" -> ReplaceRuleController.testRule(postData)
                            else -> null
                        }
                    }
                }

                Method.GET -> {
                    val parameters = session.parameters

                    returnData = when (uri) {
                        "/getBookSource" -> BookSourceController.getSource(parameters)
                        "/getBookSources" -> BookSourceController.sources
                        "/getBookshelf" -> BookController.bookshelf
                        "/getChapterList" -> BookController.getChapterList(parameters)
                        "/refreshToc" -> BookController.refreshToc(parameters)
                        "/getBookContent" -> BookController.getBookContent(parameters)
                        "/cover" -> BookController.getCover(parameters)
                        "/image" -> BookController.getImg(parameters)
                        "/getReadConfig" -> BookController.getWebReadConfig()
                        "/getRssSource" -> RssSourceController.getSource(parameters)
                        "/getRssSources" -> RssSourceController.sources
                        "/getReplaceRules" -> ReplaceRuleController.allRules
                        else -> null
                    }
                }

                else -> Unit
            }

            if (returnData == null) {
                if (uri.endsWith("/"))
                    uri += "index.html"
                return assetsWeb.getResponse(uri)
            }

            val response = if (returnData.data is Bitmap) {
                val outputStream = ByteArrayOutputStream()
                (returnData.data as Bitmap).compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                outputStream.close()
                val inputStream = ByteArrayInputStream(byteArray)
                newFixedLengthResponse(
                    Response.Status.OK,
                    "image/png",
                    inputStream,
                    byteArray.size.toLong()
                )
            } else {
                val data = returnData.data
                if (data is List<*> && data.size > 3000) {
                    val pipe = Pipe(16 * 1024)
                    Coroutine.async {
                        pipe.sink.buffer().outputStream().bufferedWriter(Charsets.UTF_8).use {
                            GSON.toJson(returnData, it)
                        }
                    }
                    newChunkedResponse(
                        Response.Status.OK,
                        "application/json",
                        pipe.source.buffer().inputStream()
                    )
                } else {
                    newFixedLengthResponse(GSON.toJson(returnData))
                }
            }
            response.addHeader("Access-Control-Allow-Methods", "GET, POST")
            response.addHeader("Access-Control-Allow-Origin", session.headers["origin"])
            LogUtils.d(TAG) {
                "${session.method.name} - $uri - ${session.queryParameterString} - End($startAt)"
            }
            return response
        } catch (e: Exception) {
            LogUtils.d(TAG) {
                "${session.method.name} - $uri - ${session.queryParameterString} - Error End($startAt)\n$e\n${e.stackTraceStr}"
            }
            return newFixedLengthResponse(e.message)
        }

    }

    /** 静态资源（网页本体）与封面图不走令牌校验 */
    private fun isStaticAsset(uri: String): Boolean {
        if (uri == "/" || uri.endsWith("/")) return true
        return STATIC_SUFFIXES.any { uri.endsWith(it, ignoreCase = true) }
    }

    /** 校验访问令牌：X-YueRead-Token 请求头或 token 查询参数 */
    private fun checkAccessToken(session: IHTTPSession): Boolean {
        val expected = AppConfig.webAccessToken
        if (expected.isBlank()) return false
        val fromHeader = session.headers["x-yueread-token"]
        val fromQuery = session.parameters["token"]?.firstOrNull()
        val provided = fromHeader ?: fromQuery ?: return false
        if (provided.length != expected.length) return false
        var diff = 0
        for (i in expected.indices) {
            diff = diff or (expected[i].code xor provided[i].code)
        }
        return diff == 0
    }

    companion object {
        private const val TAG = "HttpServer"

        private val STATIC_SUFFIXES = listOf(
            ".html", ".htm", ".css", ".js", ".mjs", ".map",
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".eot", ".txt"
        )
    }

}
