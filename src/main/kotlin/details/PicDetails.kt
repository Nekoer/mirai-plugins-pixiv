package com.hcyacg.details

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config.getDetailOfId
import com.hcyacg.config.Config.acgmx
import com.hcyacg.config.Config.groups
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil.Companion
import com.hcyacg.utils.RequestUtil.Companion.request
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayInputStream
import java.lang.Exception

class PicDetails {
    private val headers = Headers.Builder().add("token", "$acgmx")
    private val requestBody: RequestBody? = null


    suspend fun getDetailOfId(event: GroupMessageEvent, logger: MiraiLogger) {
        var data: JSONObject? = null
        val messageChain: MessageChain = event.message

        /**
         * 获取要查询的id和图片的张数，通过分割获取
         */
        var id: String? = null
        var page: String? = null
        try {
            page = getDetailOfId?.let { messageChain.content.replace(it, "").replace(" ","").split("-")[1] }
        } catch (e: Exception) {
            id = messageChain.content.replace(getDetailOfId!!, "").replace(" ","")
            page = "1"
        }

        if(null == id){
            try {
                id = messageChain.content.replace(getDetailOfId!!, "").replace(" ","").split("-")[0]
            } catch (e: Exception) {
                event.subject.sendMessage("请输入正确的插画id  ${getDetailOfId}id")
                return
            }
        }

        if (StringUtils.isBlank(id)){
            event.subject.sendMessage("请输入正确的插画id ${getDetailOfId}id")
            return
        }


        /**
         * 设置出现异常时默认的张数
         */
        if (null == page || page.toInt() <= 0) {
            page = "1"
        }

        data = request(
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
        if (null == tempData){
            event.subject.sendMessage("该作品是被删除或不存在的作品ID.")
            return
        }
        val picId = tempData.getString("id")
        val title = tempData.getString("title")
        val author = JSONObject.parseObject(tempData.getString("user")).getString("name")
        val authorId = JSONObject.parseObject(tempData.getString("user")).getString("id")
        var large = JSONObject.parseObject(tempData.getString("image_urls")).getString("large")
        val pageCount = tempData.getIntValue("page_count")
        val sanityLevel = tempData.getIntValue("sanity_level")
        if (sanityLevel == 6 && groups.indexOf(event.group.id.toString()) < 0){
            event.subject.sendMessage("该群无权限查看涩图")
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


//        val imageId: String = event.group.uploadImage(ByteArrayInputStream(ImageUtil.getImageFromPixiv(large)?.toByteArray().toExternalResource())).imageId
//        ImageUtil.getImageFromPixiv(large)?.toByteArray()?.toExternalResource()!!.sendAsImageTo(event.group)
        val imageId: String = ImageUtil.getImageFromPixiv(large)?.toByteArray()?.toExternalResource()
            ?.uploadAsImage(event.group)!!.imageId

//        ImageUtil.getImageFromPixiv(large)?.toByteArray()?.toExternalResource()?.let { event.subject.sendImage(it) }
//        event.subject.sendMessage(LightApp(content = "{\"app\":\"com.tencent.mobileqq.reading\",\"desc\":\"\",\"view\":\"singleImg\",\"ver\":\"1.0.0.70\",\"prompt\":\"$title\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\",\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"singleImg\":{\"mainImage\":\"https://gchat.qpic.cn/gchatpic_new/0/0-0-${imageId.replace("-", "").replace("{","").replace("}.mirai","")}/0\",\"mainUrl\":\"https://gchat.qpic.cn/gchatpic_new/0/0-0-${imageId.replace("-", "").replace("{","").replace("}.mirai","")}/0\"}},\"config\":{\"forward\":0,\"showSender\":1},\"text\":\"\",\"extraApps\":[],\"sourceAd\":\"\",\"extra\":\"\"}"))

        val message : Message = At(event.sender)
            .plus(Image(imageId)).plus("\n")
            .plus("ID: $picId").plus("\n")
            .plus("标题: $title").plus("\n")
            .plus("画师: $author").plus("\n")
            .plus("画师ID: $authorId").plus("\n")
            .plus("当前共有: $pageCount 张,现处在 $page 张")

        /**
         * 判断key是否配置，未配置提醒用户
         */
        if (null == acgmx){
            message.plus("\n").plus("您未配置acgmx_token,请到https://www.acgmx.com/account申请")
        }

        event.subject.sendMessage(message)
    }


}