package com.hcyacg.initial.entity
import com.hcyacg.anno.NoArgOpenDataClass
import kotlinx.serialization.*

@NoArgOpenDataClass
@Serializable
data class Config(
    @SerialName("token")
    var token: Token = Token(),
    @SerialName("proxy")
    var proxy: Proxy = Proxy(),
    @SerialName("recall")
    var recall: Long = 5000,
    @SerialName("tlsVersion")
    var tlsVersion: String = "TLSv1.2"
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