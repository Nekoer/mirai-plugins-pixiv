package com.hcyacg.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting

import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import okhttp3.internal.closeQuietly
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * http请求
 */
class RequestUtil {
    companion object {
        private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
        private var response: Response? = null

        fun requestObject(
            method: Method,
            uri: String,
            body: RequestBody?,
            headers: Headers,
            logger: MiraiLogger
        ): JSONObject? {
            /**
             * 进行请求转发
             */
            when (method) {
                Method.GET -> {
                    return httpObject(Request.Builder().url(uri).headers(headers).get().build(), logger)
                }
                Method.POST -> {
                    return body?.let { Request.Builder().url(uri).headers(headers).post(it).build() }
                        ?.let { httpObject(it, logger) }
                }
                Method.PUT -> {
                    return body?.let { Request.Builder().url(uri).headers(headers).put(it).build() }
                        ?.let { httpObject(it, logger) }
                }
                Method.DEL -> {
                    return httpObject(Request.Builder().url(uri).headers(headers).delete(body).build(), logger)
                }
            }
        }

        fun requestArray(
            method: Method,
            uri: String,
            body: RequestBody?,
            headers: Headers,
            logger: MiraiLogger
        ): JSONArray? {
            /**
             * 进行请求转发
             */
            when (method) {
                Method.GET -> {
                    return httpArray(Request.Builder().url(uri).headers(headers).get().build(), logger)
                }
                Method.POST -> {
                    return body?.let { Request.Builder().url(uri).headers(headers).post(it).build() }
                        ?.let { httpArray(it, logger) }
                }
                Method.PUT -> {
                    return body?.let { Request.Builder().url(uri).headers(headers).put(it).build() }
                        ?.let { httpArray(it, logger) }
                }
                Method.DEL -> {
                    return httpArray(Request.Builder().url(uri).headers(headers).delete(body).build(), logger)
                }
            }

        }

        /**
         * 发送http请求，返回数据（其中根据proxy是否配置加入代理机制）
         */
        private fun httpObject(request: Request, logger: MiraiLogger): JSONObject? {
            val host = Setting.config.proxy.host
            val port = Setting.config.proxy.port


            response = if (host.isBlank() || port == -1) {
                client.build().newCall(request).execute()
            } else {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port!!))
                client.proxy(proxy).build().newCall(request).execute()
            }


            if (response!!.isSuccessful) {
                return JSONObject.parseObject(response!!.body?.string())
            }

            response!!.close()
            return null
        }

        private fun httpArray(request: Request, logger: MiraiLogger): JSONArray? {
            val host = Setting.config.proxy.host
            val port = Setting.config.proxy.port

            response = if (host.isBlank() || port == -1) {
                client.build().newCall(request).execute()
            } else {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
                client.proxy(proxy).build().newCall(request).execute()
            }


            if (response!!.isSuccessful) {
                return JSONArray.parseArray(response!!.body?.string())
            }
            response!!.close()
            return null
        }

        /**
         * http的请求方式
         */
        enum class Method {
            GET, POST, PUT, DEL
        }
    }
}