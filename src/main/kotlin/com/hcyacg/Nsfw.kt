package com.hcyacg


import com.hcyacg.initial.Config
import com.hcyacg.search.Yandex
import com.hcyacg.utils.*
import kotlinx.serialization.json.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.math.RoundingMode
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


object Nsfw {
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS).followRedirects(false)
    private val tags = mutableMapOf<String,String>()
    private val json = Json
    const val url = "https://fastly.jsdelivr.net/gh/Nekoer/mirai-plugins-pixiv@master/src/main/resources/tags.json"
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
                            tags[t] = u.jsonPrimitive.content
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
                                    tags[t] = u.jsonPrimitive.content
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
                        tags[t] = u.jsonPrimitive.content
                    }
                }
            }else{
                jsonTemp.jsonObject["data"]!!.jsonObject.forEach { t, u ->
                    tags[t] = u.jsonPrimitive.content
                }
            }
        }


    }

    suspend fun load(event: GroupMessageEvent) {
        println("监控中……")
        event.subject.sendMessage(At(event.sender).plus("检测中,请稍后"));

        val picUri = DataUtil.getSubString(event.message.toString().replace("\\s*".toRegex(), "").replace(" ", ""), "[overflow:image,url=", "]")!!


        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        val body = ImageUtil.getImage(picUri, CacheUtil.Type.NONSUPPORT).toByteArray()
        val bodies = body.toRequestBody(
            "multipart/form-data".toMediaTypeOrNull(),
            0, body.size
        )
        requestBody.addFormDataPart("file", "${picUri}.jpeg", bodies)
        requestBody.addFormDataPart("network_type", "general")

        var uri = "http://dev.kanotype.net:8003/deepdanbooru/upload"

        val host = Config.proxy.host
        val port = Config.proxy.port
        headers.add("Content-Type", "multipart/form-data;boundary=----WebKitFormBoundaryHxQHPpxAl7v7sCSa")
        val response: Response? = try {
            if (host.isBlank() || port == -1) {
                client.build().newCall(Request.Builder().url(uri).headers(headers.build()).post(requestBody.build()).build()).execute()
            } else {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
                client.proxy(proxy).build().newCall(Request.Builder().url(uri).headers(headers.build()).post(requestBody.build()).build()).execute()
            }
        }catch (e: Exception){
            logger.error(e.message)
            null
        }

        val location = response?.header("location")

        //构建消息体
        var quoteReply: MessageChain = QuoteReply(event.message).plus("")
        val format = DecimalFormat("#.##")
        //舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR

        if (response?.code != 302){
            logger.warning("HTTP代码：${response?.code}")

            uri = "http://${Config.deepdanbooru}/deepdanbooru"
            val requestB = MultipartBody.Builder().setType(MultipartBody.FORM)

            requestB.addFormDataPart("image","${picUri}.jpeg", bodies)

            headers.add("Content-Type", "multipart/form-data;boundary=ebf9f03029db4c2799ae16b5428b06bd1")
            headers.add("Accept", "application/json")
            val data = try {
                RequestUtil.request(RequestUtil.Companion.Method.POST, uri,requestB.build(),headers.build())
            }catch (e:Exception){
                logger.warning(e.message)
                null
            }
            println(data)
            data?.jsonArray?.forEach {
                val tag = it.jsonObject["tag"]?.jsonPrimitive?.content
                val score = it.jsonObject["score"]?.jsonPrimitive?.float

                var lv = format.format(score).replace("0.", "")
                if (lv.length < 2) {
                    lv = lv.plus("0")
                }
                if (lv.toInt() >= 90) {
                    lv = lv.plus("%")
                    quoteReply = if (null != tags[tag] && tags[tag] != ""){
                        quoteReply.plus("${tags[tag]}(${tag}):$lv\n")
                    }else{
                        quoteReply.plus("${tag}:$lv\n")
                    }
                }
            }

        }else {
            var doc = Jsoup.connect("http://dev.kanotype.net:8003$location").get()
            doc = Jsoup.parse(doc.html())
            val tables = doc.body().getElementsByTag("table")



            tables.forEach { table ->
                val theads = table.select("thead > tr > th")
                val tbody = table.getElementsByTag("tbody")
                theads.forEach { thead ->
                    val title = thead.text()
                    title.let {
                        if (it.contentEquals("General Tags")){
                            if (tbody.size>=1){
                                val trs = tbody[0].select("tr")
                                //其他标签 general
                                quoteReply = quoteReply.plus("===其他标签===\n")
                                trs.forEach {tr ->
                                    val td = tr.select("td")
                                    val tag = td[0].getElementsByTag("a").text()
                                    val score = td[1].text()
                                    var lv = format.format(score.toFloat()).replace("0.", "")
                                    if (lv.length < 2) {
                                        lv = lv.plus("0")
                                    }
                                    if (lv.toInt() >= 90) {
                                        lv = lv.plus("%")
                                        quoteReply = if (null != tags[tag] && tags[tag] != ""){
                                            quoteReply.plus("${tags[tag]}(${tag}):$lv\n")
                                        }else{
                                            quoteReply.plus("${tag}:$lv\n")
                                        }
                                    }
                                }}

                        }
                        if (it.contentEquals("Character Tags")){
                            if (tbody.size>=2){
                                val trs = tbody[1].select("tr")
                                //角色识别 character
                                quoteReply = quoteReply.plus("===角色识别===\n")
                                trs.forEach {tr ->
                                    println(tr)
                                    val td = tr.select("td")
                                    val tag = td[0].getElementsByTag("a").text()
                                    val score = td[1].text()

                                    var lv = format.format(score.toFloat()).replace("0.", "")
                                    if (lv.length < 2) {
                                        lv = lv.plus("0")
                                    }
                                    lv = lv.plus("%")
                                    quoteReply = if (null != tags[tag] && tags[tag] != ""){
                                        quoteReply.plus("${tags[tag]}(${tag}):$lv\n")
                                    }else{
                                        quoteReply.plus("${tag}:$lv\n")
                                    }
                                }
                            }

                        }
                        if (it.contentEquals("System Tags")){
                            if (tbody.size>=3){
                                val trs = tbody[2].select("tr")
                                //安全性 system
                                quoteReply = quoteReply.plus("===安全性===\n")
                                trs.forEach {tr ->
                                    val td = tr.select("td")
                                    val tag = td[0].getElementsByTag("a").text()
                                    val score = td[1].text()

                                    var lv = format.format(score.toFloat()).replace("0.", "")

                                    if (lv.length < 2) {
                                        lv = lv.plus("0")
                                    }

                                    lv = lv.plus("%")
                                    quoteReply = if (null != tags[tag] && tags[tag] != ""){
                                        quoteReply.plus("${tags[tag]}(${tag}):$lv\n")
                                    }else{
                                        quoteReply.plus("${tag}:$lv\n")
                                    }
                                }
                            }

                        }
                    }
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