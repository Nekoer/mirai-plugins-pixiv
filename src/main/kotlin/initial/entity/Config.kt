package com.hcyacg.initial.entity
import com.hcyacg.anno.NoArgOpenDataClass
import kotlinx.serialization.*
import net.mamoe.mirai.console.data.ValueDescription
import java.io.File

@NoArgOpenDataClass
@Serializable
data class Config(
    @SerialName("setuEnable")
    var setuEnable: Enable = Enable(),
    @SerialName("forward")
    val forward: ForWard = ForWard(),
    @SerialName("token")
    var token: Token = Token(),
    @SerialName("proxy")
    var proxy: Proxy = Proxy(),
    @SerialName("recall")
    var recall: Long = 5000,
    @SerialName("tlsVersion")
    var tlsVersion: String = "TLSv1.2",
    @SerialName("cache")
    var cache: Cache = Cache(),
    @SerialName("localImagePath")
    val localImagePath:String = System.getProperty("user.dir") + File.separator + "image",
    @SerialName("google")
    val googleConfig: GoogleConfig = GoogleConfig()
)

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