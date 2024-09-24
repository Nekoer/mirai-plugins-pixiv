package com.hcyacg.search

import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.logger
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

object Iqdb {
    private val logger by logger()

    suspend fun picToHtmlSearch(event: GroupMessageEvent, picUri: String) :List<Message>{
        val list = mutableListOf<Message>()

        try{
            val lqdb = "https://www.iqdb.org"
            val message: Message = At(event.sender).plus("\n")


            val doc: Document = Jsoup.connect(lqdb).data("url",picUri).timeout(60000).post()
            val select = doc.select("#pages div")
            select.forEach {
                if(!it.html().contains("Your image")){
                    // || it.html().contains("Additional match")
                    if (it.html().contains("Best match")){
                        val pic = lqdb + it.select(".image img").attr("src")
                        var uri = it.select(".image a").attr("href")

                        if (!uri.contains("https") && !uri.contains("http")){
                            uri = "https:$uri"
                        }

                        val similarity = it.select("td").eq(3).text().replace(" similarity","")

                        val externalResource = ImageUtil.getImage(pic, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
                        val imageId: String = externalResource.uploadAsImage(event.group).imageId
                        externalResource.close()
                        list.add(message.plus(Image(imageId)).plus("\n")
                            .plus("当前为lqdb").plus("\n")
                            .plus("网址：${uri}").plus("\n")
                            .plus("相似度：${similarity}"))
                    }

                }
            }
            return list
        } catch (e: IOException) {
            logger.warn { "连接至Iqdb出现异常，请检查网络" }
            list.add(PlainText("Iqdb网络异常"))
            return list
        } catch (e: HttpStatusException) {
            logger.warn{ "连接至Iqdb的网络超时，请检查网络" }
            list.add(PlainText("Iqdb网络异常"))
            return list
        } catch (e: SocketTimeoutException) {
            logger.warn{ "连接至Iqdb的网络超时，请检查网络" }
            list.add(PlainText("Iqdb网络异常"))
            return list
        } catch (e: ConnectException) {
            logger.warn{ "连接至Iqdb的网络出现异常，请检查网络" }
            list.add(PlainText("Iqdb网络异常"))
            return list
        } catch (e: SocketException) {
            logger.warn{ "连接至Iqdb的网络出现异常，请检查网络" }
            list.add(PlainText("Iqdb网络异常"))
            return list
        } catch (e:Exception){
            logger.error{ e.message }
            return list
        }
    }


}