package com.hcyacg.sexy

import com.hcyacg.entity.Lolicon
import com.hcyacg.initial.Setting
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.jsoup.HttpStatusException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

object LoliconCenter {
    private val requestBody: RequestBody? = null
    private var isChange: Boolean = false
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()

    suspend fun load(event: GroupMessageEvent) {
        val message = QuoteReply(event.message)
        if (!Setting.groups.contains(event.group.id.toString())) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }
        if (!Setting.config.setuEnable.lolicon) {
            event.subject.sendMessage(message.plus("已关闭lolicon"))
            return
        }
        val data: JsonElement?

        val temp = event.message.contentToString().replace("${Setting.command.lolicon} ", "").split(" ")
        //https://api.lolicon.app/setu/v2?r18=2&proxy=i.acgmx.com&size=original&keyword=loli
        var r18 = 0
        val keyword: String
        var url = "https://api.lolicon.app/setu/v2?proxy=i.acgmx.com&size=original"

        if (temp.isNotEmpty()) {
            keyword = temp[0]

            if (keyword.contentEquals("r18")){
                r18 = 1
            }else{
                val key = keyword.split("[\\&\\,\\@\\%\\$\\*\\，]".toRegex())
                if (key.size <= 3) {
                    key.forEach {
                        url = url.plus("&tag=$it")
                    }
                } else {
                    event.subject.sendMessage(message.plus("关联tag最多三个"))
                    return
                }
            }
        }



        if (temp.size == 2) {
            r18 = if (temp[1].contentEquals("r18")) {
                1
            } else {
                0
            }
        }
        url = url.plus("&r18=$r18")


        try {
            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                url,
                requestBody,
                headers.build()
            )


            val lolicon = data?.let { Json.decodeFromJsonElement<Lolicon>(it) }

            if (null == lolicon) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }

            if (lolicon.data.isNullOrEmpty()) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }


            if (null == lolicon.data[0].urls) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }


            val toExternalResource =
                ImageUtil.getImage(lolicon.data[0].urls?.original!!, CacheUtil.Type.LOLICON).toByteArray()
                    .toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            if (r18 == 1 && Setting.config.recall != 0L) {
                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("图片链接：\nhttps://www.pixiv.net/artworks/${lolicon.data[0].pid}")
                ).recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("图片链接：\nhttps://www.pixiv.net/artworks/${lolicon.data[0].pid}")
                )
            }
        } catch (e: IOException) {
            logger.warning("连接至Lolicon出现异常，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SSLHandshakeException) {
            logger.warning("连接至Lolicon的网络超时，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: HttpStatusException) {
            logger.warning("连接至Lolicon的网络超时，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SocketTimeoutException) {
            logger.warning("连接至Lolicon的网络超时，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: ConnectException) {
            logger.warning("连接至Lolicon的网络出现异常，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SocketException) {
            logger.warning("连接至Lolicon的网络出现异常，请检查网络")
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: Exception) {
            e.printStackTrace()
            event.subject.sendMessage(message.plus("服务错误"))
        }
    }

}