package com.hcyacg.science

import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object Style2paints {

    private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
        .readTimeout(60000, TimeUnit.MILLISECONDS)
    private val logger = MiraiLogger.Factory.create(this::class.java)

    suspend fun coloring(event: GroupMessageEvent) {
        /**
         * 获取图片的代码
         */
        val url = DataUtil.getImageLink(event.message) ?: return

        val base64 = Base64.getEncoder().encodeToString(ImageUtil.getImage(url, CacheUtil.Type.NONSUPPORT).toByteArray())


        var requestBody: RequestBody = FormBody.Builder()
            .add("room", "new")
//            .add("step", "new")
            .add("sketch", "data:image/png;base64,$base64")
//            .add("method", "colorization")
            .build()
        val sketchRes: Response =
            client.build().newCall(Request.Builder().url("http://127.0.0.1:8233/upload_sketch").post(requestBody).build())
                .execute()

        val sketch = sketchRes.body?.string().toString().split("_")
        sketchRes.close()

        val room = sketch[0]
        val time = sketch[1]

        /**
         * room: H14M18S59
        points: []
        face: null
        faceID: 65563
        need_render: 0
        skipper: null
        inv4: 1
        r: 0.99
        g: 0.83
        b: 0.66
        h: 0.16666666666666666
        d: 0
         */
        val num = 35 .. 66


        //http://localhost/request_result
        requestBody = FormBody.Builder()
            .add("room", room)
            .add("points", "[]")
            .add("face", "null")
            .add("faceID", "655${num.random()}")
            .add("need_render", "0")
            .add("skipper", "null")
            .add("inv4", "1")
            .add("r", "0.99")
            .add("g", "0.83")
            .add("b", "0.66")
            .add("h", "0.16666666666666666")
            .add("d", "0")
            .build()
        val resultRes: Response =
            client.build().newCall(Request.Builder().url("http://127.0.0.1:8233/request_result").post(requestBody).build())
                .execute()
        val result = resultRes.body?.string().toString().split("_")[1]
        resultRes.close()
        val file =

            File("E:${File.separator}Desktop${File.separator}style2paints45beta1214B${File.separator}assets${File.separator}game${File.separator}rooms${File.separator}${room}${File.separator}${result}.blended_smoothed_careful.png")
//E:\Desktop\style2paints45beta1214B\assets\game\rooms\H14M28S44\H14M28S46.blended_smoothed_careful.png
        var t: Int = 0;

        while (true) {
            t++
            if (file.exists()) {
                delay(1000)
                val image = file.toExternalResource()
                val imageId: String = image.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    image.close()
                }
                event.subject.sendMessage(Image(imageId))
                break
            }

            if (t == 60) {
                break
            }
            delay(1000)
        }
    }

}