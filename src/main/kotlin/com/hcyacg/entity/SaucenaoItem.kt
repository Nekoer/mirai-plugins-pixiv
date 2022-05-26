package com.hcyacg.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

@Serializable
data class SaucenaoItem(
    @SerialName("header")
    val header: Header? = Header(),
    @SerialName("results")
    val results: List<SResult>? = listOf()
)

@Serializable
data class Header(
    @SerialName("account_type")
    val accountType: String? = "",
    @SerialName("index")
    val index: Index? = Index(),
    @SerialName("long_limit")
    val longLimit: String? = "",
    @SerialName("long_remaining")
    val longRemaining: Int? = 0,
    @SerialName("minimum_similarity")
    val minimumSimilarity: Double? = 0.0,
    @SerialName("query_image")
    val queryImage: String? = "",
    @SerialName("query_image_display")
    val queryImageDisplay: String? = "",
    @SerialName("results_requested")
    val resultsRequested: Int? = 0,
    @SerialName("results_returned")
    val resultsReturned: Int? = 0,
    @SerialName("search_depth")
    val searchDepth: String? = "",
    @SerialName("short_limit")
    val shortLimit: String? = "",
    @SerialName("short_remaining")
    val shortRemaining: Int? = 0,
    @SerialName("status")
    val status: Int? = 0,
    @SerialName("user_id")
    val userId: String? = ""
)

@Serializable
data class SResult(
    @SerialName("data")
    val `data`: Data,
    @SerialName("header")
    val header: HeaderX? = HeaderX()
)

@Serializable
data class Index(
    @SerialName("0")
    val x0: X0? = X0(),
    @SerialName("10")
    val x10: X10? = X10(),
    @SerialName("11")
    val x11: X11? = X11(),
    @SerialName("12")
    val x12: X12? = X12(),
    @SerialName("16")
    val x16: X16? = X16(),
    @SerialName("18")
    val x18: X18? = X18(),
    @SerialName("19")
    val x19: X19? = X19(),
    @SerialName("2")
    val x2: X2? = X2(),
    @SerialName("20")
    val x20: X20? = X20(),
    @SerialName("21")
    val x21: X21? = X21(),
    @SerialName("211")
    val x211: X211? = X211(),
    @SerialName("22")
    val x22: X22? = X22(),
    @SerialName("23")
    val x23: X23? = X23(),
    @SerialName("24")
    val x24: X24? = X24(),
    @SerialName("25")
    val x25: X25? = X25(),
    @SerialName("26")
    val x26: X26? = X26(),
    @SerialName("27")
    val x27: X27? = X27(),
    @SerialName("28")
    val x28: X28? = X28(),
    @SerialName("29")
    val x29: X29? = X29(),
    @SerialName("30")
    val x30: X30? = X30(),
    @SerialName("31")
    val x31: X31? = X31(),
    @SerialName("32")
    val x32: X32? = X32(),
    @SerialName("33")
    val x33: X33? = X33(),
    @SerialName("34")
    val x34: X34? = X34(),
    @SerialName("341")
    val x341: X341? = X341(),
    @SerialName("35")
    val x35: X35? = X35(),
    @SerialName("36")
    val x36: X36? = X36(),
    @SerialName("37")
    val x37: X37? = X37(),
    @SerialName("371")
    val x371: X371? = X371(),
    @SerialName("38")
    val x38: X38? = X38(),
    @SerialName("39")
    val x39: X39? = X39(),
    @SerialName("40")
    val x40: X40? = X40(),
    @SerialName("41")
    val x41: X41? = X41(),
    @SerialName("42")
    val x42: X42? = X42(),
    @SerialName("43")
    val x43: X43? = X43(),
    @SerialName("44")
    val x44: X44? = X44(),
    @SerialName("5")
    val x5: X5? = X5(),
    @SerialName("51")
    val x51: X51? = X51(),
    @SerialName("52")
    val x52: X52? = X52(),
    @SerialName("53")
    val x53: X53? = X53(),
    @SerialName("6")
    val x6: X6? = X6(),
    @SerialName("8")
    val x8: X8? = X8(),
    @SerialName("9")
    val x9: X9? = X9()
)

