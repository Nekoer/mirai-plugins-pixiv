package com.hcyacg.details

import com.hcyacg.entity.PixivImageDetail
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import com.hcyacg.lowpoly.LowPoly
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil.Companion
import com.hcyacg.utils.RequestUtil.Companion.request
import com.hcyacg.utils.ZipUtil
import com.hcyacg.utils.logger
import com.madgag.gif.fmsware.AnimatedGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.*
import org.apache.commons.lang3.StringUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO


object PicDetails {
    private val headers = Headers.Builder().add("token", Config.token.acgmx).add("referer", "https://www.acgmx.com")
    private val requestBody: RequestBody? = null
    private var isChange: Boolean = false
    private val logger by logger()
    private val json = Json { ignoreUnknownKeys = true }


    suspend fun load(event: GroupMessageEvent){

        val messageChain: MessageChain = event.message

        if (!event.message.contentToString().contains(Command.getDetailOfId)) {
            return
        }
        event.subject.sendMessage(At(event.sender).plus("正在获取中,请稍后"))
        /**
         * 获取要查询的id和图片的张数，通过分割获取
         */
        var id: String? = null
        var page: String?
        try {
            page =
                messageChain.contentToString().replace(Command.getDetailOfId, "").replace(" ", "").split("-")[1]
        } catch (e: Exception) {
            logger.error { e.message }
            id = messageChain.contentToString().replace(Command.getDetailOfId, "").replace(" ", "")
            page = "1"
        }

        if (null == id) {
            try {
                id = messageChain.content.replace(Command.getDetailOfId, "").replace(" ", "").split("-")[0]
            } catch (e: Exception) {
                logger.error { e.message }
                event.subject.sendMessage("请输入正确的插画id  ${Command.getDetailOfId}id")
                return
            }
        }


        if (StringUtils.isBlank(id)) {
            event.subject.sendMessage("请输入正确的插画id ${Command.getDetailOfId}id")
            return
        }


        /**
         * 设置出现异常时默认的张数
         */
        if (null == page || page.toInt() <= 0) {
            page = "1"
        }

        val detail = getDetailOfId(id)



        /**
         * 判断该id是否有数据
         */
        if (null == detail) {
            event.subject.sendMessage("该作品是被删除或不存在的作品ID亦或您不是本站会员.")
            return
        }

        val picId = detail.id
        val title = detail.title
        val type = detail.type

        val author = detail.user!!.name
        val authorId = detail.user.id
        var large = detail.imageUrls?.large
        val pageCount = detail.pageCount!!
        val sanityLevel = detail.sanityLevel
        if (sanityLevel == 6 && Setting.groups.indexOf(event.group.id.toString()) < 0) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }

        if ("ugoira".contentEquals(type)) {
//            val toExternalResource = if (sanityLevel == 6 && Config.lowPoly){
//                val byte = getUgoira(picId!!.toLong())
//                LowPoly.generate(
//                    ByteArrayInputStream(byte),
//                    200,
//                    1F,
//                    true,
//                    "png",
//                    false,
//                    200
//                ).toByteArray().toExternalResource()
//            }else{
//                getUgoira(picId!!.toLong())!!.toExternalResource()
//            }
            val toExternalResource = getUgoira(picId!!.toLong())!!.toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            val message: Message = At(event.sender)
                .plus(Image(imageId)).plus("\n")
                .plus("ID: $picId").plus("\n")
                .plus("标题: $title").plus("\n")
                .plus("画师: $author").plus("\n")
                .plus("画师ID: $authorId").plus("\n")
                .plus("当前共有: $pageCount 张,现处在 $page 张")

            /**
             * 判断是否配置了撤回时间
             */

            if (sanityLevel == 6 && StringUtils.isNotBlank(Config.recall.toString()) && Config.recall != 0L && !Config.lowPoly) {
                event.subject.sendMessage(message).recallIn(Config.recall)
            } else {
                event.subject.sendMessage(message)
            }

            return
        }

        /**
         * 判断是否超出图片的总张数
         */
        if (page.toInt() > pageCount) {
            event.subject.sendMessage("已超出该图片的数量，该图片共 $pageCount 张")
            return
        }

