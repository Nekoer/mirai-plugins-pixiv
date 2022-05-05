package com.hcyacg.entity
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class Trace(
    @SerialName("error")
    val error: String? = "",
    @SerialName("frameCount")
    val frameCount: Int? = 0,
    @SerialName("result")
    val result: List<Result>? = listOf()
)

@Serializable
data class Result(
    @SerialName("anilist")
    val anilist: Int? = 0,
    @SerialName("episode")
    val episode: Int? = 0,
    @SerialName("filename")
    val filename: String? = "",
    @SerialName("from")
    val from: Double? = 0.0,
    @SerialName("image")
    val image: String? = "",
    @SerialName("similarity")
    val similarity: Double? = 0.0,
    @SerialName("to")
    val to: Double? = 0.0,
    @SerialName("video")
    val video: String? = ""
)