package com.hcyacg.search


import com.hcyacg.config.Config
import com.hcyacg.plugin.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.*


class Ascii2d {

    private var md5: String = ""
    private val baseUrl: String = "https://ascii2d.net"

    suspend fun picToHtmlSearch(event: GroupMessageEvent, logger: MiraiLogger, picUri: String) :List<Message>{

        /**
         * 获取图片的代码
         */
//        val picUri = DataUtil.getSubString(messageChain.toString().replace(" ",""), "[mirai:image:{", "}.")!!
//            .replace("-", "")
        val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
        val ascii2d = "https://ascii2d.net/search/url/$url"
        val headers = mutableMapOf<String,String>()
        headers["User-Agent"] = "PostmanRuntime/7.28.4"

        val doc: Document = Jsoup.connect(ascii2d).timeout(60000).headers(headers).get()

        val elementsByClass = doc.select(".item-box")
        val list = mutableListOf<Message>()

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

    }

    private suspend fun color(elements: Elements, event: GroupMessageEvent, logger: MiraiLogger): Message {
        val message: Message = At(event.sender).plus("\n")
        elements.forEach {
            val link = it.select(".detail-box a")
            if (link.size > 1) {
                val title = link[0].html()
                val author = link[1].html()
                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
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
                val author = link[1].html()
                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
                val authorUrl = link[1].attr("href")

                val externalResource = ImageUtil.getImage(thumbnail).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()

                return message.plus(Image(imageId)).plus("\n")
                    .plus("当前为Ascii2D 特征检索").plus("\n")
                    .plus("标题：${title}").plus("\n")
                    .plus("作者：${author}").plus("\n")
                    .plus("网址：${uri}").plus("\n")
                    .plus("作者网址：${authorUrl}")


            }
        }
        return message.plus("程序出现一些问题~请稍后再尝试")
    }


}