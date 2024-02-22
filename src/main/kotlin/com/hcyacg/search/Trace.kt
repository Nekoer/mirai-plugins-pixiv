package com.hcyacg.search

import com.hcyacg.entity.Anilist
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import com.madgag.gif.fmsware.AnimatedGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.jsoup.HttpStatusException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 以图片搜番剧
 */
object Trace {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)

    suspend fun searchInfoByPic(event: GroupMessageEvent) {
        var data: JsonElement? = null
        // https://api.trace.moe/search?url=

        try {
            /**
             * 获取图片的代码
             */
            /**
             * 获取图片的代码
             */
            val picUri = DataUtil.getImageLink(event.message) ?: return

            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://api.trace.moe/search?cutBorders&url=https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?",
                requestBody,
                headers.build()
            )

            val trace = data?.let { Json.decodeFromJsonElement<com.hcyacg.entity.Trace>(it) }
//                logger.warning(data.toString())
            val result = trace?.result




            val message: Message = At(event.sender).plus("\n")

            /**
             * 获得搜到的番剧信息
             */
            val anilist = result?.get(0)?.anilist
            val fileName = result?.get(0)?.filename
            val episode = result?.get(0)?.episode
            val from = result?.get(0)?.from
            val to = result?.get(0)?.to
            val similarity = result?.get(0)?.similarity
            val video = result?.get(0)?.video
            val image = result?.get(0)?.image
//        var externalResource = ImageUtil.getImage(image)?.toByteArray()?.toExternalResource()
//        val imageId: String = externalResource?.uploadAsImage(event.group)!!.imageId
            headers.add("Content-Type", "application/json")


            requestBody = "{\"query\": \"query{Media(id: $anilist, type: ANIME) {id title { native} coverImage {extraLarge}}}\"}"
                .toRequestBody()
            val tempData = RequestUtil.request(
                RequestUtil.Companion.Method.POST,
                "https://graphql.anilist.co",
                requestBody,
                headers.build()
            )
            val json = Json{ignoreUnknownKeys = true}

            val aniListEntity = tempData?.let { json.decodeFromJsonElement<Anilist>(it) }

//            val cn = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(tempData!!.getString("data")).getString("Media")).getString("title")).getString("chinese")
            val jp = aniListEntity?.data?.media?.title?.native
            val coverImage = aniListEntity?.data?.media?.coverImage?.extraLarge
            var externalResource = coverImage?.let { ImageUtil.getImage(it, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource() }
            val imageId: String? = externalResource?.uploadAsImage(event.group)?.imageId



            //开始时间
            val formatter = SimpleDateFormat("HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("GMT+00:00")
            val startTime = formatter.format(from.toString().split(".")[0].toLong() * 1000)
            val endTime = formatter.format(to.toString().split(".")[0].toLong() * 1000)

            event.subject.sendMessage(
                message
                    .plus(Image(imageId!!)).plus("\n")
//                    .plus("番名：${cn}").plus("\n")
                    .plus("番名：${jp}").plus("\n")
                    .plus("别名：${fileName}").plus("\n")
                    .plus("集数：${episode}").plus("\n")
                    .plus("出现在：${startTime} - $endTime").plus("\n")
                    .plus("相似度：${similarity?.let { DataUtil.getPercentFormat(it.toDouble(), 2, 2) }}")
            )
            withContext(Dispatchers.IO) {
                externalResource?.close()
            }
            /**
             * 发送视频文件
             */
            val input = ImageUtil.getVideo("$video&size=l")
            if (null != input){
                externalResource = video2Gif(input).toByteArray().toExternalResource()
                event.subject.sendMessage(Image(externalResource.uploadAsImage(event.group).imageId))
            }

        } catch (e: IOException) {
            logger.warning("连接至Trace出现异常，请检查网络")
            event.subject.sendMessage("Trace网络异常")

        } catch (e: HttpStatusException) {
            logger.warning("连接至Trace的网络超时，请检查网络")
            event.subject.sendMessage("Trace网络异常")

        } catch (e: SocketTimeoutException) {
            logger.warning("连接至Trace的网络超时，请检查网络")
            event.subject.sendMessage("Trace网络异常")

        } catch (e: ConnectException) {
            logger.warning("连接至Trace的网络出现异常，请检查网络")
            event.subject.sendMessage("Trace网络异常")

        } catch (e: SocketException) {
            logger.warning("连接至Trace的网络出现异常，请检查网络")
            event.subject.sendMessage("Trace网络异常")
        } catch (e:IllegalStateException){
            event.subject.sendMessage("该功能发现错误,错误信息【${e.message}】")
        }
    }

    @Throws(Exception::class)
    private fun video2Gif(videoPath: InputStream): ByteArrayOutputStream {
        val infoStream = ByteArrayOutputStream()

        try{
            FFmpegFrameGrabber(videoPath).use { grabber ->
                grabber.start()
                val frames: Int = grabber.lengthInFrames
                val encoder = AnimatedGifEncoder()
                encoder.setFrameRate(frames.toFloat())
                encoder.start(infoStream)
                val converter = Java2DFrameConverter()
                var i = 0
                while (i < frames) {
                    // 8帧合成1帧？（反正越大动图越小、越快）
                    encoder.setDelay(grabber.delayedTime.toInt())
                    encoder.addFrame(converter.convert(grabber.grabImage()))
                    grabber.frameNumber = i
                    i += 8
                }
                encoder.finish()
            }
            return infoStream
        }catch (e:Exception){
//            e.printStackTrace()
            return infoStream
        }catch(e:NoClassDefFoundError){
            logger.error("您未使用FFmpeg版本,将缺少视频转Gif的功能")
            return infoStream
        }
    }

}