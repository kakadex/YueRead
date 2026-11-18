@file:Suppress("unused")

package com.yueread.app.exception

/**
 * 并发限制
 */
class ConcurrentException(msg: String, val waitTime: Long) : NoStackTraceException(msg)