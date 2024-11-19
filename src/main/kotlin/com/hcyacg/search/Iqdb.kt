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
import org.jsoup.Jsoup

object Iqdb: Search {
    private val logger by logger()

    override suspend fun load(event: GroupMessageEvent) :List<Message>{
        val list = mutableListOf<Message>()
        val host = Config.proxy.host
        val port = Config.proxy.port
        try{
            /**
             * 获取图片的代码
             */
            val picUri = DataUtil.getImageLink(event.message)
            if (picUri == null) {
                event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
                return list
            }

            val lqdb = "https://www.iqdb.org"
            val message: Message = At(event.sender).plus("\n")


            val doc = try {
                if (host.isBlank() || port == -1) {
                    Jsoup.connect(lqdb).data("url",picUri).timeout(60000).post()
                } else {
                    Jsoup.connect(lqdb).data("url",picUri).proxy(host, port).timeout(60000).post()
                }
            } catch (e: Exception) {
                if (isNetworkException(e)) {
                    logger.warn { "连接至Iqdb的网络出现异常，请检查网络" }
                    list.add(PlainText("Iqdb网络异常"))
                } else {
                    logger.error{ e.message }
                }
                return list
            }

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
        } catch (e:Exception){
            if (isNetworkException(e)) {
                logger.warn { "连接至Iqdb的网络出现异常，请检查网络" }
                list.add(PlainText("Iqdb网络异常"))
            } else {
                logger.error{ e.message }
            }
            return list
        }
    }
}