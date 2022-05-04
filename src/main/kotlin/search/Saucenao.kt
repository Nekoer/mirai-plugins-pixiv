package com.hcyacg.search

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import com.hcyacg.plugin.utils.DataUtil
import com.hcyacg.utils.ImageUtil.Companion.getImage
import com.hcyacg.utils.ImageUtil.Companion.rotate
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

object Saucenao {
    private val headers = Headers.Builder()
    private val requestBody: RequestBody? = null
    private val jsonObject: JSONObject? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val tracePath: File =
        File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "trace")

    /**
     * 通过图片来搜索信息
     */
    suspend fun picToSearch(event: GroupMessageEvent, picUri: String): List<Message> {
        var data: JSONObject? = null
        val messageChain: MessageChain = event.message
        val dataList = HashMap<Int, JSONObject?>()
        var header: Any? = null
        val list = mutableListOf<Message>()

        try {

            /**
             * 未配置key通知用户
             */
            if (Setting.config.token.saucenao.isBlank()) {
                event.subject.sendMessage(At(event.sender).plus("您还未配置saucenao的api_key,申请网址为https://saucenao.com/user.php?page=search-api"))
                return list
            }

            /**
             * 获取图片的代码
             */
//            val picUri = DataUtil.getSubString(messageChain.toString().replace(" ",""), "[mirai:image:{", "}.")!!
//                .replace("-", "")

            //旋转三次
            val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
            val rotate90 = rotate(withContext(Dispatchers.IO) {
                ImageIO.read(URL(url))
            }, 90).toByteArray().toExternalResource()
            val code90 = DataUtil.getSubString(rotate90.uploadAsImage(event.group).imageId.replace("-", ""), "{", "}")
            withContext(Dispatchers.IO) {
                rotate90.close()
            }

            val rotate180 = rotate(withContext(Dispatchers.IO) {
                ImageIO.read(URL(url))
            }, 180).toByteArray().toExternalResource()
            val code180 = DataUtil.getSubString(rotate180.uploadAsImage(event.group).imageId.replace("-", ""), "{", "}")
            withContext(Dispatchers.IO) {
                rotate180.close()
            }

            val rotate270 = rotate(withContext(Dispatchers.IO) {
                ImageIO.read(URL(url))
            }, 270).toByteArray().toExternalResource()
            val code270 = DataUtil.getSubString(rotate270.uploadAsImage(event.group).imageId.replace("-", ""), "{", "}")
            withContext(Dispatchers.IO) {
                rotate270.close()
            }


            val imageData = getInfo(picUri, logger)
            val imageData90 = getInfo(code90!!, logger)
            val imageData180 = getInfo(code180!!, logger)
            val imageData270 = getInfo(code270!!, logger)


            /**
             * 进行相似度对比，取最大值
             */
            var similarity: Double = 0.0
            var similarity90: Double = 0.0
            var similarity180: Double = 0.0
            var similarity270: Double = 0.0

            var double = mutableListOf<Double>()

            if (null != imageData) {
                header = JSONObject.parseObject(imageData.toString())["header"]
                similarity = JSONObject.parseObject(header.toString()).getDouble("similarity")
                double.add(similarity)
            }

            if (null != imageData90) {
                header = JSONObject.parseObject(imageData90.toString())["header"]
                similarity90 = JSONObject.parseObject(header.toString()).getDouble("similarity")
                double.add(similarity90)
            }

            if (null != imageData180) {
                header = JSONObject.parseObject(imageData180.toString())["header"]
                similarity180 = JSONObject.parseObject(header.toString()).getDouble("similarity")
                double.add(similarity180)
            }

            if (null != imageData270) {
                header = JSONObject.parseObject(imageData270.toString())["header"]
                similarity270 = JSONObject.parseObject(header.toString()).getDouble("similarity")
                double.add(similarity270)
            }

//            double.sort()


            var temp: Double = 0.0
            for (i in double.indices) {
                for (j in i + 1 until double.size) {
                    if (double[i] > double[j]) {
                        temp = double[i]
                        double[i] = double[j]
                        double[j] = temp
                    }
                }
            }

            when (temp) {
                similarity -> {
                    imageData?.let { mate(event, it, logger)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                }
                similarity90 -> {
                    imageData90?.let { mate(event, it, logger)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                }
                similarity180 -> {
                    imageData180?.let { mate(event, it, logger)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                }
                similarity270 -> {
                    imageData270?.let { mate(event, it, logger)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                }
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${Setting.command.picToSearch}图片")
            list.clear()
            return list
        }

    }

    /**
     * 匹配数据格式
     */
    suspend fun mate(event: GroupMessageEvent, data: Any, logger: MiraiLogger): Message? {

        try {
            var message: Message? = pixivSearch(event, data)
            if (null != message) {
                return message
            }

            message = danBooRuSearch(event, data)
            if (null != message) {
                return message
            }

            message = seiGaSearch(event, data)
            if (null != message) {
                return message
            }

            message = daSearch(event, data)
            if (null != message) {
                return message
            }

            message = bcySearch(event, data)
            if (null != message) {
                return message
            }

            message = maSearch(event, data)
            if (null != message) {
                return message
            }
            message = niJieSearch(event, data)
            if (null != message) {
                return message
            }
            message = drawrSearch(event, data)
            if (null != message) {
                return message
            }

        } catch (e: Exception) {
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${Setting.command.picToSearch}图片")
            return null
        }
        return null
    }

    /**
     * 获取四张照片的第一个搜索数据
     */
    fun getInfo(picUri: String, logger: MiraiLogger): JSONObject? {
        var data: JSONObject? = null
        try {
            data = RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://saucenao.com/search.php?db=999&output_type=2&api_key=${Setting.config.token.saucenao}&testmode=1&numres=16&url=https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?",
                requestBody,
                headers.build(),
                logger
            )
            val header = JSONObject.parseObject(data!!.getString("header"))
            val status = header.getIntValue("status")
            if (status != 0) {
                return null
            }
            return JSONObject.parseObject(JSONArray.parseArray(data.getString("results"))[0].toString())
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 以下皆为各个网站的搭配，不详解
     */

    private suspend fun pixivSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val pixivId = JSONObject.parseObject(data.toString()).getString("pixiv_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("member_name")
            val memberId = JSONObject.parseObject(data.toString()).getString("member_id")
            if (StringUtils.isBlank(pixivId)) {
                throw RuntimeException("搜索到的插画id为空")
            }

            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            if (!thumbnail.contains("https://img3.saucenao.com/")) {
                At(event.sender).plus("\r\n").plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $pixivId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            } else {
                At(event.sender).plus("\r\n").plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $pixivId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            }
        } catch (e: java.lang.Exception) {
            null
            //return At(event.sender).plus(e.message.toString())
        }
    }

    private suspend fun danBooRuSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val danbooruId = JSONObject.parseObject(data.toString()).getString("danbooru_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("creator")
            if (StringUtils.isBlank(danbooruId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {
                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("ID: $danbooruId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {
                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("ID: $danbooruId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun seiGaSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val seigaId = JSONObject.parseObject(data.toString()).getString("seiga_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("member_name")
            val memberId = JSONObject.parseObject(data.toString()).getString("member_id")
            if (StringUtils.isBlank(seigaId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $seigaId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $seigaId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun daSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val daId = JSONObject.parseObject(data.toString()).getString("da_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("author_name")
            if (StringUtils.isBlank(daId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $daId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $daId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun bcySearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val bcyId = JSONObject.parseObject(data.toString()).getString("bcy_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("member_name")
            val memberId = JSONObject.parseObject(data.toString()).getString("member_id")
            if (StringUtils.isBlank(bcyId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $bcyId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $bcyId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun maSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val mdId = JSONObject.parseObject(data.toString()).getString("md_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("author")
            if (StringUtils.isBlank(mdId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("ID: $mdId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("ID: $mdId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun niJieSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val nijieId = JSONObject.parseObject(data.toString()).getString("nijie_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("member_name")
            val memberId = JSONObject.parseObject(data.toString()).getString("member_id")
            if (StringUtils.isBlank(nijieId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $nijieId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $nijieId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private suspend fun drawrSearch(event: GroupMessageEvent, everything: Any): Message? {
        val jsonObject: JSONObject? = null
        var header: Any? = null
        var data: Any? = null
        var similarity: String? = null
        var thumbnail: String? = null
        var extUrls: JSONArray? = null
        return try {
            header = JSONObject.parseObject(everything.toString())["header"]
            data = JSONObject.parseObject(everything.toString())["data"]
            similarity = JSONObject.parseObject(header.toString()).getString("similarity")
            thumbnail = JSONObject.parseObject(header.toString()).getString("thumbnail")
            extUrls = JSONArray.parseArray(JSONObject.parseObject(data.toString()).getString("ext_urls"))
            val title = JSONObject.parseObject(data.toString()).getString("title")
            val drawrID = JSONObject.parseObject(data.toString()).getString("drawr_id")
            val memberName = JSONObject.parseObject(data.toString()).getString("member_name")
            val memberId = JSONObject.parseObject(data.toString()).getString("member_id")
            if (StringUtils.isBlank(drawrID)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail, false).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title$title").plus("\r\n")
                    .plus("ID: $drawrID").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $drawrID").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            null
        }
    }


    private fun getExtUrls(extUrls: JSONArray?): String {
        return try {
            val data = StringBuilder()
            for (extUrl in extUrls!!) {
                data.append(extUrl.toString().replace("\"", "")).append("\r\n")
            }
            data.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun getExtOneUrls(extUrls: JSONArray?): String? {
        return try {
            val data = StringBuilder()
            data.append(extUrls!![0]).toString().replace("\"", "")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}