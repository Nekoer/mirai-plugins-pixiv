package com.hcyacg.rank

import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import com.hcyacg.utils.RequestUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 21:52
 */
object Tag {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null

    suspend fun init(event: GroupMessageEvent, logger: MiraiLogger) {

        try {
            val q = event.message.content.replace(Setting.command.tag, "").replace(" ", "").split("-")[0]
            val page = event.message.content.replace(Setting.command.tag, "").replace(" ", "").split("-")[1].toInt()
            var offset = 0
            var num = 0
            if (page % 3 != 0) {
                offset = ((page - (page % 3)) / 3) * 30 + 30
                num = page % 3 * 10
            } else {
                offset = (page / 3) * 30
                num = 30
            }


            val data = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search?q=$q&offset=$offset",
                requestBody,
                headers.build(),
                logger
            )

            /**
             * 针对数据为空进行通知
             */
            if (null == data || StringUtils.isNotBlank(data.getString("errors"))) {
                event.subject.sendMessage("当前排行榜暂无数据")
                return
            }

            var message: Message = At(event.sender).plus("\n").plus("======标签排行榜($q)======").plus("\n")
            val illusts = data.getJSONArray("illusts")


            for (i in (num - 10) until num) {
                if (illusts.size > i ) {
                    val id = JSONObject.parseObject(illusts[i].toString()).getString("id")
                    val title = JSONObject.parseObject(illusts[i].toString()).getString("title")
                    val user = JSONObject.parseObject(JSONObject.parseObject(illusts[i].toString()).getString("user"))
                        .getString("name")

                    message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")
                }
            }
            event.subject.sendMessage(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}