package com.hcyacg.initial.entity
import com.hcyacg.anno.NoArgOpenDataClass
import kotlinx.serialization.*

@NoArgOpenDataClass
@Serializable
data class Command(
    @SerialName("getDetailOfId")
    var getDetailOfId: String = "psid-",
    @SerialName("picToSearch")
    var picToSearch: String = "ptst-",
    @SerialName("showRank")
    var showRank: String = "rank-",
    @SerialName("rankAndTagAndUserByForward")
    val rankAndTagAndUserByForward:Boolean = false,
    @SerialName("findUserWorksById")
    var findUserWorksById: String = "user-",
    @SerialName("searchInfoByPic")
    var searchInfoByPic: String = "ptsf-",
    @SerialName("setu")
    var setu: String =  "来点色图",
    @SerialName("lolicon")
    var lolicon: String =  "loli",
    @SerialName("tag")
    var tag: String = "tag-",
    @SerialName("help")
    var help: String = "帮助"
)