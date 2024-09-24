package com.hcyacg.search

import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage

/**
 * 搜索二次元图片转发中心
 */
object SearchPicCenter {
    private val logger by logger()
    suspend fun forward(event: GroupMessageEvent) {
        val nodes = mutableListOf<ForwardMessage.Node>()
        event.subject.sendMessage(At(event.sender).plus("正在获取中,请稍后"))
        /**
         * 获取图片的代码
         */
        val picUri = DataUtil.getImageLink(event.message)
        if (picUri == null) {
            event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
            return
        }

        val scope = CoroutineScope(Dispatchers.Default)

        val srSauceNao = scope.launch {
            if (Config.enable.search.saucenao) {
                //Saucenao 搜索
                val picToSearch = Saucenao.picToSearch(event, picUri, Config.saucenaoEco)
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
            }
        }

        val srAscii2d = scope.launch {
            if (Config.enable.search.ascii2d) {
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
            }
        }

        val srIqdb = scope.launch {
            if (Config.enable.search.iqdb) {
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
            }
        }

        val srYandex = scope.launch {
            if (Config.enable.search.yandex) {
                val yandex = Yandex.picToHtmlSearch(event, picUri)
                yandex.forEach {
                    nodes.add(
                        ForwardMessage.Node(
                            senderId = event.bot.id,
                            senderName = event.bot.nameCardOrNick,
                            time = System.currentTimeMillis().toInt(),
                            message = it
                        )
                    )
                }
            }
        }

        val srGoogle = scope.launch {
            if (Config.enable.search.google) {
                //谷歌搜图
                val google = Google.load(event, picUri)
                google.forEach {
                    nodes.add(
                        ForwardMessage.Node(
                            senderId = event.bot.id,
                            senderName = event.bot.nameCardOrNick,
                            time = System.currentTimeMillis().toInt(),
                            message = it
                        )
                    )
                }
            }
        }

        // 等待并发完成
        runBlocking {
            srSauceNao.join()
            srAscii2d.join()
            srIqdb.join()
            srYandex.join()
            srGoogle.join()
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