package com.hcyacg.search

import com.hcyacg.utils.ImageUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Iqdb {
    private val logger = MiraiLogger.Factory.create(this::class.java)

    suspend fun picToHtmlSearch(event: GroupMessageEvent, picUri: String) :List<Message>{
        val list = mutableListOf<Message>()

        try{
            val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
            val lqdb = "https://www.iqdb.org"
            val message: Message = At(event.sender).plus("\n")


            val doc: Document = Jsoup.connect(lqdb).data("url",url).timeout(60000).post()
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

                        val externalResource = ImageUtil.getImage(pic,4).toByteArray().toExternalResource()
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
        }catch (e:Exception){
            logger.error(e)
            return list
        }
    }


}