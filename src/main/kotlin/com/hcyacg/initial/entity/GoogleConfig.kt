package com.hcyacg.initial.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleConfig(
    @SerialName("googleUrl")
    val googleUrl:String = "https://www.google.com.hk",
    @SerialName("resultNum")
    val resultNum:Int = 6
)