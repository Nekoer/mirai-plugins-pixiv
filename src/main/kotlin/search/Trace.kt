package com.hcyacg.search

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.plugin.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import com.madgag.gif.fmsware.AnimatedGifEncoder
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
import okhttp3.internal.closeQuietly
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 以图片搜番剧
 */
object Trace {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null


    suspend fun searchInfoByPic(event: GroupMessageEvent, logger: MiraiLogger) {
        var data: JSONObject? = null
        // https://api.trace.moe/search?url=

        try {
            /**
             * 获取图片的代码
             */
            val picUri = DataUtil.getSubString(event.message.toString().replace(" ", ""), "[mirai:image:{", "}.jpg]")!!
                .replace("}.png]", "")
                .replace("}.mirai]", "").replace("}.gif]", "")

            data = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://api.trace.moe/search?cutBorders&url=https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri.replace("-", "")}/0?",
                requestBody,
                headers.build(),
                logger
            )
//                logger.warning(data.toString())
            val result = JSONArray.parseArray(data!!.getString("result"))

            val message: Message = At(event.sender).plus("\n")

            /**
             * 获得搜到的番剧信息
             */
            val anilist = JSONObject.parseObject(result[0].toString()).getString("anilist")
            val fileName = JSONObject.parseObject(result[0].toString()).getString("filename")
            val episode = JSONObject.parseObject(result[0].toString()).getString("episode")
            val from = JSONObject.parseObject(result[0].toString()).getString("from")
            val to = JSONObject.parseObject(result[0].toString()).getString("to")
            val similarity = JSONObject.parseObject(result[0].toString()).getString("similarity")
            val video = JSONObject.parseObject(result[0].toString()).getString("video")
            val image = JSONObject.parseObject(result[0].toString()).getString("image")
//        var externalResource = ImageUtil.getImage(image)?.toByteArray()?.toExternalResource()
//        val imageId: String = externalResource?.uploadAsImage(event.group)!!.imageId
            headers.add("Content-Type", "application/json")

            requestBody = JSONObject.toJSON("{\"query\": \"query{Media(id: $anilist, type: ANIME) {id title { native} coverImage {extraLarge}}}\"}")
                .toString().toRequestBody()
            val tempData = RequestUtil.requestObject(
                RequestUtil.Companion.Method.POST,
                "https://graphql.anilist.co",
                requestBody,
                headers.build(),
                logger
            )



//            val cn = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(tempData!!.getString("data")).getString("Media")).getString("title")).getString("chinese")
            val jp = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(tempData!!.getString("data")).getString("Media")).getString("title")).getString("native")
            val coverImage = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(tempData.getString("data")).getString("Media")).getString("coverImage")).getString("extraLarge")
            var externalResource = ImageUtil.getImage(coverImage)!!.toByteArray().toExternalResource()
            val imageId: String = externalResource.uploadAsImage(event.group).imageId



            //开始时间
            val formatter = SimpleDateFormat("HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("GMT+00:00")
            val startTime = formatter.format(from.split(".")[0].toLong() * 1000)
            val endTime = formatter.format(to.split(".")[0].toLong() * 1000)

            event.subject.sendMessage(
                message
                    .plus(Image(imageId)).plus("\n")
//                    .plus("番名：${cn}").plus("\n")
                    .plus("番名：${jp}").plus("\n")
                    .plus("别名：${fileName}").plus("\n")
                    .plus("集数：${episode}").plus("\n")
                    .plus("出现在：${startTime} - $endTime").plus("\n")
                    .plus("相似度：${DataUtil.getPercentFormat(similarity.toDouble(), 2, 2)}")
            )
            externalResource.closeQuietly()
            /**
             * 发送视频文件
             */
            externalResource = video2Gif(ImageUtil.getVideo("$video&size=l")!!,logger).toByteArray().toExternalResource()
            event.subject.sendMessage(Image(externalResource.uploadAsImage(event.group).imageId))
        }catch (e:IllegalStateException){
//            e.printStackTrace()
//            event.subject.sendMessage("该功能发现错误,错误信息【${e.message}】")
        }catch (e:IllegalStateException){
            e.printStackTrace()
            event.subject.sendMessage("该功能发现错误,错误信息【${e.message}】")
        }
    }

    @Throws(Exception::class)
    private fun video2Gif(videoPath: InputStream,logger:MiraiLogger): ByteArrayOutputStream {
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