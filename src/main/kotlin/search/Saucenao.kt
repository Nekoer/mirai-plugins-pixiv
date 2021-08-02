package com.hcyacg.search

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config.saucenao
import com.hcyacg.config.Config.picToSearch
import com.hcyacg.plugin.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
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
import java.net.URL

class Saucenao {
    private val headers = Headers.Builder()
    private val requestBody: RequestBody? = null
    private val jsonObject: JSONObject? = null

    /**
     * 通过图片来搜索信息
     */
    suspend fun picToSearch(event: GroupMessageEvent, logger: MiraiLogger) {
        var data: JSONObject? = null
        val messageChain: MessageChain = event.message


        try{
            /**
             * 未配置key通知用户
             */
            if (null == saucenao){
                event.subject.sendMessage(At(event.sender).plus("您还未配置saucenao的api_key,申请网址为https://saucenao.com/user.php?page=search-api"))
                return
            }

            /**
             * 获取图片的代码
             */
            val picUri = DataUtil.getSubString(messageChain.toString().replace(" ",""), "[mirai:image:{", "}.jpg]")!!.replace("}.png]", "")
                .replace("}.mirai]", "").replace("}.gif]", "")

            data = RequestUtil.request(RequestUtil.Companion.Method.GET, "https://saucenao.com/search.php?db=999&output_type=2&api_key=$saucenao&testmode=1&numres=16&url=https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri.replace("-", "")}/0?", requestBody, headers.build(), logger)
            val header = JSONObject.parseObject(data!!.getString("header"))
            val status = header.getIntValue("status")
            if (status != 0) {
                event.subject.sendMessage(
                    At(event.sender)
                        .plus("您已被限流,请稍后再试").plus("\r\n")
                        .plus("You have been restricted, please try again later")
                )
            }

            val results = JSONArray.parseArray(data.getString("results"))

            /**
             * 通过循环判断当前json数据为哪种网站，进而进行搭配
             */
            for (o in results) {
                var message: Message? = pixivSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

                message = danBooRuSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

                message = seiGaSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

                message = daSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

                message = bcySearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

                message = maSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }
                message = niJieSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }
                message = drawrSearch(event, o)
                if (null != message) {
                    event.subject.sendMessage(message)
                    return
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${picToSearch}图片")
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

            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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
            val imageId: String = ImageUtil.getImage(thumbnail)?.toByteArray()?.toExternalResource()
                ?.uploadAsImage(event.group)!!.imageId
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