@Serializable
data class X0(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X10(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X11(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X12(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X16(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X18(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X19(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X2(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X20(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X21(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X211(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X22(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X23(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X24(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X25(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X26(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X27(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X28(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X29(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X30(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X31(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X32(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X33(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X34(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X341(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X35(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X36(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X37(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X371(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X38(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X39(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X40(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X41(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X42(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X43(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X44(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X5(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X51(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X52(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X53(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X6(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X8(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

@Serializable
data class X9(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("parent_id")
    val parentId: Int? = 0,
    @SerialName("results")
    val results: Int? = 0,
    @SerialName("status")
    val status: Int? = 0
)

object CreatorSerializer : JsonTransformingSerializer<String>(String.serializer()) {
    // If response is an array, then return a empty Icon
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonArray || element is List<*>) {
            var name = ""
            for (index in 0 until element.jsonArray.size) {
                name = if (index == element.jsonArray.size - 1) {
                    name.plus(element.jsonArray[index].jsonPrimitive.content)
                } else {
                    name.plus(element.jsonArray[index].jsonPrimitive.content).plus("和")
                }
            }
            Json.encodeToJsonElement(name)
        } else {
            element
        }
    }
}


@Serializable
data class Data(
    @SerialName("anime-pictures_id")
    val animePicturesId: Int? = 0,
    @SerialName("artist")
    val artist: String? = "",
    @SerialName("author")
    val author: String? = "",
    @SerialName("author_name")
    val authorName: String? = "",
    @SerialName("author_url")
    val authorUrl: String? = "",
    @SerialName("bcy_id")
    val bcyId: Int? = 0,
    @SerialName("bcy_type")
    val bcyType: String? = "",
    @SerialName("characters")
    val characters: String? = "",
    @SerialName("created_at")
    val createdAt: String? = "",
    @Serializable(with = CreatorSerializer::class)
    @SerialName("creator")
    val creator: String? = "",
    @SerialName("creator_name")
    val creatorName: String? = "",
    @SerialName("da_id")
    val daId: String? = "",
    @SerialName("danbooru_id")
    val danbooruId: Int? = 0,
    @SerialName("eng_name")
    val engName: String? = "",
    @SerialName("ext_urls")
    val extUrls: List<String>? = listOf(),
    @SerialName("gelbooru_id")
    val gelbooruId: Int? = 0,
    @SerialName("jp_name")
    val jpName: String? = "",
    @SerialName("mal_id")
    val malId: Int? = 0,
    @SerialName("material")
    val material: String? = "",
    @SerialName("md_id")
    val mdId: String? = "",
    @SerialName("member_id")
    val memberId: Int? = 0,
    @SerialName("member_link_id")
    val memberLinkId: Int? = 0,
    @SerialName("member_name")
    val memberName: String? = "",
    @SerialName("mu_id")
    val muId: Int? = 0,
    @SerialName("part")
    val part: String? = "",
    @SerialName("path")
    val path: String? = "",
    @SerialName("pawoo_id")
    val pawooId: Int? = 0,
    @SerialName("pawoo_user_acct")
    val pawooUserAcct: String? = "",
    @SerialName("pawoo_user_display_name")
    val pawooUserDisplayName: String? = "",
    @SerialName("pawoo_user_username")
    val pawooUserUsername: String? = "",
    @SerialName("pixiv_id")
    val pixivId: Int? = 0,
    @SerialName("seiga_id")
    val seigaId: Int? = 0,
    @SerialName("source")
    val source: String? = "",
    @SerialName("title")
    val title: String? = "",
    @SerialName("type")
    val type: String? = "",
    @SerialName("yandere_id")
    val yandereId: Int? = 0,
    @SerialName("nijie_id")
    val nijieId: Int? = 0,
    @SerialName("drawr_id")
    val drawrId: Int? = 0
)

@Serializable
data class HeaderX(
    @SerialName("dupes")
    val dupes: Int? = 0,
    @SerialName("hidden")
    val hidden: Int? = 0,
    @SerialName("index_id")
    val indexId: Int? = 0,
    @SerialName("index_name")
    val indexName: String? = "",
    @SerialName("similarity")
    val similarity: String? = "",
    @SerialName("thumbnail")
    val thumbnail: String? = ""
)