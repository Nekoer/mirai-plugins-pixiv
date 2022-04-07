package com.hcyacg.search

import com.hcyacg.initial.Setting
import com.hcyacg.initial.entity.Config
import com.hcyacg.utils.ImageUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


object Ascii2d {

    private var md5: String = ""
    private val baseUrl: String = "https://ascii2d.net"

    suspend fun picToHtmlSearch(event: GroupMessageEvent, logger: MiraiLogger, picUri: String) :List<Message>{
        val list = mutableListOf<Message>()
        try{

            /**
             * 设置TLSv1.1
             */

            val trustManager: X509TrustManager? = object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }
            }

            val sc = SSLContext.getInstance(Setting.config.tlsVersion)
            sc.init(null, arrayOf(trustManager), null)

            val sslsf = SSLConnectionSocketFactory(sc)
            val httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()


//            val ctx:SSLContext = SSLContexts.createSystemDefault()
//            val fac = SSLConnectionSocketFactory(
//                ctx,
//                arrayOf("TLSv1.1","TLSv1.2","TLSv1.3"),
//                null,
//                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
//            )
//            val httpClientConnectionManager = PoolingHttpClientConnectionManager(RegistryBuilder.create<ConnectionSocketFactory>().register("http",PlainConnectionSocketFactory.getSocketFactory()).register("https",fac).build())
//            httpClientConnectionManager.defaultMaxPerRoute = 100
//            httpClientConnectionManager.maxTotal = 200
//            val requestConfig:RequestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000).setSocketTimeout(60000).build()
//            val httpClient:CloseableHttpClient = HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager).setDefaultRequestConfig(requestConfig).build()

            val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
            val ascii2d = "https://ascii2d.net/search/url/$url"
//            val headers = mutableMapOf<String,String>()
//            headers["User-Agent"] = "PostmanRuntime/7.28.4"


            val httpGet = HttpGet(ascii2d)
            httpGet.addHeader("User-Agent","PostmanRuntime/7.29.0")
            val httpResponse = httpClient.execute(httpGet)

            val httpEntity = httpResponse.entity
            val result = EntityUtils.toString(httpEntity,"UTF-8")

            println(httpResponse.statusLine.statusCode)
            val doc: Document = Jsoup.parse(result)
            val elementsByClass = doc.select(".item-box")

            elementsByClass.forEach {
                val link = it.select(".detail-box a")
                if (link.size == 0) {
                    md5 = it.selectFirst(".image-box img")?.attr("alt").toString().toLowerCase()
                } else {
                    list.add(color(elementsByClass,event, logger))
                    list.add(bovw(event, logger))
                    return list
                }
            }

            list.clear()
            return list
        }catch (e:Exception){
            logger.error(e)
            return list
        }

    }

    private suspend fun color(elements: Elements, event: GroupMessageEvent, logger: MiraiLogger): Message {
        val message: Message = At(event.sender).plus("\n")
        elements.forEach {
            val link = it.select(".detail-box a")
            if (link.size > 1) {
                val title = link[0].html()

                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
                val author = link[1].html()
                val authorUrl = link[1].attr("href")

                val externalResource = ImageUtil.getImage(thumbnail).toByteArray().toExternalResource()
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

    private suspend fun bovw(event: GroupMessageEvent, logger: MiraiLogger): Message {
        val bovwUri = "https://ascii2d.net/search/bovw/$md5"
        val headers = mutableMapOf<String,String>()
        headers["User-Agent"] = "PostmanRuntime/7.28.4"

        val doc: Document = Jsoup.connect(bovwUri).headers(headers).timeout(60000).get()
        val elements = doc.select(".item-box")
        val message: Message = At(event.sender).plus("\n")
        elements.forEach {
            val link = it.select(".detail-box a")
            if (link.size != 0) {
                val title = link[0].html()

                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")



                val externalResource = ImageUtil.getImage(thumbnail).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()
                return if (link.size > 1){
                    val author = link[1].html()
                    val authorUrl = link[1].attr("href")
                    message.plus(Image(imageId)).plus("\n")
                        .plus("当前为Ascii2D 特征检索").plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("网址：${uri}").plus("\n")
                        .plus("作者：${author}").plus("\n")
                        .plus("作者网址：${authorUrl}")
                }else{
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