package com.hcyacg.entity
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class YandexSearchResult(
    @SerialName("counterPaths")
    val counterPaths: CounterPaths? = CounterPaths(),
    @SerialName("faviconSpriteSeed")
    val faviconSpriteSeed: String? = "",
    @SerialName("lazyThumbsFromIndex")
    val lazyThumbsFromIndex: Int? = 0,
    @SerialName("loadedPagesCount")
    val loadedPagesCount: Int? = 0,
    @SerialName("pageSize")
    val pageSize: Int? = 0,
    @SerialName("sites")
    val sites: List<Site>? = listOf(),
    @SerialName("title")
    val title: String? = "",
    @SerialName("withFavicon")
    val withFavicon: Boolean? = false
)

@Serializable
data class CounterPaths(
    @SerialName("item")
    val item: String? = "",
    @SerialName("itemDomainClick")
    val itemDomainClick: String? = "",
    @SerialName("itemThumbClick")
    val itemThumbClick: String? = "",
    @SerialName("itemTitleClick")
    val itemTitleClick: String? = "",
    @SerialName("loadPage")
    val loadPage: String? = ""
)

@Serializable
data class Site(
    @SerialName("description")
    val description: String? = "",
    @SerialName("domain")
    val domain: String? = "",
    @SerialName("originalImage")
    val originalImage: OriginalImage? = OriginalImage(),
    @SerialName("thumb")
    val thumb: Thumb? = Thumb(),
    @SerialName("title")
    val title: String? = "",
    @SerialName("url")
    val url: String? = ""
)

@Serializable
data class OriginalImage(
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("url")
    val url: String? = "",
    @SerialName("width")
    val width: Int? = 0
)

@Serializable
data class Thumb(
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("url")
    val url: String? = "",
    @SerialName("width")
    val width: Int? = 0
)