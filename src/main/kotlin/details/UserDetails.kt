package com.hcyacg.details

import com.hcyacg.initial.Setting

import com.hcyacg.utils.RequestUtil
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils

object UserDetails {
    private val headers = Headers.Builder().add("token", Setting.config.token.acgmx)
    private val requestBody: RequestBody? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)
    suspend fun findUserWorksById(event: GroupMessageEvent){
        var data: JsonElement? = null
        var authorData: JsonElement? = null
        try{
            if (!event.message.contentToString().contains(Setting.command.findUserWorksById)){
                return
            }

            val authorId = event.message.contentToString().replace(Setting.command.findUserWorksById,"").replace(" ","").split("-")[0]


            val page = if(event.message.contentToString().replace(Setting.command.findUserWorksById,"").replace(" ","").split("-").size == 2){
                event.message.contentToString().replace(Setting.command.findUserWorksById,"").replace(" ","").split("-")[1].toInt()
            }else{
                1
            }
            var offset = 0
            var num = 0
            if (page % 3 != 0){
                offset = ((page - (page % 3)) / 3) * 30 + 30
                num = page % 3 * 10
            }else{
                offset = (page / 3) * 30
                num = 30
            }

            if (StringUtils.isBlank(authorId)){
                event.subject.sendMessage("请输入正确的命令 ${Setting.command.findUserWorksById}作者Id-页码")
                return
            }

            /**
             * 获取作者信息
             */
            authorData = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search/users/details?id=$authorId",
                requestBody,
                headers.build()
            )


            //作者插画作品数量
            val totalIllusts =  authorData?.jsonObject?.get("profile")?.jsonObject?.get("total_illusts")?.jsonPrimitive?.content!!.toInt()

            val author = authorData.jsonObject["user"]?.jsonObject?.get("name")?.jsonPrimitive?.content

            /**
             * 获取作者作品信息
             */
            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search/users/illusts?id=$authorId&offset=$offset",
                requestBody,
                headers.build()
            )

            val tempData = data?.jsonObject?.get("illusts")?.jsonArray
            var message : Message = At(event.sender).plus("\n").plus("======${author}作品======").plus("\n")


            if (null == tempData){
                return
            }

            if (tempData.size == 0){
                event.subject.sendMessage(message.plus("当前页为空"))
                return
            }


            for (i in (num-10) until num){
                if (tempData.size - 1 < num){
                    event.subject.sendMessage(message.plus("当前页为空"))
                    return
                }

                val id = tempData[i].jsonObject["id"]?.jsonPrimitive?.content
                val title = tempData[i].jsonObject["title"]?.jsonPrimitive?.content

                message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $id").plus("\n")
            }
//            loop@ for ((index,o) in tempData.withIndex()){
//                if(index > 9){
//                    break@loop
//                }
//                val id = JSONObject.parseObject(o.toString()).getString("id")
//                val title = JSONObject.parseObject(o.toString()).getString("title")
//                message = message.plus("${index +1 }. $title - $id").plus("\n")
//            }

            event.subject.sendMessage(message.plus("作品共 ${if(totalIllusts % 10 != 0){ (totalIllusts/10) +1 }else{totalIllusts/10}} 页,目前处于${page}页"))
        }catch (e:Exception){
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${Setting.command.findUserWorksById}作者Id-页码")
        }
    }
}