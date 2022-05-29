package com.hcyacg.rank

import com.hcyacg.details.PicDetails
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
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
                    event.message.content.replace(Command.showRank, "").replace(" ", "").split("-")[1].toInt()
            } catch (e: Exception) {
                mode = event.message.content.replace(Command.showRank, "").replace(" ", "")
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
                    mode = event.message.content.replace(Command.showRank, "").replace(" ", "").split("-")[0]
                } catch (e: java.lang.Exception) {
                    event.subject.sendMessage("请输入正确的排行榜命令 ${Command.showRank}[day|week|month|setu]-页码")
                    return
                }
            }
            /**
             * 判断是否为已有参数
             * daily	每日
             * weekly	每周
             * monthly	每月
             * rookie	新画师
             * original	原创
             * male	男性向
             * female	女性向
             * daily_r18	每日工口
             * weekly_r18	每周工口
             * male_r18	男性工口
             * female_r18	女性腐向
             * r18g	工口加强型（猎奇）
             */
            val modeList = mutableListOf(
                "daily",
                "weekly",
                "monthly",
                "rookie",
                "original",
                "male",
                "female",
                "daily_r18",
                "weekly_r18",
                "male_r18",
                "female_r18",
                "r18g"
            )

            if (!modeList.contains(mode)) {
                event.subject.sendMessage("请输入正确的排行榜命令 ${Command.showRank}[$modeList]-页码")
                return
            }

            /**
             * 进行数据分发请求
             */

            val r18List = mutableListOf("daily_r18", "weekly_r18", "male_r18", "female_r18", "r18g")
            if (r18List.contains(mode)) {
                /**
                 * 判断该群是否有权查看涩图
                 */
                if (Setting.groups.indexOf(event.group.id.toString()) < 0) {
                    event.subject.sendMessage("该群暂时无权限查看涩图排行榜")
                    return
                }
            }
            data = TotalProcessing().dealWith("illust", mode, page, perPage, date)


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
            var isR18 = false

            for (i in (num - 10) until num) {
                val id = illusts?.get(i)?.jsonObject?.get("id")?.jsonPrimitive?.content
                val title = illusts?.get(i)?.jsonObject?.getValue("title")?.jsonPrimitive?.content

                val user = illusts?.get(i)?.jsonObject?.get("user")?.jsonObject?.get("name")?.jsonPrimitive?.content


//                val large =
//                    illusts?.get(i)?.jsonObject?.get("image_urls")?.jsonObject?.get("large")?.jsonPrimitive?.content

                val large =
                    if (null != illusts?.get(i)?.jsonObject?.get("meta_single_page")?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content) {
                        illusts[i].jsonObject["meta_single_page"]?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content
                    } else {
                        illusts?.get(i)?.jsonObject?.get("meta_pages")?.jsonArray?.get(0)?.jsonObject?.get("image_urls")?.jsonObject?.get(
                            "original"
                        )?.jsonPrimitive?.content
                    }

                val type = illusts?.get(i)?.jsonObject?.get("type")?.jsonPrimitive?.content
                val pageCount = illusts?.get(i)?.jsonObject?.get("page_count")?.jsonPrimitive?.content
                val sanityLevel = illusts?.get(i)?.jsonObject?.get("sanity_level")?.jsonPrimitive?.content?.toInt()
                if (sanityLevel == 6 && !isR18) {
                    isR18 = true
                }
                message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")

                if (Config.forward.rankAndTagAndUserByForward) {
                    var tempMessage =
                        PlainText("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("  作品共${pageCount}张")
                            .plus("\n")
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

                            tempMessage = if (sanityLevel == 6 && enable) {
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

            if (Config.forward.rankAndTagAndUserByForward) {
                val forward = RawForwardMessage(nodes).render(object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String {
                        return "Pixiv排行榜"
                    }

                    override fun generateSummary(forward: RawForwardMessage): String {
                        return "查看${nodes.size}条图片"
                    }
                })
                if (isR18 && Config.recall != 0L) {
                    event.subject.sendMessage(forward).recallIn(Config.recall)
                } else {
                    event.subject.sendMessage(forward)
                }

            } else {
                event.subject.sendMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        event.subject.sendMessage(message)
    }

}