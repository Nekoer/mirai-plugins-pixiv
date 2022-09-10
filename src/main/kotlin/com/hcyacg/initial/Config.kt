package com.hcyacg.initial
import com.hcyacg.anno.NoArgOpenDataClass
import com.hcyacg.initial.entity.Cache
import com.hcyacg.initial.entity.Enable
import com.hcyacg.initial.entity.ForWard
import com.hcyacg.initial.entity.GoogleConfig
import kotlinx.serialization.*
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value
import java.io.File


object Config : AutoSavePluginConfig("Config") {
    @ValueName("enable")
    @ValueDescription("开关 search:搜索图片的引擎开关 sexy:涩图库来源开关")
    var enable: Enable by value()

    @ValueName("forward")
    @ValueDescription("转发模式 rankAndTagAndUserByForward:排行榜、标签、作者三个变成转发模式; imageToForward:查看图片详情变成转发模式 可发送该作品的所有图片")
    val forward: ForWard by value()

    @ValueName("token")
    @ValueDescription("Token必填项 https://www.acgmx.com/ https://saucenao.com/user.php")
    var token: Token by value()

    @ValueName("proxy")
    @ValueDescription("代理 默认clash 127.0.0.1:7890 不知道的话window 搜索代理设置")
    var proxy: Proxy by value()

    @ValueName("recall")
    @ValueDescription("自动撤回 涩图专用 单位ms")
    var recall: Long by value(5000L)

    @ValueName("tlsVersion")
    var tlsVersion: String by value("TLSv1.2")

    @ValueName("cache")
    @ValueDescription("缓存 图片缓存将按照来源分开存放")
    var cache: Cache by value()

    @ValueName("localImagePath")
    @ValueDescription("本地图库目录")
    val localImagePath: String by value(System.getProperty("user.dir") + File.separator + "image")

    @ValueName("google")
    @ValueDescription("Google搜索配置项 镜像源和搜索显示的数量")
    val googleConfig: GoogleConfig by value()

    @ValueName("lowPoly")
    @ValueDescription("涩图晶格化开关")
    var lowPoly: Boolean by value(false)

    @ValueName("loliconSize")
    @ValueDescription("lolicon图大小:original,regular,small,thumb,mini")
    var loliconSize:String by value("original")
}

@NoArgOpenDataClass
@Serializable
data class Proxy(
    @SerialName("host")
    var host: String = "",
    @SerialName("port")
    var port: Int = -1
)

@NoArgOpenDataClass
@Serializable
data class Token(
    @SerialName("acgmx")
    var acgmx: String = "",
    @SerialName("saucenao")
    var saucenao: String = ""
)