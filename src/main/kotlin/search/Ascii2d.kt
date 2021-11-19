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

    private var md5:String = ""
    private val baseUrl:String = "https://ascii2d.net"

    suspend fun  picToHtmlSearch(event: GroupMessageEvent, logger: MiraiLogger){
        logger.warning(Config.state.toString())

        val messageChain: MessageChain = event.message
        /**
         * 获取图片的代码
         */
        val picUri = DataUtil.getSubString(messageChain.toString().replace(" ",""), "[mirai:image:{", "}.")!!
            .replace("-", "")
        val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
        val ascii2d = "https://ascii2d.net/search/url/$url"

        val doc: Document = Jsoup.connect(ascii2d).timeout(60000).get()
        val elementsByClass = doc.select(".item-box")

        elementsByClass.forEach{
            val link = it.select(".detail-box a")
            if (link.size == 0){
                md5 = it.selectFirst(".image-box img")?.attr("alt").toString().toLowerCase()
            }else if (link.size != 0){
                when(Config.state){
                    1 -> color(elementsByClass,event,logger)
                    2 -> bovw(event,logger)
                }
                return
            }
        }


    }

    private suspend fun color(elements: Elements, event: GroupMessageEvent, logger: MiraiLogger){
        elements.forEach{
            val link = it.select(".detail-box a")
            if (link.size != 0) {
                val title = link[0].html()
                val author = link[1].html()
                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
                val authorUrl = link[1].attr("href")

                var externalResource = ImageUtil.getImage(thumbnail).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()
                val message: Message = At(event.sender).plus("\n")

                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("作者：${author}").plus("\n")
                        .plus("网址：${uri}").plus("\n")
                        .plus("作者网址：${authorUrl}")
                )
                return
            }
        }
    }

    private suspend fun bovw(event: GroupMessageEvent, logger: MiraiLogger){
        val bovwUri = "https://ascii2d.net/search/bovw/$md5"
        logger.warning(md5)
        val doc: Document = Jsoup.connect(bovwUri).timeout(60000).get()
        val elements = doc.select(".item-box")
        elements.forEach{
            val link = it.select(".detail-box a")
            if (link.size != 0) {
                val title = link[0].html()
                val author = link[1].html()
                val thumbnail = baseUrl + it.select(".image-box img").attr("src")
                val uri = link[0].attr("href")
                val authorUrl = link[1].attr("href")
                println(thumbnail)
                var externalResource = ImageUtil.getImage(thumbnail).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()
                val message: Message = At(event.sender).plus("\n")

                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("\n")
                        .plus("标题：${title}").plus("\n")
                        .plus("作者：${author}").plus("\n")
                        .plus("网址：${uri}").plus("\n")
                        .plus("作者网址：${authorUrl}")
                )
                return
            }
        }
    }

    suspend fun changeState(event: GroupMessageEvent, logger: MiraiLogger){
        if (Config.state == 1){
            Config.state = 2
            event.subject.sendMessage(At(event.sender).plus("\n").plus("当前搜索已改为特徴検索"))
            return
        }

        if (Config.state == 2){
            Config.state = 1
            event.subject.sendMessage(At(event.sender).plus("\n").plus("当前搜索已改为色合検索"))
            return
        }
    }

}