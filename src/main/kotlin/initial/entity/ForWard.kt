package com.hcyacg.initial.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForWard(
    @SerialName("rankAndTagAndUserByForward")
    var rankAndTagAndUserByForward:Boolean = false,
    @SerialName("imageToForward")
    var imageToForward:Boolean = false
)