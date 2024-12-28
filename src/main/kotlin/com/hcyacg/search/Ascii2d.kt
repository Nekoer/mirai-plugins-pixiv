package com.hcyacg.search

import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.logger
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.TlsConfig
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.http.ssl.TLS
import org.apache.hc.core5.util.Timeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.*

object Ascii2d: Search {

    private var md5: String = ""
    private const val BASEURL: String = "https://ascii2d.net"
    private val logger by logger()

    override suspend fun load(event: GroupMessageEvent): List<Message> {
        val list = mutableListOf<Message>()
        try {
            /**
             * 获取图片的代码
             */
            val picUri = DataUtil.getImageLink(event.message)
            if (picUri == null) {
                event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
                return list
            }
            val cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultTlsConfig(TlsConfig.custom()
                    .setHandshakeTimeout(Timeout.ofSeconds(30))
                    .setSupportedProtocols(TLS.V_1_1,TLS.V_1_2,TLS.V_1_3)
                    .build())
                .build()
            val host = Config.proxy.host
            val port = Config.proxy.port

            val httpClient = if (host.isBlank() || port == -1) {
                HttpClients.custom().setConnectionManager(cm).build()
            } else{
                HttpClients.custom().setConnectionManager(cm)
                    .setProxy(HttpHost(Config.proxy.host,Config.proxy.port)).build()
            }
            val ascii2d = "https://ascii2d.net/search/url/${DataUtil.urlEncode(picUri)}"
//            val headers = mutableMapOf<String,String>()
//            headers["User-Agent"] = "PostmanRuntime/7.28.4"


            val httpGet = HttpGet(ascii2d)
            httpGet.addHeader("User-Agent", "PostmanRuntime/7.29.0")

            val result = httpClient.execute(httpGet,BasicHttpClientResponseHandler())

            val doc: Document = Jsoup.parse(result)
            val elementsByClass = doc.select(".item-box")

            elementsByClass.forEach {
                val link = it.select(".detail-box a")
                if (link.isEmpty()) {
                    md5 = it.selectFirst(".image-box img")?.attr("alt").toString().lowercase(Locale.getDefault())
                } else {
                    list.add(color(elementsByClass, event))
                    list.add(bovw(event))
                    return list
                }
            }

            list.clear()
            return list
        } catch (e: Exception) {
            if (isNetworkException(e)) {
                logger.warn { "连接至Ascii2d的网络出现异常，请检查网络" }
                list.add(PlainText("Ascii2d网络异常"))
            } else {
                logger.error{ e.message }
            }
            return list
        }
    }

    private suspend fun color(elements: Elements, event: GroupMessageEvent): Message {
        val message: Message = At(event.sender).plus("\n")
        elements.forEach {
            val link = it.select(".detail-box a")
            if (link.size > 1) {
                val title = link[0].html()

                val thumbnail = BASEURL + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
                val author = link[1].html()
                val authorUrl = link[1].attr("href")

                val externalResource =
                    ImageUtil.getImage(thumbnail, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()


                return message.plus(Image(imageId)).plus("\n")
                    .plus("当前为Ascii2D 颜色检索").plus("\n")
                    .plus("标题：${title}").plus("\n")
                    .plus("作者：${author}").plus("\n")
                    .plus("网址：${uri}").plus("\n")
                    .plus("作者网址：${authorUrl}")
            }
        }
        return message.plus("程序出现一些问题~请稍后再尝试")
    }

    private suspend fun bovw(event: GroupMessageEvent): Message {
        val bovwUri = "https://ascii2d.net/search/bovw/$md5"
        val headers = mutableMapOf<String, String>()
        headers["User-Agent"] = "PostmanRuntime/7.28.4"

        val doc: Document = Jsoup.connect(bovwUri).headers(headers).timeout(60000).get()
        val elements = doc.select(".item-box")
        val message: Message = At(event.sender).plus("\n")
        elements.forEach {
            val link = it.select(".detail-box a")
            if (link.isNotEmpty()) {
                val title = link[0].html()

                val thumbnail = BASEURL + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")


                val externalResource =
                    ImageUtil.getImage(thumbnail, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()
                return if (link.size > 1) {
                    val author = link[1].html()
                    val authorUrl = link[1].attr("href")
                    message.plus(Image(imageId)).plus("\n")
                        .plus("当前为Ascii2D 特征检索").plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("网址：${uri}").plus("\n")
                        .plus("作者：${author}").plus("\n")
                        .plus("作者网址：${authorUrl}")
                } else {
                    message.plus(Image(imageId)).plus("\n")
                        .plus("当前为Ascii2D 特征检索").plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("网址：${uri}").plus("\n")
                }
            }
        }
        return message.plus("程序出现一些问题~请稍后再尝试")
    }


}