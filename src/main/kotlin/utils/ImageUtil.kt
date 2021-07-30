package com.hcyacg.utils

import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayOutputStream

class ImageUtil {
    companion object {
        private val client = OkHttpClient()
        private val headers = Headers.Builder()
            .add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36 Edg/84.0.522.59")
            .add("Referer", "https://www.pixiv.net")

        /**
         * 将图片链接读取到内存转换成ByteArrayOutputStream，方便操作
         */
        fun getImageFromPixiv(imageUri: String): ByteArrayOutputStream? {

//            val request: Request = Request.Builder().url(imageUri.replace("i.pximg.net","i.pixiv.cat")).get().build()
            val request: Request = Request.Builder().url(imageUri).headers(headers.build()).get().build()

            val infoStream = ByteArrayOutputStream()
            val response: Response = client.newCall(request).execute();

            val `in` = response.body?.byteStream()
            val buffer = ByteArray(2048)
            var len = 0
            val data = ""
            if (`in` != null) {
                while (`in`.read(buffer).also { len = it } > 0) {
                    infoStream.write(buffer, 0, len)
                }
            }
            infoStream.write((Math.random() * 100).toInt() + 1)
            infoStream.close()
            return infoStream
        }
    }
}