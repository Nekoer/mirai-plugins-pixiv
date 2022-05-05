package com.hcyacg.entity

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class Anilist(
    @SerialName("data")
    val `data`: AData? = AData()
)

@Serializable
data class AData(
    @SerialName("Media")
    val media: Media? = Media()
)

@Serializable
data class Media(
    @SerialName("coverImage")
    val coverImage: CoverImage? = CoverImage(),
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("title")
    val title: Title? = Title()
)

@Serializable
data class CoverImage(
    @SerialName("extraLarge")
    val extraLarge: String? = ""
)

@Serializable
data class Title(
    @SerialName("native")
    val native: String? = ""
)