package com.hcyacg.sexy

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config
import com.hcyacg.rank.TotalProcessing
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.internal.closeQuietly
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 19:05
 */
class SexyCenter {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    val sdf = SimpleDateFormat("yyyy-MM-dd")

    suspend fun init(event: GroupMessageEvent, logger: MiraiLogger){
        val randoms : Int = (0 until 2).random()
        if (!Config.groups.contains(event.group.id.toString())){
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }

        when(randoms){
            0 -> {
                yande(event,logger)
                return
            }
            1 -> {
                konachan(event,logger)
                return
            }
            2 -> {
                pixiv(event,logger)
                return
            }
        }

    }

    suspend fun yande(event: GroupMessageEvent, logger: MiraiLogger){
        val list = arrayOf("topless","nipples","no_bra")
        val randoms : Int = (0 until (list.size - 1)).random()
        val obj = RequestUtil.requestArray(
            RequestUtil.Companion.Method.GET,
            "https://yande.re/post.json?limit=500&tags=${list[randoms]}",
            requestBody,
            headers.build(),
            logger
        )
        val num : Int = (0 until (obj!!.size -1)).random()
        val id = JSONObject.parseObject(obj[num].toString()).getString("id")
        val jpegUrl = JSONObject.parseObject(obj[num].toString()).getString("jpeg_url")
        val toExternalResource = ImageUtil.getImage(jpegUrl).toByteArray().toExternalResource()
        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
        toExternalResource.closeQuietly()
        val quoteReply :QuoteReply = QuoteReply(event.message)
        /**
         * 判断是否配置了撤回时间
         */

        if (!StringUtils.isBlank(Config.recall.toString())){
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)")).recallIn(Config.recall)
        }else{
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
        }

    }

    suspend fun konachan(event: GroupMessageEvent, logger: MiraiLogger){
        val list = arrayOf("topless","nipples","no_bra")
        val randoms : Int = (0 until (list.size - 1)).random()
        val obj = RequestUtil.requestArray(
            RequestUtil.Companion.Method.GET,
            "https://konachan.com/post.json?limit=500&tags=${list[randoms]}",
            requestBody,
            headers.build(),
            logger
        )
        val num : Int = (0 until (obj!!.size -1)).random()
        val id = JSONObject.parseObject(obj[num].toString()).getString("id")
        val jpegUrl = JSONObject.parseObject(obj[num].toString()).getString("jpeg_url")
        val toExternalResource = ImageUtil.getImage(jpegUrl).toByteArray().toExternalResource()
        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
        toExternalResource.closeQuietly()
        val quoteReply :QuoteReply = QuoteReply(event.message)

        if (!StringUtils.isBlank(Config.recall.toString())){
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)")).recallIn(Config.recall)
        }else{
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
        }
    }

    suspend fun pixiv(event: GroupMessageEvent, logger: MiraiLogger){
        //获取日本排行榜时间，当前天数-2
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        val date: String = sdf.format(calendar.time)
        var obj = TotalProcessing().dealWith("illust", "daily_r18",1,10,date,logger)
        val total = JSONObject.parseObject(obj!!.getString("pagination")).getIntValue("total")
        obj = TotalProcessing().dealWith("illust", "daily_r18",1,total,date,logger)
        val randoms : Int = (0 until (total - 1)).random()
        val response = JSONObject.parseObject(JSONArray.parseArray(obj!!.getString("response"))[0].toString())
        val works = JSONArray.parseArray(response.getString("works"))
        val tempData = JSONObject.parseObject(works[randoms].toString())
        val id = JSONObject.parseObject(JSONObject.parseObject(tempData.getString("work")).getString("id"))
        val image = JSONObject.parseObject(JSONObject.parseObject(tempData.getString("work")).getString("image_urls")).getString("large")
        val toExternalResource = ImageUtil.getImage(image).toByteArray().toExternalResource()
        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
        toExternalResource.closeQuietly()
        val quoteReply :QuoteReply = QuoteReply(event.message)
        if (!StringUtils.isBlank(Config.recall.toString())){
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)")).recallIn(Config.recall)
        }else{
            event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
        }
    }

}