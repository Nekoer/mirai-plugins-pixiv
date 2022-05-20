package com.hcyacg.rank

import com.hcyacg.details.PicDetails
import com.hcyacg.initial.Setting
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import java.text.SimpleDateFormat
import java.util.*


object Rank {
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val logger = MiraiLogger.Factory.create(this::class.java)
    suspend fun showRank(event: GroupMessageEvent) {
        var data: JsonElement? = null
        val perPage = 10
        //获取日本排行榜时间，当前天数-2
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        val date: String = sdf.format(calendar.time)

        var page = 1
        var mode: String? = null
        var enable = false

        try {
            if (Setting.groups.contains(event.group.id.toString())) {
                enable = true
            }
            /**
             * 对接收到到的命令进行分析获取
             */
            try {
                page =
                    event.message.content.replace(Setting.command.showRank, "").replace(" ", "").split("-")[1].toInt()
            } catch (e: Exception) {
                mode = event.message.content.replace(Setting.command.showRank, "").replace(" ", "")
                page = 1
            }

            if (page < 1) {
                page = 1
            }


            val num = if (page % 3 != 0) {
                page % 3 * 10
            } else {
                30
            }

            if (null == mode) {
                try {
                    mode = event.message.content.replace(Setting.command.showRank, "").replace(" ", "").split("-")[0]
                } catch (e: java.lang.Exception) {
                    event.subject.sendMessage("请输入正确的排行榜命令 ${Setting.command.showRank}[day|week|month|setu]-页码")
                    return
                }
            }
            /**
             * 判断是否为已有参数
             */
            if (!mode.contains("day") && !mode.contains("week") && !mode.contains("month") && !mode.contains("setu")) {
                event.subject.sendMessage("请输入正确的排行榜命令 ${Setting.command.showRank}[day|week|month|setu]-页码")
                return
            }

            /**
             * 进行数据分发请求
             */
            if (mode.contains("day")) {
                data = TotalProcessing().dealWith("illust", "daily", page, perPage, date)
            }

            if (mode.contains("week")) {
                data = TotalProcessing().dealWith("illust", "weekly", page, perPage, date)
            }

            if (mode.contains("month")) {
                data = TotalProcessing().dealWith("illust", "monthly", page, perPage, date)
            }

            if (mode.contains("setu")) {
                /**
                 * 判断该群是否有权查看涩图
                 */
                if (Setting.groups.indexOf(event.group.id.toString()) >= 0) {
                    data = TotalProcessing().dealWith("illust", "daily_r18", page, perPage, date)
                } else {
                    event.subject.sendMessage("该群暂时无权限查看涩图排行榜")
                    return
                }
            }


            /**
             * 针对数据为空进行通知
             */
            if (null == data || data.jsonObject["errors"].toString().isEmpty()) {
                event.subject.sendMessage("当前排行榜暂无数据")
                return
            }


            var message: Message = At(event.sender).plus("\n").plus("======插画排行榜($mode)======").plus("\n")
            val illusts = data.jsonObject["illusts"]?.jsonArray

            val nodes = mutableListOf<ForwardMessage.Node>()


            for (i in (num - 10) until num) {
                val id = illusts?.get(i)?.jsonObject?.get("id")?.jsonPrimitive?.content
                val title = illusts?.get(i)?.jsonObject?.getValue("title")?.jsonPrimitive?.content

                val user = illusts?.get(i)?.jsonObject?.get("user")?.jsonObject?.get("name")?.jsonPrimitive?.content

//                val large =
//                    illusts?.get(i)?.jsonObject?.get("image_urls")?.jsonObject?.get("large")?.jsonPrimitive?.content

                val large = if (null != illusts?.get(i)?.jsonObject?.get("meta_single_page")?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content){
                    illusts[i].jsonObject["meta_single_page"]?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content
                }else{
                    illusts?.get(i)?.jsonObject?.get("meta_pages")?.jsonArray?.get(0)?.jsonObject?.get("image_urls")?.jsonObject?.get("original")?.jsonPrimitive?.content
                }

                val type = illusts?.get(i)?.jsonObject?.get("type")?.jsonPrimitive?.content

                val sanityLevel = illusts?.get(i)?.jsonObject?.get("sanity_level")?.jsonPrimitive?.content?.toInt()
                message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")

                if (Setting.config.rankAndTagAndUserByForward) {
                    var tempMessage = PlainText("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")
//                val detail = PicDetails.getDetailOfId(id!!)

                    if ("ugoira".contentEquals(type)) {
                        val toExternalResource = PicDetails.getUgoira(id!!.toLong())?.toExternalResource()
                        val imageId: String? = toExternalResource?.uploadAsImage(event.group)?.imageId
                        withContext(Dispatchers.IO) {
                            toExternalResource?.close()
                        }
                        if (null != imageId) {
                            /**
                             * 判断是否配置了撤回时间
                             */

                            tempMessage = if (sanityLevel != 6 || enable) {
                                tempMessage.plus(Image(imageId))
                            } else {
                                tempMessage.plus("无权限查看涩图")
                            }
                        }

                    } else {
                        val toExternalResource =
                            ImageUtil.getImage(
                                large!!.replace("i.pximg.net", "i.acgmx.com"),
                                CacheUtil.Type.PIXIV
                            ).toByteArray().toExternalResource()
                        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                        withContext(Dispatchers.IO) {
                            toExternalResource.close()
                        }
                        tempMessage = if (sanityLevel != 6 || enable) {
                            tempMessage.plus(Image(imageId))
                        } else {
                            tempMessage.plus("无权限查看涩图")
                        }
                    }

                    nodes.add(
                        ForwardMessage.Node(
                            senderId = event.bot.id,
                            senderName = event.bot.nameCardOrNick,
                            time = System.currentTimeMillis().toInt(),
                            message = tempMessage
                        )
                    )
                }

            }

            if (Setting.config.rankAndTagAndUserByForward) {
                val forward = RawForwardMessage(nodes).render(object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String {
                        return "Pixiv排行榜"
                    }

                    override fun generateSummary(forward: RawForwardMessage): String {
                        return "查看${nodes.size}条图片"
                    }
                })
                event.subject.sendMessage(forward)
            } else {
                event.subject.sendMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        event.subject.sendMessage(message)
    }

}