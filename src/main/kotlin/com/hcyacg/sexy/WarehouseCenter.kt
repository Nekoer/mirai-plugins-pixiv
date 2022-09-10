package com.hcyacg.sexy

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 从指定仓库发送图片
 */
object WarehouseCenter {
    private val logger = MiraiLogger.Factory.create(this::class.java)

    suspend fun init(event: GroupMessageEvent) {

        try{
            val quote: QuoteReply = event.message.getOrFail(QuoteReply.Key)


        }catch (e:Exception){
            e.printStackTrace()
        }
    }


}