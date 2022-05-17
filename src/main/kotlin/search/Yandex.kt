package com.hcyacg.search

import com.hcyacg.entity.YandexImage
import com.hcyacg.entity.YandexSearchResult
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import entity.SaucenaoItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

object Yandex {

    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()
    private val requestBody: RequestBody? = null
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun picToHtmlSearch(event: GroupMessageEvent, picUri: String): List<Message> {
        val list = mutableListOf<Message>()

        try {
            val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"

            val yandexImageUpload =
                "https://yandex.com/images-apphost/image-download?url=${url}&cbird=111&images_avatars_size=preview&images_avatars_namespace=images-cbir"
            val message: Message = At(event.sender).plus("\n")


            val data = RequestUtil.request(RequestUtil.Companion.Method.GET, yandexImageUpload, requestBody, headers.build())
            val yandexImage = data?.let { json.decodeFromJsonElement<YandexImage>(it) }

            val yandexSearch = "https://yandex.com/images/search?rpt=imageview&url=${yandexImage?.url?.replace("preview","orig")}&cbir_id=${yandexImage?.imageShard}/${yandexImage?.imageId}"


            val doc: Document = Jsoup.connect(yandexSearch).timeout(60000).get()
            val select = doc.select(".cbir-section_name_sites").select(".Root")
            select.forEach { it ->
                val yandexSearchResult = json.parseToJsonElement(it.attr("data-state")).let { json.decodeFromJsonElement<YandexSearchResult>(it) }


                var pic = yandexSearchResult.sites?.get(0)?.thumb?.url

                if (!pic!!.contains("http") && !pic.contains("http")){
                    pic = "https:${pic}"
                }

                val externalResource = ImageUtil.getImage(pic, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
                val imageId: String = externalResource.uploadAsImage(event.group).imageId
                externalResource.close()
                list.add(message.plus(Image(imageId)).plus("\n")
                    .plus("当前为Yandex").plus("\n")
                    .plus("标题：${yandexSearchResult.sites?.get(0)?.title}").plus("\n")
                    .plus("介绍：${yandexSearchResult.sites?.get(0)?.description}").plus("\n")
                    .plus("网址：${yandexSearchResult.sites?.get(0)?.url}").plus("\n")
                )
            }
            return list
        } catch (e: IOException) {
            logger.warning("连接至Yandex出现异常，请检查网络")
            list.add(PlainText("Yandex网络异常"))
            return list
        } catch (e: HttpStatusException) {
            logger.warning("连接至Yandex的网络超时，请检查网络")
            list.add(PlainText("Yandex网络异常"))
            return list
        } catch (e: SocketTimeoutException) {
            logger.warning("连接至Yandex的网络超时，请检查网络")
            list.add(PlainText("Yandex网络异常"))
            return list
        } catch (e: ConnectException) {
            logger.warning("连接至Yandex的网络出现异常，请检查网络")
            list.add(PlainText("Yandex网络异常"))
            return list
        } catch (e: SocketException) {
            logger.warning("连接至Yandex的网络出现异常，请检查网络")
            list.add(PlainText("Yandex网络异常"))
            return list
        } catch (e: Exception) {
            logger.error(e)
            return list
        }
    }
}