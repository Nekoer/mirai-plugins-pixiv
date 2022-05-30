package com.hcyacg.utils

import com.hcyacg.initial.Config
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit


object DownloadUtil {
    private var okHttpClient = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS)
    /**
     * @param url 下载连接
     * @param saveDir 储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    fun download(url: String, saveDir: String, listener: OnDownloadListener) {
        val host = Config.proxy.host
        val port = Config.proxy.port


        val request: Request = Request.Builder().url(url).build()
        val client = if (host.isBlank() || port == -1){
            okHttpClient.build()
        }else{
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
            okHttpClient.proxy(proxy).build()
        }
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                listener.onDownloadFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                // 储存下载文件的目录
                val savePath = isExistDir(saveDir)
                try {
                    `is` = response.body?.byteStream()
                    val total: Long = response.body?.contentLength() ?: 0L
                    val file = File(savePath, getNameFromUrl(url))
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    if (`is` != null) {
                        while (`is`.read(buf).also { len = it } != -1) {
                            fos.write(buf, 0, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / total * 100).toInt()
                            // 下载中
                            listener.onDownloading(progress)
                        }
                    }
                    fos.flush()
                    // 下载完成
                    listener.onDownloadSuccess()
                } catch (e: Exception) {
                    listener.onDownloadFailed()
                } finally {
                    try {
                        `is`?.close()
                    } catch (_: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (_: IOException) {
                    }
                }
            }
        })
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException
     * 判断下载目录是否存在
     */
    @Throws(IOException::class)
    private fun isExistDir(saveDir: String): String {
        // 下载位置
        val downloadFile = File(saveDir)
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile()
        }
        return downloadFile.absolutePath
    }

    /**
     * @param url
     * @return
     * 从下载连接中解析出文件名
     */
    private fun getNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    interface OnDownloadListener {
        /**
         * 下载成功
         */
        fun onDownloadSuccess()

        /**
         * @param progress
         * 下载进度
         */
        fun onDownloading(progress: Int)

        /**
         * 下载失败
         */
        fun onDownloadFailed()
    }
}