package com.hcyacg.rank

import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*


object Rank {
    val sdf = SimpleDateFormat("yyyy-MM-dd")

    suspend fun showRank(event: GroupMessageEvent, logger: MiraiLogger){
        var data :JSONObject? = null
        val perPage = 10
        //获取日本排行榜时间，当前天数-2
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        val date: String = sdf.format(calendar.time)

        var page : Int = 1
        var mode : String? = null

        /**
         * 对接收到到的命令进行分析获取
         */
        try{
            page = event.message.content.replace(Setting.command.showRank,"").replace(" ","").split("-")[1].toInt()
        }catch (e:Exception){
            mode = event.message.content.replace(Setting.command.showRank,"").replace(" ","")
            page = 1
        }

        if (page < 1){
            page = 1
        }


        val num  = if (page % 3 != 0){
            page % 3 * 10
        }else{
            30
        }

        if (null == mode){
            try {
                mode = event.message.content.replace(Setting.command.showRank,"").replace(" ","").split("-")[0]
            } catch (e: java.lang.Exception) {
                event.subject.sendMessage("请输入正确的排行榜命令 ${Setting.command.showRank}[day|week|month|setu]-页码")
                return
            }
        }
        /**
         * 判断是否为已有参数
         */
        if(!mode.contains("day") && !mode.contains("week") && !mode.contains("month") && !mode.contains("setu")){
            event.subject.sendMessage("请输入正确的排行榜命令 ${Setting.command.showRank}[day|week|month|setu]-页码")
            return
        }

        /**
         * 进行数据分发请求
         */
        if(mode.contains("day")){
            data =  TotalProcessing().dealWith("illust", "daily",page,perPage,date,logger)
        }

        if(mode.contains("week")){
            data =  TotalProcessing().dealWith("illust", "weekly",page,perPage,date,logger)
        }

        if(mode.contains("month")){
            data =  TotalProcessing().dealWith("illust", "monthly",page,perPage,date,logger)
        }

        if(mode.contains("setu")){
            /**
             * 判断该群是否有权查看涩图
             */
            if(Setting.groups.indexOf(event.group.id.toString()) >= 0){
                data =  TotalProcessing().dealWith("illust", "daily_r18",page,perPage,date,logger)
            }else{
                event.subject.sendMessage("该群暂时无权限查看涩图排行榜")
                return
            }
        }

        /**
         * 针对数据为空进行通知
         */
        if (null == data || !StringUtils.isBlank(data.getString("errors"))){
            event.subject.sendMessage("当前排行榜暂无数据")
            return
        }


        var message : Message = At(event.sender).plus("\n").plus("======插画排行榜($mode)======").plus("\n")
        val illusts = data.getJSONArray("illusts")

        for (i in (num-10) until num){
            val id = JSONObject.parseObject(illusts[i].toString()).getString("id")
            val title = JSONObject.parseObject(illusts[i].toString()).getString("title")
            val user = JSONObject.parseObject(JSONObject.parseObject(illusts[i].toString()).getString("user")).getString("name")

            message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")
        }
        event.subject.sendMessage(message)
    }

}