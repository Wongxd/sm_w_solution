package io.wongxd.solution.logger

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

object WLogUtil {

    private var allowed = true
    private var isInitialization = false

    /**
     * 初始化
     * @param tag       标签
     * @param allowed   是否允许打印日志
     * @param modify    是否强制修改之前的初始化配置
     */
    fun init(tag: String, allowed: Boolean, modify: Boolean = false) {
        if (isInitialization && !modify) return
        isInitialization = true
        WLogUtil.allowed = allowed
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .tag(tag)
            .methodCount(2)
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return allowed
            }
        })
    }

    fun init(formatStrategy: PrettyFormatStrategy, allowed: Boolean) {
        WLogUtil.allowed = allowed
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return allowed
            }
        })
    }

    fun d(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.d(message, *args)
    }

    fun d(`object`: Any) {
        if (!allowed) return
        Logger.d(`object`)
    }

    fun e(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.e(null, message, *args)
    }

    fun e(throwable: Throwable, message: String, vararg args: Any) {
        if (!allowed) return
        Logger.e(throwable, message, *args)
    }

    fun i(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.i(message, *args)
    }

    fun v(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.v(message, *args)
    }

    fun w(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.w(message, *args)
    }

    fun wtf(message: String, vararg args: Any) {
        if (!allowed) return
        Logger.wtf(message, *args)
    }

    fun json(json: String) {
        if (!allowed) return
        Logger.json(json)
    }

    fun xml(xml: String) {
        if (!allowed) return
        Logger.xml(xml)
    }

    fun warnTrace(throwable: Throwable, message: String = "") {
        if (!allowed) return
        if (message.isEmpty()) {
            Logger.w(getStackTraceString(throwable))
        } else {
            Logger.w(message, getStackTraceString(throwable))
        }
    }

    fun errorTrace(throwable: Throwable, message: String = "") {
        if (!allowed) return
        if (message.isEmpty()) {
            Logger.e(getStackTraceString(throwable))
        } else {
            Logger.e(message, getStackTraceString(throwable))
        }
    }

    fun getStackTraceString(throwable: Throwable?): String {
        if (throwable == null) {
            return ""
        }
        var t = throwable
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

}