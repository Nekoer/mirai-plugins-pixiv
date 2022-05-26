package com.hcyacg.initial.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Enable(
    @SerialName("pixiv")
    var pixiv :Boolean = true,
    @SerialName("yande")
    var yande :Boolean = true,
    @SerialName("konachan")
    var konachan :Boolean = true,
    @SerialName("lolicon")
    var lolicon :Boolean = true,
    @SerialName("localImage")
    var localImage :Boolean = true,
)