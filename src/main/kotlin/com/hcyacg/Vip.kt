package com.hcyacg

import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Headers

object Vip {
    private val headers = Headers.Builder().add("authorization", Config.token.acgmx)

    //https://api.acgmx.com/pays/pay?vip=1&json=false&base64=true

    suspend fun buy(event:GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }
        val message = event.message.contentToString()
        var type = 1
        var typeName = "月费"


        val packages = RequestUtil.request(RequestUtil.Companion.Method.GET, "https://api.acgmx.com/vips/vips", null, headers.build())



        when (message.replace("购买","").replace("会员","")){
            "月费" -> {
                type = 1
                typeName = "月费"
            }
            "季度" -> {
                type = 2
                typeName = "季度"
            }
            "半年" -> {
                type = 3
                typeName = "半年"
            }
            "年费" -> {
                type = 4
                typeName = "年费"
            }
        }
        val url = "https://api.acgmx.com/pays/pay?vip=$type&json=false&base64=true"

        val request = RequestUtil.request(RequestUtil.Companion.Method.GET, url, null, headers.build())
        if (request != null) {
            val base = request.jsonObject["data"]?.jsonPrimitive?.content
            val toExternalResource = base?.let { ImageUtil.generateImage(it) }?.toExternalResource()
            val image = toExternalResource?.uploadAsImage(event.group)
            val price = packages?.jsonObject?.get("data")?.jsonArray?.get(0)?.jsonObject?.get("vipPackages")?.jsonArray?.get(type -1)?.jsonObject?.get("price")?.jsonPrimitive?.content
            var discount = packages?.jsonObject?.get("data")?.jsonArray?.get(0)?.jsonObject?.get("vipPackages")?.jsonArray?.get(type -1)?.jsonObject?.get("discount")?.jsonPrimitive?.content
            if (null == discount){
                discount = "0"
            }

            image?.let { event.subject.sendMessage(it.plus("\n").plus("お買い上げありがとうございます").plus("\n").plus("你正在购买${typeName}会员,请使用支付宝进行支付~").plus("\n").plus("共计${price},折扣${discount}%")) }
        }

    }
}