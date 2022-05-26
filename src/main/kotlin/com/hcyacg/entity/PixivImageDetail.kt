package com.hcyacg.entity

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class PixivImageDetail(
    @SerialName("caption")
    val caption: String? = "",
    @SerialName("create_date")
    val createDate: String? = "",
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("image_urls")
    val imageUrls: ImageUrls? = ImageUrls(),
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean? = false,
    @SerialName("is_muted")
    val isMuted: Boolean? = false,
    @SerialName("meta_pages")
    val metaPages: List<MetaPage>? = listOf(),
    @SerialName("meta_single_page")
    val metaSinglePage: MetaSinglePage? = MetaSinglePage(),
    @SerialName("page_count")
    val pageCount: Int? = 0,
    @SerialName("restrict")
    val restrict: Int? = 0,
    @SerialName("sanity_level")
    val sanityLevel: Int? = 0,
//    @SerialName("series")
//    val series: Any? = Any(),
    @SerialName("tags")
    val tags: List<Tag>? = listOf(),
    @SerialName("title")
    val title: String? = "",
    @SerialName("tools")
    val tools: List<String>? = listOf(),
    @SerialName("total_bookmarks")
    val totalBookmarks: Int? = 0,
    @SerialName("total_view")
    val totalView: Int? = 0,
    @SerialName("type")
    val type: String? = "",
    @SerialName("user")
    val user: User? = User(),
    @SerialName("visible")
    val visible: Boolean? = false,
    @SerialName("width")
    val width: Int? = 0,
    @SerialName("x_restrict")
    val xRestrict: Int? = 0
)

@Serializable
data class ImageUrls(
    @SerialName("large")
    val large: String? = "",
    @SerialName("medium")
    val medium: String? = "",
    @SerialName("square_medium")
    val squareMedium: String? = ""
)

@Serializable
data class MetaPage(
    @SerialName("image_urls")
    val imageUrls: ImageUrlsX? = ImageUrlsX()
)

@Serializable
data class MetaSinglePage(
    @SerialName("original_image_url")
    val originalImageUrl: String? = ""
)

@Serializable
data class Tag(
    @SerialName("name")
    val name: String? = "",
    @SerialName("translated_name")
    val translatedName: String? = ""
)

@Serializable
data class User(
    @SerialName("account")
    val account: String? = "",
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("is_followed")
    val isFollowed: Boolean? = false,
    @SerialName("name")
    val name: String? = "",
    @SerialName("profile_image_urls")
    val profileImageUrls: ProfileImageUrls? = ProfileImageUrls()
)

@Serializable
data class ImageUrlsX(
    @SerialName("large")
    val large: String? = "",
    @SerialName("medium")
    val medium: String? = "",
    @SerialName("original")
    val original: String? = "",
    @SerialName("square_medium")
    val squareMedium: String? = ""
)

@Serializable
data class ProfileImageUrls(
    @SerialName("medium")
    val medium: String? = ""
)
//
//object MetaPagesSerializer : JsonTransformingSerializer<List<>>(List.serializer()) {
//    // If response is an array, then return a empty Icon
//    override fun transformDeserialize(element: JsonElement): JsonElement {
//        return if (element is JsonArray || element is List<*>) {
//            var name = ""
//            for (index in 0 until element.jsonArray.size) {
//                name = if (index == element.jsonArray.size - 1) {
//                    name.plus(element.jsonArray[index].jsonPrimitive.content)
//                } else {
//                    name.plus(element.jsonArray[index].jsonPrimitive.content).plus("和")
//                }
//            }
//            Json.encodeToJsonElement(name)
//        } else {
//            element
//        }
//    }
//}
