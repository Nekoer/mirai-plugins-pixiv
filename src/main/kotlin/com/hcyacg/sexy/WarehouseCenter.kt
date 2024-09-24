package com.hcyacg.sexy

import com.hcyacg.utils.logger
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.QuoteReply

/**
 * 从指定仓库发送图片
 */
object WarehouseCenter {
    private val logger by logger()

    suspend fun init(event: GroupMessageEvent) {

        try{
            val message = QuoteReply(event.message)
            event.subject.sendMessage(message)

        }catch (e:Exception){
            e.printStackTrace()
        }
    }


}