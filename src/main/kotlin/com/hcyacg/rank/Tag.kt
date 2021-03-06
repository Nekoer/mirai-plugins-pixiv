package com.hcyacg.rank

import com.hcyacg.details.PicDetails
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 21:52
 */
object Tag {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)
    suspend fun init(event: GroupMessageEvent) {
        var enable = false

        try {
            if (Setting.groups.contains(event.group.id.toString())) {
                enable = true
            }
            val q = event.message.content.replace(Command.tag, "").replace(" ", "").split("-")[0]
            val page = event.message.content.replace(Command.tag, "").replace(" ", "").split("-")[1].toInt()
            var offset = 0
            var num = 0
            if (page % 3 != 0) {
                offset = ((page - (page % 3)) / 3) * 30 + 30
                num = page % 3 * 10
            } else {
                offset = (page / 3) * 30
                num = 30
            }


            val data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/search?q=$q&offset=$offset",
                requestBody,
                headers.build()
            )

            /**
             * ??????????????????????????????
             */
            if (null == data || data.jsonObject["errors"].toString().isEmpty()) {
                event.subject.sendMessage("???????????????????????????")
                return
            }

            var message: Message = At(event.sender).plus("\n").plus("======???????????????($q)======").plus("\n")
            val illusts = data.jsonObject["illusts"]?.jsonArray

            if (null == illusts) {
                event.subject.sendMessage("tag????????????")
                return
            }
            val nodes = mutableListOf<ForwardMessage.Node>()
            var isR18 = false
            for (i in (num - 10) until num) {
                if (illusts.size > i) {
                    val id = illusts[i].jsonObject["id"]?.jsonPrimitive?.content
                    val title = illusts[i].jsonObject["title"]?.jsonPrimitive?.content

                    val user = illusts[i].jsonObject["user"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                    val pageCount = illusts[i].jsonObject["page_count"]?.jsonPrimitive?.content
//                    val large =
//                        illusts[i].jsonObject["image_urls"]?.jsonObject?.get("large")?.jsonPrimitive?.content

                    val large = if (null != illusts[i].jsonObject["meta_single_page"]?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content){
                        illusts[i].jsonObject["meta_single_page"]?.jsonObject?.get("original_image_url")?.jsonPrimitive?.content
                    }else{
                        illusts[i].jsonObject["meta_pages"]?.jsonArray?.get(0)?.jsonObject?.get("image_urls")?.jsonObject?.get("original")?.jsonPrimitive?.content
                    }
                    val type = illusts[i].jsonObject["type"]?.jsonPrimitive?.content

                    val sanityLevel = illusts[i].jsonObject["sanity_level"]?.jsonPrimitive?.content?.toInt()
                    if (sanityLevel == 6 && !isR18){
                        isR18 = true
                    }

                    message = message.plus("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("\n")
                    if (Config.forward.rankAndTagAndUserByForward) {
                        var tempMessage = PlainText("${(page * 10) - 9 + (i % 10)}. $title - $user - $id").plus("  ?????????${pageCount}???").plus("\n")
//                val detail = PicDetails.getDetailOfId(id!!)

                        if ("ugoira".contentEquals(type)) {
                            val toExternalResource = PicDetails.getUgoira(id!!.toLong())?.toExternalResource()
                            val imageId: String? = toExternalResource?.uploadAsImage(event.group)?.imageId
                            withContext(Dispatchers.IO) {
                                toExternalResource?.close()
                            }
                            if (null != imageId) {
                                /**
                                 * ?????????????????????????????????
                                 */

                                tempMessage = if (sanityLevel != 6 && enable) {
                                    tempMessage.plus(Image(imageId))
                                } else {
                                    tempMessage.plus("?????????????????????")
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
                            tempMessage = if (sanityLevel == 6 || enable) {
                                tempMessage.plus(Image(imageId))
                            } else {
                                tempMessage.plus("?????????????????????")
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
            }
            if (Config.forward.rankAndTagAndUserByForward) {
                val forward = RawForwardMessage(nodes).render(object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String {
                        return "Tag?????????"
                    }

                    override fun generateSummary(forward: RawForwardMessage): String {
                        return "??????${nodes.size}?????????"
                    }
                })
                if (isR18 && Config.recall != 0L){
                    event.subject.sendMessage(forward).recallIn(Config.recall)
                }else{
                    event.subject.sendMessage(forward)
                }
            } else {
                event.subject.sendMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}