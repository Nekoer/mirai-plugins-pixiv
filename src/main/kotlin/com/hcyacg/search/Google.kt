package com.hcyacg.search

import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.logger
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Google: Search {
    private val logger by logger()

    override suspend fun load(event: GroupMessageEvent): List<Message> {

        val list = mutableListOf<Message>()
        val ua =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        val host = Config.proxy.host
        val port = Config.proxy.port
        /**
         * 获取图片的代码
         */
        val picUri = DataUtil.getImageLink(event.message)
        if (picUri == null) {
            event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
            return list
        }
        val doc = try {
            if (host.isBlank() || port == -1) {
                Jsoup.connect(
                    "${Config.googleConfig.googleImageUrl}/searchbyimage?safe=off&sbisrc=tg&image_url=${
                        DataUtil.urlEncode(picUri)
                    }&hl=zh-CN"
                ).header("User-Agent", ua).timeout(60000).get()
            } else {
                Jsoup.connect(
                    "${Config.googleConfig.googleImageUrl}/searchbyimage?safe=off&sbisrc=tg&image_url=${
                        DataUtil.urlEncode(picUri)
                    }&hl=zh-CN"
                ).header("User-Agent", ua).proxy(host, port).timeout(60000).get()
            }
        } catch (e: Exception) {
            return if (isNetworkException(e)) {
                logger.warn { "连接至Google的网络出现异常，请检查网络" }
                list.add(PlainText("Google网络异常"))
                list
            } else {
                logger.error{ e.message }
                list
            }
        }

        // Google精准搜索
        try {
            val links = doc.select("a[href]")
            val linkHref = links.find { it.text() == "全部尺寸" }
            if (linkHref != null) {
                val docAllSize: Document = if (host.isBlank() || port == -1) {
                    Jsoup.connect("${Config.googleConfig.googleImageUrl}${linkHref.attr("href")}")
                        .header("User-Agent", ua).timeout(60000).get()
                } else {
                    Jsoup.connect("${Config.googleConfig.googleImageUrl}${linkHref.attr("href")}")
                        .header("User-Agent", ua).proxy(host, port).timeout(60000).get()
                }
                val element = docAllSize.selectFirst("div[data-ri]")
                if (element != null) {
                    val title = element.selectFirst("h3")?.text()
                    val url = element.selectFirst("a[rel=\"noopener\"]")?.attr("href")
                    val imageDataId = element.selectFirst("img")?.attr("data-iid")
                    val image = docAllSize.html().substringAfter("_setImgSrc('${imageDataId}','").substringBefore("');</script>")
                    val imageId = if (image.isNotBlank()) {
                        val toExternalResource = ImageUtil.generateImage(image)?.toExternalResource()
                        toExternalResource?.uploadAsImage(event.group)?.imageId
                    } else {
                        null
                    }

                    list.add(buildMessageChain {
                        +At(event.sender)
                        +PlainText("\n")
                        if (imageId != null) {
                            +Image(imageId)
                        }
                        +PlainText("\n当前为Google精准搜索\n")
                        +PlainText("标题：${title}\n")
                        +PlainText("网址：${url}")
                    })
                }
            }
        } catch (e: Exception) {
            if (isNetworkException(e)) {
                logger.warn {  "连接至Google的网络出现异常，请检查网络" }
                list.add(PlainText("Google精准搜索网络异常"))
            } else {
                logger.error{e.message}
            }
        }

        // Google相似搜索
        try {
            var num = 0
            doc.select("#search .ULSxyf").last()?.select(".g")?.forEach {
                if (num < Config.googleConfig.resultNum - 1) {
                    val title = it.selectFirst("h3")?.text()
                    val url = it.selectFirst("a")?.attr("href")
                    val imageDataId = it.select("img").last()?.id()
                    val image = doc.html().substringBefore("';var ii=['${imageDataId}']").substringAfterLast("(function(){var s='")
                    logger.debug { image }
                    val imageId = if (image.isNotBlank()) {
                        val toExternalResource = ImageUtil.generateImage(image)?.toExternalResource()
                        toExternalResource?.uploadAsImage(event.group)?.imageId
                    } else {
                        null
                    }

                    list.add(buildMessageChain {
                        +At(event.sender)
                        +PlainText("\n")
                        if (imageId != null) {
                            +Image(imageId)
                        }
                        +PlainText("\n当前为Google相似搜索\n")
                        +PlainText("标题：${title}\n")
                        +PlainText("网址：${url}")
                    })
                    num += 1
                }
            }
        } catch (e: Exception) {
            if (isNetworkException(e)) {
                logger.warn { "连接至Google的网络出现异常，请检查网络" }
                list.add(PlainText("Google相似搜索网络异常"))
            } else {
                logger.error{ e.message }
            }
        }

        return list
    }
}