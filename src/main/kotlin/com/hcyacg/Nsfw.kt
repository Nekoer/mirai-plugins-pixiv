package com.hcyacg

import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.serialization.json.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

object Nsfw {
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()
    private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS).followRedirects(false)

    private val tag = mutableMapOf<String,String>()
    private val json = Json
    init {
        val file = File(MiraiConsole.pluginManager.pluginsConfigFolder.path + File.separator +"com.hcyacg.pixiv"+ File.separator+ "tags.json")
        if (!file.exists()){
            val resourceAsStream: InputStream? =
                Nsfw::class.java.classLoader.getResourceAsStream("tags.json")
            resourceAsStream?.let { file.writeBytes(it.readAllBytes()) }
        }

        val jsonTemp = json.parseToJsonElement(file.readText())
        jsonTemp.jsonObject.forEach { t, u ->
            tag[t]=u.jsonPrimitive.content
        }
    }

    suspend fun load(event: GroupMessageEvent) {
        println("检测中")
        val picUri = DataUtil.getSubString(event.message.toString().replace(" ", ""), "[mirai:image:{", "}.")!!
            .replace("-", "")
        val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"

        val uri = "https://api.dnlab.net/animepic/upload"


        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        val body = RequestBody.create(
            "image/*".toMediaTypeOrNull(),
            ImageUtil.getImage(url, CacheUtil.Type.NONSUPPORT).toByteArray()
        )
        requestBody.addFormDataPart("img", "${picUri}.png", body)

        headers.add(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36 Edg/85.0.564.44"
        )


        val data = RequestUtil.request(RequestUtil.Companion.Method.POST, uri, requestBody.build(), headers.build())


        if (null == data) {
            return
        }
        var quoteReply: MessageChain = QuoteReply(event.message).plus("")
        val format = DecimalFormat("#.##")
        //舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        //安全性 system
        quoteReply = quoteReply.plus("===安全性===\n")
        data.jsonObject["system"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.", "")

            if (lv.length < 2) {
                lv = lv.plus("0")
            }

            lv = lv.plus("%")
            quoteReply = if (null != tag[t] && tag[t] != ""){
                quoteReply.plus("${tag[t]}:$lv\n")
            }else{
                quoteReply.plus("${t}:$lv\n")
            }

        }

        //角色识别 character
        quoteReply = quoteReply.plus("===角色识别===\n")

        data.jsonObject["character"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.", "")
            if (lv.length < 2) {
                lv = lv.plus("0")
            }
            if (lv.toInt() >= 90) {
                lv = lv.plus("%")
                quoteReply = if (null != tag[t] && tag[t] != ""){
                    quoteReply.plus("${tag[t]}:$lv\n")
                }else{
                    quoteReply.plus("${t}:$lv\n")
                }
            }
        }
        //其他标签 general
        quoteReply = quoteReply.plus("===其他标签===\n")
        data.jsonObject["general"]?.jsonObject?.forEach { t, u ->
            var lv = format.format(u.jsonPrimitive.content.toFloat()).replace("0.", "")
            if (lv.length < 2) {
                lv = lv.plus("0")
            }
            if (lv.toInt() >= 90) {
                lv = lv.plus("%")
                quoteReply = if (null != tag[t] && tag[t] != ""){
                    quoteReply.plus("${tag[t]}:$lv\n")
                }else{
                    quoteReply.plus("${t}:$lv\n")
                }
            }

        }

        event.subject.sendMessage(quoteReply)
    }

    private fun translate(data: MutableList<String>): JsonElement? {
        return try {
//            val url = "http://fanyi.youdao.com/translate?&doctype=json&type=EN2ZH_CN&i=$data"
//            val temp = RequestUtil.request(RequestUtil.Companion.Method.GET, url, null, headers.build())
//            temp!!.jsonObject["translateResult"]!!.jsonArray[0].jsonArray[0].jsonObject["tgt"]!!.jsonPrimitive.content
            val url = "http://api.interpreter.caiyunai.com/v1/translator"
            val headers = Headers.Builder()
            headers.add("content-type", "application/json")
            headers.add("x-authorization","token 彩云小译Token")
            var msg = "["
            data.toTypedArray().forEach {
                msg = msg.plus("\"${it.replace("_"," ").replace("-"," ")}\",")
            }
            msg = msg.substring(0,msg.length - 1).plus("]")
            val body = "{\n" +
                    "        \"source\": ${msg},\n" +
                    "        \"trans_type\": \"en2zh\",\n" +
                    "        \"request_id\": \"demo\",\n" +
                    "        \"detect\": true\n" +
                    "    }"
            val temp = RequestUtil.request(RequestUtil.Companion.Method.POST, url, body.toRequestBody(), headers.build())
            println(temp)
            temp?.jsonObject?.get("target")?.jsonArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}