package com.hcyacg.anime

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.Pixiv
import com.hcyacg.config.Config.agefans
import com.hcyacg.config.Config.isSend
import com.hcyacg.entity.AgefansItem
import com.hcyacg.plugin.utils.DataUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.MiraiLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class Agefans {


    //将所有番剧数据缓存下来
    fun ready(){
        agefans.clear()
        isSend.clear()

        val animeInfo = getAnimeInfo()
        animeInfo.forEach {
            if (it.isnew){
                isSend.add(it)
            }
        }

//        agefans.addAll(animeInfo)
    }

    fun getAnimeInfo():MutableList<AgefansItem>{
        val agefans:MutableList<AgefansItem> = mutableListOf()

        val doc: Document = Jsoup.connect("https://www.agefans.live").timeout(60000).get()
//        val doc: Document = Jsoup.connect("https://m.acgmx.com/test.html").timeout(60000).get()
        val elementsByTag = doc.select(".blockcontent")[0].getElementsByTag("script")[0]
        val data = DataUtil.getSubString(elementsByTag.data().toString(),"var new_anime_list = ","}];").plus("}]")
        JSONArray.parseArray(data).forEach {
            val agefansItem = AgefansItem(
                id = JSONObject.parseObject(it.toString()).getString("id"),
                isnew = JSONObject.parseObject(it.toString()).getBoolean("isnew"),
                namefornew =  JSONObject.parseObject(it.toString()).getString("namefornew"),
                name = JSONObject.parseObject(it.toString()).getString("name"),
                mtime = JSONObject.parseObject(it.toString()).getString("mtime"),
                wd = JSONObject.parseObject(it.toString()).getIntValue("wd")
            )
            agefans.add(agefansItem)
        }
        return agefans
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startTask(){

        Timer().schedule(object:  TimerTask(){
             override  fun run() {
                getAnimeInfo().forEach { info ->
                    agefans.addIfAbsent(info)

                    agefans.forEach {
                        //是否是相同的番剧
                        if(info.id == it.id){
                            //是否已更新
                            if(info.isnew != it.isnew && !info.mtime.contains(it.mtime)  && info.isnew){
                                println("已更新 ${info.name}")
                                //如果更新了就删掉已经发送的鉴权
                                isSend.forEach { send ->
                                    if(send.id == info.id && send.mtime.contains(info.mtime)){
                                        isSend.remove(send)
                                    }
                                }
                                if (isSend.indexOf(info) > -1){
                                    return
                                }


                                GlobalScope.launch{
                                    Bot.instances.forEach { bot ->
                                        bot.getGroup(77708393)?.sendMessage("${info.name} ${info.namefornew} 更新了")
                                        bot.getGroup(960879198)?.sendMessage("${info.name} ${info.namefornew} 更新了")
                                    }
//                                    event.bot.getGroup(77708393)?.sendMessage("${info.name} ${info.namefornew} 更新了")
                                }
                                isSend.add(info)
                            }
                            //不管更没更新都覆盖一下本地缓存
                            agefans.remove(it)
                            agefans.add(info)
                        }
                    }
                }

            }
        }, Date(), 60000)
    }
}