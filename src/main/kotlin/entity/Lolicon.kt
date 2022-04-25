package com.hcyacg.entity
import com.alibaba.fastjson.annotation.JSONField


data class Lolicon(
    @JSONField(name = "data")
    val `data`: List<Data>? = listOf(),
    @JSONField(name = "error")
    val error: String? = ""
)

data class Data(
    @JSONField(name = "author")
    val author: String? = "",
    @JSONField(name = "ext")
    val ext: String? = "",
    @JSONField(name = "height")
    val height: Int? = 0,
    @JSONField(name = "p")
    val p: Int? = 0,
    @JSONField(name = "pid")
    val pid: Int? = 0,
    @JSONField(name = "r18")
    val r18: Boolean? = false,
    @JSONField(name = "tags")
    val tags: List<String>? = listOf(),
    @JSONField(name = "title")
    val title: String? = "",
    @JSONField(name = "uid")
    val uid: Int? = 0,
    @JSONField(name = "uploadDate")
    val uploadDate: Long? = 0,
    @JSONField(name = "urls")
    val urls: Urls? = Urls(),
    @JSONField(name = "width")
    val width: Int? = 0
)

data class Urls(
    @JSONField(name = "original")
    val original: String? = ""
)