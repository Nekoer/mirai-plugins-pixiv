package com.hcyacg.entity
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class YandexImage(
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("image_id")
    val imageId: String? = "",
    @SerialName("image_shard")
    val imageShard: Int? = 0,
    @SerialName("namespace")
    val namespace: String? = "",
    @SerialName("url")
    val url: String? = "",
    @SerialName("width")
    val width: Int? = 0
)