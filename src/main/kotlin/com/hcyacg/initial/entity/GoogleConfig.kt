package com.hcyacg.initial.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleConfig(
    @SerialName("googleImageUrl")
    val googleImageUrl:String = "https://images.google.com",
    @SerialName("resultNum")
    val resultNum:Int = 2
)