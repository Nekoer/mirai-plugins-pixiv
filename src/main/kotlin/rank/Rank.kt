package com.hcyacg.rank

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config
import com.hcyacg.config.Config.groups
import com.hcyacg.config.Config.showRank
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class Rank {
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
            page = event.message.content.replace(showRank!!,"").replace(" ","").split("-")[1].toInt()
        }catch (e:Exception){
            mode = event.message.content.replace(showRank!!,"").replace(" ","")
            page = 1
        }

        if (page < 1){
            page = 1
        }

        if (null == mode){
            try {
                mode = event.message.content.replace(showRank!!,"").replace(" ","").split("-")[0]
            } catch (e: java.lang.Exception) {
                event.subject.sendMessage("请输入正确的排行榜命令 ${showRank}[day|week|month|setu]-页码")
                return
            }
        }
        /**
         * 判断是否为已有参数
         */
        if(!mode.contains("day") && !mode.contains("week") && !mode.contains("month") && !mode.contains("setu")){
            event.subject.sendMessage("请输入正确的排行榜命令 ${showRank}[day|week|month|setu]-页码")
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
            if(groups.indexOf(event.group.id.toString()) >= 0){
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

        val total = JSONObject.parseObject(data.getString("pagination")).getIntValue("total")
        val pages = JSONObject.parseObject(data.getString("pagination")).getIntValue("pages")
        val response = JSONObject.parseObject(JSONArray.parseArray(data.getString("response"))[0].toString())
        val works = JSONArray.parseArray(response.getString("works"))
        var message : Message = At(event.sender).plus("\n").plus("======插画排行榜($mode)======").plus("\n")

        for ((index,o) in works.withIndex()){
            val id = JSONObject.parseObject(JSONObject.parseObject(o.toString()).getString("work")).getString("id")
            val title = JSONObject.parseObject(JSONObject.parseObject(o.toString()).getString("work")).getString("title")
            val user = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(o.toString()).getString("work")).getString("user")).getString("name")

            message = message.plus("${((perPage * page) - perPage)+(index+1)}. $title - $user - $id").plus("\n")
        }
        event.subject.sendMessage(message.plus("本排行榜共 $pages 页，当前处在 $page 页"))
    }

}