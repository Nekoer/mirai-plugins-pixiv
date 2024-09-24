package com.hcyacg.sexy

import com.hcyacg.entity.Lolicon
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import com.hcyacg.lowpoly.LowPoly
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import com.hcyacg.utils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Headers
import okhttp3.RequestBody
import org.jsoup.HttpStatusException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

object LoliconCenter {
    private val requestBody: RequestBody? = null
    private var isChange: Boolean = false
    private val logger by logger()
    private val headers = Headers.Builder()

    suspend fun load(event: GroupMessageEvent) {
        val message = QuoteReply(event.message)
        if (!Setting.groups.contains(event.group.id.toString())) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }

        if (!Config.enable.sexy.lolicon) {
            event.subject.sendMessage(message.plus("已关闭lolicon"))
            return
        }

        event.subject.sendMessage(At(event.sender).plus("正在获取中,请稍后"))
        val data: JsonElement?

        val temp = event.message.contentToString().replace("${Command.lolicon} ", "").split(" ")
        //https://api.lolicon.app/setu/v2?r18=2&proxy=i.acgmx.com&size=original&keyword=loli
        var r18 = 0
        val keyword: String
        var url = "https://api.lolicon.app/setu/v2?proxy=i.acgmx.com&size=${Config.loliconSize}"

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
            val toExternalResource: ExternalResource
            if (Config.lowPoly){
                val byte = ImageUtil.getImage(lolicon.data[0].urls?.original!!, CacheUtil.Type.LOLICON).toByteArray()

                /**
                 * 生成low poly风格的图片
                 * @param inputStream  源图片
                 * @param accuracy     精度值，越小精度越高
                 * @param scale        缩放，源图片和目标图片的尺寸比例
                 * @param fill         是否填充颜色，为false时只绘制线条
                 * @param format       输出图片格式
                 * @param antiAliasing 是否抗锯齿
                 * @param pointCount   随机点的数量
                 */
                toExternalResource = LowPoly.generate(
                    ByteArrayInputStream(byte),
                    200,
                    1F,
                    true,
                    "png",
                    false,
                    200
                ).toByteArray().toExternalResource()
            }else{
                toExternalResource = ImageUtil.getImage(lolicon.data[0].urls?.original!!, CacheUtil.Type.LOLICON).toByteArray()
                    .toExternalResource()
            }




            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            if (r18 == 1 && Config.recall != 0L) {
                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("图片链接：\nhttps://www.pixiv.net/artworks/${lolicon.data[0].pid}")
                ).recallIn(Config.recall)
            } else {
                event.subject.sendMessage(
                    message.plus(Image(imageId)).plus("图片链接：\nhttps://www.pixiv.net/artworks/${lolicon.data[0].pid}")
                )
            }
        } catch (e: IOException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至Lolicon的网络超时，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: HttpStatusException) {
            logger.warn { "连接至Lolicon的网络超时，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至Lolicon的网络超时，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: ConnectException) {
            logger.warn { "连接至Lolicon的网络超时，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: SocketException) {
            logger.warn { "连接至Lolicon的网络超时，请检查网络" }
            event.subject.sendMessage(message.plus("网络异常"))
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            event.subject.sendMessage(message.plus("服务错误"))
        }
    }

}