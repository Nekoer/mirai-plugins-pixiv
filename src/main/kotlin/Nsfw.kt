package com.hcyacg

import com.hcyacg.initial.Setting
import com.hcyacg.search.Google
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.math.RoundingMode
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

object Nsfw {
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()
    private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS).followRedirects(false)


    suspend fun load(event: GroupMessageEvent){
        println("检测中")
        val picUri = DataUtil.getSubString(event.message.toString().replace(" ", ""), "[mirai:image:{", "}.")!!
            .replace("-", "")
        val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"

        val host = Setting.config.proxy.host
        val port = Setting.config.proxy.port
        val uri = "https://api.dnlab.net/animepic/upload"


        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        val body = RequestBody.create("image/*".toMediaTypeOrNull(),ImageUtil.getImage(url,CacheUtil.Type.NONSUPPORT).toByteArray())
        requestBody.addFormDataPart("img","${picUri}.png",body)

        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36 Edg/85.0.564.44")


        val data = RequestUtil.request(RequestUtil.Companion.Method.POST,uri,requestBody.build(), headers.build())

        if (null == data){
            return
        }
        var quoteReply: MessageChain = QuoteReply(event.message).plus("")
        val format = DecimalFormat("#.##")
        //舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        //安全性 system
        val system = mutableMapOf<String,String>()
        quoteReply =quoteReply.plus("===安全性===\n")
        data.jsonObject["system"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.","")
            if (lv.length < 2){
                lv = lv.plus("0")
            }
            lv= lv.plus("%")
            quoteReply =quoteReply.plus("${translate(t.replace("_"," ").replace("-"," "))}:$lv\n")
//            system[translate(t.replace("_"," "))] = lv
        }

        //角色识别 character
        quoteReply =quoteReply.plus("===角色识别===\n")
        val character = mutableMapOf<String,String>()
        data.jsonObject["character"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.","")
            if (lv.length < 2){
                lv = lv.plus("0")
            }
            lv= lv.plus("%")
            quoteReply = quoteReply.plus("${translate(t.replace("_"," ").replace("-"," "))}:$lv\n")
//            character[translate(t.replace("_"," "))] = lv
        }
        //其他标签 general
        quoteReply =quoteReply.plus("===其他标签===\n")
        val general = mutableMapOf<String,String>()
        data.jsonObject["general"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.","")
            if (lv.length < 2){
                lv = lv.plus("0")
            }
            lv= lv.plus("%")
            quoteReply = quoteReply.plus("${translate(t.replace("_"," ").replace("-"," "))}:$lv\n")
//            general[translate(t.replace("_"," "))] = lv
        }


        event.subject.sendMessage(quoteReply)

    }

    private fun translate(data: String):String{
        val url = "http://fanyi.youdao.com/translate?&doctype=json&type=EN2ZH_CN&i=$data"
        val temp = RequestUtil.request(RequestUtil.Companion.Method.GET,url,null, headers.build())
        return temp!!.jsonObject["translateResult"]!!.jsonArray[0].jsonArray[0].jsonObject["tgt"]!!.jsonPrimitive.content
    }

}