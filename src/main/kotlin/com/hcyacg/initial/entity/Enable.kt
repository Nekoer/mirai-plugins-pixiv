package com.hcyacg.initial.entity

import com.hcyacg.anno.NoArgOpenDataClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Enable(
    @SerialName("search")
    var search :Search = Search(),
    @SerialName("sexy")
    var sexy :Sexy = Sexy()
)

@NoArgOpenDataClass
@Serializable
data class Search(
    @SerialName("google")
    var google: Boolean = true,
    @SerialName("ascii2d")
    var ascii2d: Boolean = true,
    @SerialName("iqdb")
    var iqdb: Boolean = true,
    @SerialName("saucenao")
    var saucenao: Boolean = true,
    @SerialName("yandex")
    var yandex: Boolean = true
)

@NoArgOpenDataClass
@Serializable
data class Sexy(
    @SerialName("pixiv")
    var pixiv :Boolean = true,
    @SerialName("yande")
    var yande :Boolean = true,
    @SerialName("konachan")
    var konachan :Boolean = true,
    @SerialName("lolicon")
    var lolicon :Boolean = true,
    @SerialName("localImage")
    var localImage :Boolean = true
)