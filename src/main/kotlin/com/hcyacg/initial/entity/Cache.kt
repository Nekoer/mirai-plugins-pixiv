package com.hcyacg.initial.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Cache(
    @SerialName("enable")
    var enable: Boolean = true,
    @SerialName("directory")
    val directory: String = System.getProperty("user.dir") + File.separator + "image"
)