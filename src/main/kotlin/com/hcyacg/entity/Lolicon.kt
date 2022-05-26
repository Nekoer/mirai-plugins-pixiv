package com.hcyacg.entity
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class Lolicon(
    @SerialName("data")
    val `data`: List<LData>? = listOf(),
    @SerialName("error")
    val error: String? = ""
)

@Serializable
data class LData(
    @SerialName("author")
    val author: String? = "",
    @SerialName("ext")
    val ext: String? = "",
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("p")
    val p: Int? = 0,
    @SerialName("pid")
    val pid: Int? = 0,
    @SerialName("r18")
    val r18: Boolean? = false,
    @SerialName("tags")
    val tags: List<String>? = listOf(),
    @SerialName("title")
    val title: String? = "",
    @SerialName("uid")
    val uid: Int? = 0,
    @SerialName("uploadDate")
    val uploadDate: Long? = 0,
    @SerialName("urls")
    val urls: Urls? = Urls(),
    @SerialName("width")
    val width: Int? = 0
)

@Serializable
data class Urls(
    @SerialName("original")
    val original: String? = ""
)