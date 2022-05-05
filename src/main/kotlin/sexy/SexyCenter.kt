package com.hcyacg.sexy

import com.hcyacg.entity.Lolicon
import com.hcyacg.initial.Setting
import com.hcyacg.rank.TotalProcessing
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 19:05
 */
object SexyCenter {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val logger = MiraiLogger.Factory.create(this::class.java)
    suspend fun init(event: GroupMessageEvent) {

        if (!Setting.groups.contains(event.group.id.toString())) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }


        val keys = event.message.content.split(" ")
        if (keys.size >= 2) {
            yandeTagSearch(event, keys[1], keys.equals("r18"))
            return
        }

        val list = mutableListOf<String>()
        if (Setting.config.setuEnable.pixiv) {
            list.add("pixiv")
        }

        if (Setting.config.setuEnable.yande) {
            list.add("yande")
        }

        if (Setting.config.setuEnable.konachan) {
            list.add("konachan")
        }


        if (Setting.config.setuEnable.localImage) {
            list.add("localImage")
        }

        if (Setting.config.setuEnable.lolicon) {
            list.add("lolicon")
        }

        if (list.size <= 0) {
            event.subject.sendMessage("该群涩图来源已全部关闭")
            return
        }
        val num = (0 until list.size).random()

        when (list[num]) {
            "yande" -> {
                yande(event)
                return
            }
            "konachan" -> {
                konachan(event)
                return
            }
            "pixiv" -> {
                pixiv(event)
                return
            }
            "localImage" -> {
                localImage(event)
                return
            }
            "lolicon" -> {
                lolicon(event)
                return
            }
        }

    }

    private suspend fun yandeTagSearch(event: GroupMessageEvent, tag: String, isR18: Boolean) {
        try {
            if (!Setting.config.setuEnable.yande) {
                return
            }
            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${tag}",
                requestBody,
                headers.build()
            )

            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content

                val toExternalResource =
                    ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray().toExternalResource()
                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }

                val quoteReply: QuoteReply = QuoteReply(event.message)
                /**
                 * 判断是否配置了撤回时间
                 */

                if (Setting.config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                        .recallIn(Setting.config.recall)
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }

        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun yande(event: GroupMessageEvent) {
        if (!Setting.config.setuEnable.yande) {
            return
        }
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build()
            )
            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content


                val toExternalResource =
                    ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray().toExternalResource()
                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }

                val quoteReply: QuoteReply = QuoteReply(event.message)
                /**
                 * 判断是否配置了撤回时间
                 */

                if (Setting.config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                        .recallIn(Setting.config.recall)
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun lolicon(event: GroupMessageEvent) {
        val message = QuoteReply(event.message)
        if (!Setting.config.setuEnable.lolicon) {
            return
        }
        try {
            val data: JsonElement?
            val url = "https://api.lolicon.app/setu/v2?proxy=i.acgmx.com&size=original&r18=2"
            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                url,
                requestBody,
                headers.build()
            )

            val lolicon = data?.let { Json.decodeFromJsonElement<Lolicon>(it) }
            if (null == lolicon){
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

            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(message.plus(Image(imageId)).plus("来源:Lolicon(${lolicon.data[0].pid})")).recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(message.plus(Image(imageId)).plus("来源:Lolicon(${lolicon.data[0].pid})"))
            }

        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun konachan(event: GroupMessageEvent) {
        if (!Setting.config.setuEnable.konachan) {
            return
        }
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://konachan.com/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build()
            )
            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content
                val toExternalResource =
                    ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.KONACHAN).toByteArray().toExternalResource()
                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }
                val quoteReply: QuoteReply = QuoteReply(event.message)

                if (Setting.config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
                        .recallIn(Setting.config.recall)
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun pixiv(event: GroupMessageEvent) {
        if (!Setting.config.setuEnable.pixiv) {
            return
        }
        try {
            //获取日本排行榜时间，当前天数-2
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            val date: String = sdf.format(calendar.time)
            val obj = TotalProcessing().dealWith("illust", "daily_r18", 3, 30, date)


            val illusts = obj?.jsonObject?.get("illusts")?.jsonArray ?: return

            val randoms: Int = (0 until (illusts.size - 1)).random()

            val tempData = illusts[randoms].jsonObject
            val id = tempData["id"]?.jsonPrimitive?.content

            val image = tempData["image_urls"]?.jsonObject?.get("large")?.jsonPrimitive?.content
            val toExternalResource =
                ImageUtil.getImage(image!!.replace("i.pximg.net", "i.acgmx.com"), CacheUtil.Type.PIXIV).toByteArray()
                    .toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            val quoteReply: QuoteReply = QuoteReply(event.message)
            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
                    .recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun localImage(event: GroupMessageEvent) {
        if (!Setting.config.setuEnable.localImage) {
            return
        }
        try {
            val file = File(Setting.config.localImagePath)
            val list = getAllImage(file)

            val num = 0 until list.size

            val imageFile = File(list[num.random()])
            val input = withContext(Dispatchers.IO) {
                FileInputStream(imageFile)
            }
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var len = 0
            while (withContext(Dispatchers.IO) {
                    input.read(buffer)
                }.also { len = it } > 0) {
                out.write(buffer, 0, len)
            }

            val toExternalResource =
                out.toByteArray().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(At(event.sender).plus("\n").plus(Image(imageId)))
                    .recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(At(event.sender).plus("\n").plus(Image(imageId)))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAllImage(file: File): MutableList<String> {
        val imageList = mutableListOf<String>()
        try {
            if (file.isDirectory) {
                file.list()?.forEach {
                    val tempFile = File(file.path + File.separator + it)
                    if (tempFile.isDirectory) {
                        imageList.addAll(getAllImage(tempFile))
                    } else {
                        imageList.add(tempFile.path)
                    }
                }
            } else {
                imageList.add(file.path)
            }
            return imageList
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }

    }
}