        /**
         * 通过张数判断读取哪个json数据
         */

        if (Config.forward.imageToForward && page.toInt() == 1){
            val nodes = mutableListOf<ForwardMessage.Node>()
            nodes.add(
                ForwardMessage.Node(
                    senderId = event.bot.id,
                    senderName = event.bot.nameCardOrNick,
                    time = System.currentTimeMillis().toInt(),
                    message = PlainText("ID: $picId").plus("\n")
                        .plus("标题: $title").plus("\n")
                        .plus("画师: $author").plus("\n")
                        .plus("画师ID: $authorId")
                )
            )
            if (null != detail.metaPages){
                detail.metaPages.forEach {
                    val toExternalResource = if (sanityLevel == 6 && Config.lowPoly){
                        val byte = ImageUtil.getImage(it.imageUrls?.original!!.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray()
                        LowPoly.generate(
                            ByteArrayInputStream(byte),
                            200,
                            1F,
                            true,
                            "png",
                            false,
                            200
                        ).toByteArray().toExternalResource()
                    }else{
                        ImageUtil.getImage(it.imageUrls?.original!!.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray().toExternalResource()
                    }


                    val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                    nodes.add(
                        ForwardMessage.Node(
                            senderId = event.bot.id,
                            senderName = event.bot.nameCardOrNick,
                            time = System.currentTimeMillis().toInt(),
                            message = Image(imageId)
                        )
                    )

                    withContext(Dispatchers.IO) {
                        toExternalResource.close()
                    }
                }
            }

            if (null != detail.metaSinglePage && detail.metaSinglePage.originalImageUrl?.isNotBlank() == true){

                val toExternalResource = if (sanityLevel == 6 && Config.lowPoly){
                    val byte = ImageUtil.getImage(detail.metaSinglePage.originalImageUrl.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray()
                    LowPoly.generate(
                        ByteArrayInputStream(byte),
                        200,
                        1F,
                        true,
                        "png",
                        false,
                        200
                    ).toByteArray().toExternalResource()
                }else{
                    ImageUtil.getImage(detail.metaSinglePage.originalImageUrl.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray().toExternalResource()
                }

                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                nodes.add(
                    ForwardMessage.Node(
                        senderId = event.bot.id,
                        senderName = event.bot.nameCardOrNick,
                        time = System.currentTimeMillis().toInt(),
                        message = Image(imageId)
                    )
                )

                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }
            }


            val forward = RawForwardMessage(nodes).render(object : ForwardMessage.DisplayStrategy {
                override fun generateTitle(forward: RawForwardMessage): String {
                    return "$title"
                }

                override fun generateSummary(forward: RawForwardMessage): String {
                    return "查看${pageCount}张图片"
                }
            })
            /**
             * 判断是否配置了撤回时间
             */

            if (sanityLevel == 6 && StringUtils.isNotBlank(Config.recall.toString()) && Config.recall != 0L && !Config.lowPoly) {
                event.subject.sendMessage(forward).recallIn(Config.recall)
            } else {
                event.subject.sendMessage(forward)
            }
            return
        }

        large = if (pageCount > 1) {
            detail.metaPages!![page.toInt() - 1].imageUrls!!.original
        } else {
            detail.metaSinglePage!!.originalImageUrl
        }

        val toExternalResource = if (sanityLevel == 6 && Config.lowPoly){
            val byte = ImageUtil.getImage(large!!.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray()
            LowPoly.generate(
                ByteArrayInputStream(byte),
                200,
                1F,
                true,
                "png",
                false,
                200
            ).toByteArray().toExternalResource()
        }else{
            ImageUtil.getImage(large!!.replace("i.pximg.net", "i.acgmx.com"),CacheUtil.Type.PIXIV).toByteArray().toExternalResource()
        }

        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
        withContext(Dispatchers.IO) {
            toExternalResource.close()
        }

        val imageMessage: Message = Image(imageId)
        val message: Message = At(event.sender).plus("\n")
            .plus("ID: $picId").plus("\n")
            .plus("标题: $title").plus("\n")
            .plus("画师: $author").plus("\n")
            .plus("画师ID: $authorId").plus("\n")
            .plus("当前共有: $pageCount 张,现处在 $page 张")

        /**
         * 判断key是否配置，未配置提醒用户
         */
        if (Config.token.acgmx.isBlank()) {
            event.subject.sendMessage("您未配置acgmx_token,请到https://www.acgmx.com/account申请")
            return
        }

        /**
         * 判断是否配置了撤回时间
         */

        if (sanityLevel == 6 && StringUtils.isNotBlank(Config.recall.toString()) && Config.recall != 0L && !Config.lowPoly) {
            event.subject.sendMessage(message)
            event.subject.sendMessage(imageMessage).recallIn(Config.recall)
        } else {
            event.subject.sendMessage(message)
            event.subject.sendMessage(imageMessage)
        }

    }

    private fun getDetailOfId(id:String): PixivImageDetail? {
        try {
            val data = request(
                Companion.Method.GET,
                "https://api.acgmx.com/illusts/detail?illustId=$id&reduction=true",
                requestBody,
                headers.build()
            ) ?: return null

            val tempData = data.jsonObject["data"]?.jsonObject?.get("illust")

            return tempData?.let<JsonElement, PixivImageDetail> { json.decodeFromJsonElement(it) }
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            return null
        }
    }

    suspend fun getUgoira(ugoiraId: Long): ByteArray? {
//        val ugoiraId = 97727495
        val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
        val dir = File(System.getProperty("user.dir") + File.separator + "cache" + File.separator + ugoiraId)
        val data: JsonElement?
        try {
            data = request(
                Companion.Method.GET,
                "https://api.acgmx.com/illusts/ugoira_metadata?illustId=$ugoiraId",
                requestBody,
                headers.build(),
            )
            if (null == data){
                return null
            }


            val tempData = data.jsonObject["data"]?.jsonObject?.get("ugoira_metadata")
            val zipUrl = tempData?.jsonObject?.get("zip_urls")?.jsonObject?.get("medium")
                ?.jsonPrimitive?.content?.replace("i.pximg.net", "i.acgmx.com")



            if (!dir.exists()) {
                dir.mkdirs()
            }
            val output = File(dir, "${ugoiraId}.zip")

            val response: Response = client.build().newCall(
                Request.Builder().url(zipUrl!!)
                    .headers(headers.build()).get().build()
            ).execute()

            response.body?.byteStream()?.let { output.writeBytes(it.readBytes()) }

            ZipUtil.unzip(dir.path + File.separator + "${ugoiraId}.zip", dir.path + File.separator + "image")

            val e = AnimatedGifEncoder()
            e.setRepeat(0)
            e.setFrameRate(30f)
            e.start(dir.path + File.separator + "screenshot.gif")
            //获取目录下所有jpg文件
            val pic: Array<String> = File("${dir.path}${File.separator}image").list()

            val src: Array<BufferedImage?> = arrayOfNulls<BufferedImage>(pic.size)
            for (i in src.indices) {
                src[i] =
                    withContext(Dispatchers.IO) {
                        ImageIO.read(File(dir.path + File.separator + "image" + File.separator + pic[i]))
                    } // 读入需要播放的jpg文件



                if(Config.lowPoly){
                    val out = ByteArrayOutputStream()
                    withContext(Dispatchers.IO) {
                        ImageIO.write(src[i], "png", out)
                    }
                    e.addFrame(withContext(Dispatchers.IO) {
                        ImageIO.read(
                            ByteArrayInputStream(
                                LowPoly.generate(
                                    ByteArrayInputStream(out.toByteArray()),
                                    200,
                                    1F,
                                    true,
                                    "png",
                                    false,
                                    200
                                ).toByteArray()
                            )
                        )
                    }) //添加到帧中
                }else{
                    e.addFrame(src[i])
                }

            }

            e.finish()

            val file = File(dir.path + File.separator +  "screenshot.gif")

            return file.readBytes()
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            return null
        } finally {
            dir.deleteRecursively()
        }
    }
}