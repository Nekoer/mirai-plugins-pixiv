package com.hcyacg.search

import com.hcyacg.plugin.utils.DataUtil
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 搜索二次元图片转发中心
 */
object SearchPicCenter {

    suspend fun forward(event: GroupMessageEvent) {
        val nodes = mutableListOf<ForwardMessage.Node>()

        /**
         * 获取图片的代码
         */
        val picUri = DataUtil.getSubString(event.message.toString().replace(" ", ""), "[mirai:image:{", "}.")!!
            .replace("-", "")

        val picToHtmlSearch = Ascii2d.picToHtmlSearch(event, picUri)
        //Ascii2d 搜索
        picToHtmlSearch.forEach {
            nodes.add(
                ForwardMessage.Node(
                    senderId = event.bot.id,
                    senderName = event.bot.nameCardOrNick,
                    time = System.currentTimeMillis().toInt(),
                    message = it
                )
            )
        }

        //Saucenao 搜索
        val picToSearch = Saucenao.picToSearch(event, picUri)
        picToSearch.forEach {
            nodes.add(
                ForwardMessage.Node(
                    senderId = event.bot.id,
                    senderName = event.bot.nameCardOrNick,
                    time = System.currentTimeMillis().toInt(),
                    message = it
                )
            )
        }

        //iqdb搜索
        val iqdb = Iqdb.picToHtmlSearch(event, picUri)
        iqdb.forEach {
            nodes.add(
                ForwardMessage.Node(
                    senderId = event.bot.id,
                    senderName = event.bot.nameCardOrNick,
                    time = System.currentTimeMillis().toInt(),
                    message = it
                )
            )
        }

        //合并QQ消息 发送查询到的图片线索
        val forward = RawForwardMessage(nodes).render(object : ForwardMessage.DisplayStrategy {
            override fun generateTitle(forward: RawForwardMessage): String {
                return "查询到的图片线索"
            }

            override fun generateSummary(forward: RawForwardMessage): String {
                return "查看${nodes.size}条图片线索"
            }
        })

        event.subject.sendMessage(forward)
    }


}