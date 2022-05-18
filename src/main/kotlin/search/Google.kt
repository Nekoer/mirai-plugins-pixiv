package com.hcyacg.search

import com.hcyacg.initial.Setting
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Google {
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()
    private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS).followRedirects(false)


    suspend fun load(event: GroupMessageEvent, picUri: String): List<Message> {

        val list = mutableListOf<Message>()

        try{
            val host = Setting.config.proxy.host
            val port = Setting.config.proxy.port
            val uri = "https://www.google.com.hk/searchbyimage?image_url=https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0&hl=zh-CN"

            val response: Response = if (host.isBlank() || port == -1) {
                client.build().newCall(Request.Builder().url(uri).headers(headers.build()).get().build()).execute()
            } else {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
                client.proxy(proxy).build().newCall(Request.Builder().url(uri).headers(headers.build()).get().build()).execute()
            }

            val location = response.header("location")

            val tbs = getParamByUrl(location,"tbs")


            val doc: Document = if (host.isBlank() || port == -1){
                Jsoup.connect("https://www.google.com.hk/search?tbs=${tbs}&hl=zh-CN").header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36 Edg/85.0.564.44").timeout(60000).get()
            }else{
                Jsoup.connect("https://www.google.com.hk/search?tbs=${tbs}&hl=zh-CN").header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36 Edg/85.0.564.44").proxy(host,port).timeout(60000).get()
            }

            val pattern = Pattern.compile("<script.*?>.*?(data:image.*?)['|\\\"];.*?</script>")
            val matcher = pattern.matcher(doc.html())
            val map = mutableMapOf<String,String>()
            while (matcher.find()){
                val id = DataUtil.getSubString(matcher.group(),"var ii=['","'];")
                val base64 = DataUtil.getSubString(matcher.group(),"(function(){var s='","';var ii=['")
                if (id != null) {
                    if (base64 != null) {
                        if (id.isNotEmpty() && base64.isNotEmpty())
                            map[id] = base64.replace("\\x3d\\x3d","")
                    }
                }
            }


            var num = 0
            doc.select("#search").select(".g").forEach {

                if (num < 6){
                    var message: Message = At(event.sender).plus("\n")
                    val title = it.selectFirst("h3")?.html()
                    val url = it.selectFirst("a")?.attr("href")
                    val image = map[it.select("img").attr("id")]

                    if (null != image){
                        val toExternalResource = ImageUtil.generateImage(image)?.toExternalResource()
                        val imageId = toExternalResource?.uploadAsImage(event.group)?.imageId
                        if (null != imageId){
                            message = message.plus(Image(imageId)).plus("\n")
                        }
                    }

                    list.add(message
                        .plus("当前为Google").plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("网址：${url}").plus("\n"))
                    num += 1
                }
            }

//            println(doc.select("#search").select(".g"))

            return list
        }catch (e: IOException) {
            logger.warning("连接至Google出现异常，请检查网络")
            list.add(PlainText("Google网络异常"))
            return list
        } catch (e: HttpStatusException) {
            logger.warning("连接至Google的网络超时，请检查网络")
            list.add(PlainText("Google网络异常"))
            return list
        } catch (e: SocketTimeoutException) {
            logger.warning("连接至Google的网络超时，请检查网络")
            list.add(PlainText("Google网络异常"))
            return list
        } catch (e: ConnectException) {
            logger.warning("连接至Google的网络出现异常，请检查网络")
            list.add(PlainText("Google网络异常"))
            return list
        } catch (e: SocketException) {
            logger.warning("连接至Google的网络出现异常，请检查网络")
            list.add(PlainText("Google网络异常"))
            return list
        } catch (e: Exception) {
            logger.error(e)
            return list
        }
    }

    private fun getParamByUrl(url: String?, name: String): String? {
        url?.replace("https://www.google.com.hk/search?", "")?.split("&")?.forEach {
            val data = it.split("=")
            if (data[0].contentEquals(name)) {
                return data[1]
            }
        }
        return null
    }


}