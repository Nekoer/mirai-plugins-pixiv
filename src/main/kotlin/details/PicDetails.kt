package com.hcyacg.details

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil.Companion
import com.hcyacg.utils.RequestUtil.Companion.requestObject
import com.hcyacg.utils.ZipUtil
import com.madgag.gif.fmsware.AnimatedGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import org.apache.commons.lang3.StringUtils
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO


object PicDetails {
    private val headers = Headers.Builder().add("token", Setting.config.token.acgmx)
    private val requestBody: RequestBody? = null
    private var isChange: Boolean = false
    private val logger = MiraiLogger.Factory.create(this::class.java)

    suspend fun getDetailOfId(event: GroupMessageEvent, logger: MiraiLogger) {
        val data: JSONObject?
        val messageChain: MessageChain = event.message

        if (!event.message.contentToString().contains(Setting.command.getDetailOfId)) {
            return
        }

        /**
         * 获取要查询的id和图片的张数，通过分割获取
         */
        var id: String? = null
        var page: String?
        try {
            page =
                messageChain.contentToString().replace(Setting.command.getDetailOfId, "").replace(" ", "").split("-")[1]
        } catch (e: Exception) {
            id = messageChain.contentToString().replace(Setting.command.getDetailOfId, "").replace(" ", "")
            page = "1"
        }

        if (null == id) {
            try {
                id = messageChain.content.replace(Setting.command.getDetailOfId, "").replace(" ", "").split("-")[0]
            } catch (e: Exception) {
                event.subject.sendMessage("请输入正确的插画id  ${Setting.command.getDetailOfId}id")
                return
            }
        }


        if (StringUtils.isBlank(id)) {
            event.subject.sendMessage("请输入正确的插画id ${Setting.command.getDetailOfId}id")
            return
        }


        /**
         * 设置出现异常时默认的张数
         */
        if (null == page || page.toInt() <= 0) {
            page = "1"
        }



        data = requestObject(
            Companion.Method.GET,
            "https://api.acgmx.com/illusts/detail?illustId=$id&reduction=true",
            requestBody,
            headers.build(),
            logger
        )

        val tempData = JSONObject.parseObject(JSONObject.parseObject(data!!.getString("data")).getString("illust"))
        /**
         * 判断该id是否有数据
         */
        if (null == tempData) {
            event.subject.sendMessage("该作品是被删除或不存在的作品ID.")
            return
        }
        val picId = tempData.getString("id")
        val title = tempData.getString("title")
        val type = tempData.getString("type")
        val author = JSONObject.parseObject(tempData.getString("user")).getString("name")
        val authorId = JSONObject.parseObject(tempData.getString("user")).getString("id")
        var large = JSONObject.parseObject(tempData.getString("image_urls")).getString("large")
        val pageCount = tempData.getIntValue("page_count")
        val sanityLevel = tempData.getIntValue("sanity_level")
        if (sanityLevel == 6 && Setting.groups.indexOf(event.group.id.toString()) < 0) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }

        if ("ugoira".contentEquals(type)) {
            val imageId = getUgoira(picId.toLong(), event)
            if (null != imageId) {
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

                if (sanityLevel == 6 && StringUtils.isNotBlank(Setting.config.recall.toString()) && Setting.config.recall != 0L) {
                    event.subject.sendMessage(message).recallIn(Setting.config.recall)
                } else {
                    event.subject.sendMessage(message)
                }
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
        large = if (pageCount > 1) {
            JSONObject.parseObject(
                JSONObject.parseObject(JSONArray.parseArray(tempData.getString("meta_pages"))[page.toInt() - 1].toString())
                    .getString("image_urls")
            ).getString("original")
        } else {
            JSONObject.parseObject(tempData.getString("meta_single_page")).getString("original_image_url")
        }

        val toExternalResource =
            ImageUtil.getImage(large.replace("i.pximg.net", "i.acgmx.com")).toByteArray().toExternalResource()
        val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
        toExternalResource.close()

        val message: Message = At(event.sender)
            .plus(Image(imageId)).plus("\n")
            .plus("ID: $picId").plus("\n")
            .plus("标题: $title").plus("\n")
            .plus("画师: $author").plus("\n")
            .plus("画师ID: $authorId").plus("\n")
            .plus("当前共有: $pageCount 张,现处在 $page 张")

        /**
         * 判断key是否配置，未配置提醒用户
         */
        if (Setting.config.token.acgmx.isBlank()) {
            message.plus("\n").plus("您未配置acgmx_token,请到https://www.acgmx.com/account申请")
        }

        /**
         * 判断是否配置了撤回时间
         */

        if (sanityLevel == 6 && StringUtils.isNotBlank(Setting.config.recall.toString()) && Setting.config.recall != 0L) {
            event.subject.sendMessage(message).recallIn(Setting.config.recall)
        } else {
            event.subject.sendMessage(message)
        }

    }

    suspend fun getUgoira(ugoiraId: Long, event: GroupMessageEvent): String? {
//        val ugoiraId = 97727495
        val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
        val headers = Headers.Builder()
        val dir = File(System.getProperty("user.dir") + File.separator + "cache" + File.separator + ugoiraId)
        val data: JSONObject?
        try {
            data = requestObject(
                Companion.Method.GET,
                "https://api.acgmx.com/illusts/ugoira_metadata?illustId=$ugoiraId",
                requestBody,
                headers.build(),
                logger
            )
            val tempData =
                JSONObject.parseObject(JSONObject.parseObject(data!!.getString("data")).getString("ugoira_metadata"))
            val zipUrl = JSONObject.parseObject(tempData.getString("zip_urls")).getString("medium")
                .replace("i.pximg.net", "i.acgmx.com")


            if (!dir.exists()) {
                dir.mkdirs()
            }
            val output = File(dir, "${ugoiraId}.zip")

            val response: Response = client.build().newCall(
                Request.Builder().url(zipUrl)
                    .headers(headers.build()).get().build()
            ).execute()

            output.writeBytes(response.body?.byteStream()?.readBytes()!!)

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
                e.addFrame(src[i]) //添加到帧中
            }

            e.finish()

            val file = File(dir.path + File.separator +  "screenshot.gif")

            val toExternalResource = file.readBytes().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            return imageId
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            dir.deleteRecursively()
        }
    }
}