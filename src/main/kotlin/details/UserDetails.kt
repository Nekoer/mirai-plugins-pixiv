package com.hcyacg.details

import com.alibaba.fastjson.JSONArray
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

object UserDetails {
    private val headers = Headers.Builder().add("token", Setting.config.token.acgmx)
    private val requestBody: RequestBody? = null

    suspend fun findUserWorksById(event: GroupMessageEvent, logger: MiraiLogger){
        var data: JSONObject? = null
        var authorData: JSONObject? = null
        try{
            val authorId = event.message.content.replace(Setting.command.findUserWorksById,"").replace(" ","")

            if (StringUtils.isBlank(authorId)){
                event.subject.sendMessage("请输入正确的命令 ${Setting.command.findUserWorksById}作者Id")
                return
            }

            /**
             * 获取作者信息
             */
            authorData = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search/users/details?id=$authorId",
                requestBody,
                headers.build(),
                logger
            )

            //作者插画作品数量
            val totalIllusts = JSONObject.parseObject(authorData!!.getString("profile")).getIntValue("total_illusts")
            val author = JSONObject.parseObject(authorData!!.getString("user")).getString("name")

            /**
             * 获取作者作品信息
             */
            data = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search/users/illusts?id=$authorId&offset=30",
                requestBody,
                headers.build(),
                logger
            )

            val tempData = JSONArray.parseArray(data!!.getString("illusts"))
            var message : Message = At(event.sender).plus("\n").plus("======${author}作品======").plus("\n")
            loop@ for ((index,o) in tempData.withIndex()){
                if(index > 9){
                    break@loop
                }
                val id = JSONObject.parseObject(o.toString()).getString("id")
                val title = JSONObject.parseObject(o.toString()).getString("title")
                message = message.plus("${index +1 }. $title - $id").plus("\n")
            }

            event.subject.sendMessage(message.plus("作品共 $totalIllusts 个,目前只显示10个"))
        }catch (e:Exception){
            event.subject.sendMessage("请输入正确的命令 ${Setting.command.findUserWorksById}id")
        }
    }
}