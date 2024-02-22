package com.hcyacg


import com.hcyacg.utils.*
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
import java.math.RoundingMode
import java.text.DecimalFormat

object Nsfw {
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()

    private val tag = mutableMapOf<String,String>()
    private val json = Json
    const val url = "https://raw.fastgit.org/Nekoer/mirai-plugins-pixiv/master/src/main/resources/tags.json"
    init {
        val file = File(MiraiConsole.pluginManager.pluginsConfigFolder.path + File.separator +"com.hcyacg.pixiv"+ File.separator+ "tags.json")

        if (!file.exists()){
            DownloadUtil.download(
                url,
                file.parentFile.path,
                object : DownloadUtil.OnDownloadListener {
                    override fun onDownloadSuccess() {
                        logger.info("tags.json下载成功")
                        val jsonTemp = json.parseToJsonElement(file.readText())
                        jsonTemp.jsonObject["data"]!!.jsonObject.forEach { t, u ->
                            tag[t] = u.jsonPrimitive.content
                        }
                    }

                    override fun onDownloading(progress: Int) {

                    }

                    override fun onDownloadFailed() {
                        logger.warning("tags.json下载失败,请使用代理或者手动下载 ${url.replace("raw.githubusercontent.com","raw.fastgit.org")} 并存放到 ${file.parentFile.path}")

                    }
                }
            )
        }else{
            val data = RequestUtil.request(RequestUtil.Companion.Method.GET,url,null, headers.build())
            val jsonTemp = json.parseToJsonElement(file.readText())
            if (data != null) {
                if (!data.jsonObject["version"]!!.jsonPrimitive.content.contentEquals(jsonTemp.jsonObject["version"]!!.jsonPrimitive.content)){
                    DownloadUtil.download(
                        url,
                        file.parentFile.path,
                        object : DownloadUtil.OnDownloadListener {
                            override fun onDownloadSuccess() {
                                logger.info("tags.json下载成功")
                                jsonTemp.jsonObject["data"]!!.jsonObject.forEach { t, u ->
                                    tag[t] = u.jsonPrimitive.content
                                }
                            }

                            override fun onDownloading(progress: Int) {

                            }

                            override fun onDownloadFailed() {
                                logger.warning("tags.json下载失败,请使用代理或者手动下载 ${url.replace("raw.githubusercontent.com","raw.fastgit.org")} 并存放到 ${file.parentFile.path}")
                            }
                        }
                    )
                }else{
                    jsonTemp.jsonObject["data"]!!.jsonObject.forEach { t, u ->
                        tag[t] = u.jsonPrimitive.content
                    }
                }
            }else{
                jsonTemp.jsonObject["data"]!!.jsonObject.forEach { t, u ->
                    tag[t] = u.jsonPrimitive.content
                }
            }
        }


    }

    suspend fun load(event: GroupMessageEvent) {
        println("检测中")
        val picUri = DataUtil.getImageLink(event.message) ?: return
        val url = "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"

        val uri = "https://api.dnlab.net/animepic/upload"


        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        val body = ImageUtil.getImage(url, CacheUtil.Type.NONSUPPORT).toByteArray()
        val bodies = body.toRequestBody(
                "image/*".toMediaTypeOrNull(),
                0, body.size
            )
        requestBody.addFormDataPart("img", "${picUri}.png", bodies)

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
                quoteReply.plus("${tag[t]}(${t}):$lv\n")
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
                    quoteReply.plus("${tag[t]}(${t}):$lv\n")
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
                    quoteReply.plus("${tag[t]}(${t}):$lv\n")
                }else{
                    quoteReply.plus("${t}:$lv\n")
                }
            }

        }

        event.subject.sendMessage(quoteReply.plus("\n").plus("本功能不保证长期使用,并且标签为机翻,如果有错误请到Github仓库PR"))
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