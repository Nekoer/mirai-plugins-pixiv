package com.hcyacg.sexy

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.hcyacg.entity.Lolicon
import com.hcyacg.initial.Setting
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody

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
        val data: JSONObject?

        val temp = event.message.contentToString().replace("${Setting.command.lolicon} ", "").split(" ")
        //https://api.lolicon.app/setu/v2?r18=2&proxy=i.acgmx.com&size=original&keyword=loli
        var r18 = 0
        val keyword:String
        var url = "https://api.lolicon.app/setu/v2?proxy=i.acgmx.com&size=original"

        if (temp.isNotEmpty()){
            keyword = temp[0]
            val key = keyword.split("|")
            if (key.size <= 3){
                key.forEach {
                    url = url.plus("&tag=$it")
                }
            }else{
                event.subject.sendMessage(message.plus("关联tag最多三个"))
                return
            }
        }



        if (temp.size == 2){
            r18 = if(temp[1].contentEquals("r18")){1}else{0}
        }
        url = url.plus("&r18=$r18")


        try {
            data = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                url,
                requestBody,
                headers.build(),
                logger
            )

            val lolicon = JSON.parseObject(data.toString(), Lolicon::class.java)


            if (lolicon.data.isNullOrEmpty()) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }


            if (null == lolicon.data[0].urls) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }


            val toExternalResource =
                ImageUtil.getImage(lolicon.data[0].urls?.original!!).toByteArray().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            if (r18 == 1 && Setting.config.recall != 0L){
                event.subject.sendMessage(message.plus(Image(imageId))).recallIn(Setting.config.recall)
            }else{
                event.subject.sendMessage(message.plus(Image(imageId)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.subject.sendMessage(message.plus("服务错误"))
        }
    }

}