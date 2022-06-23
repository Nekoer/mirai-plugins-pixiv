package com.hcyacg.initial
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

/**
 * 自定义触发命令
 */
object Command: AutoSavePluginConfig("Command") {
    @ValueName("getDetailOfId")
    @ValueDescription("根据id获得图片及其数据 psid-87984524")
    var getDetailOfId: String by value("psid-")

    @ValueName("picToSearch")
    @ValueDescription("搜索图片 ptst-图片")
    var picToSearch: String by value("ptst-")


    @ValueName("showRank")
    @ValueDescription("排行榜 rank-daily-页码 可选daily|weekly|monthly|rookie|original|male|female|daily_r18|weekly_r18|male_r18|female_r18|r18g")
    var showRank: String by value("rank-")

    @ValueName("findUserWorksById")
    @ValueDescription("获取作者所有的插画 user-87915-页码")
    var findUserWorksById: String by value("user-")

    @ValueName("searchInfoByPic")
    @ValueDescription("搜索番剧 ptsf-图片")
    var searchInfoByPic: String by value("ptsf-")

    @ValueName("setu")
    @ValueDescription("涩图 setu 或者setu loli")
    var setu: String by value("来点色图")

    @ValueName("lolicon")
    @ValueDescription("Lolicon 详细看https://api.lolicon.app/#/setu?id=tag")
    var lolicon: String by value("loli")

    @ValueName("tag")
    @ValueDescription("搜索标签排行榜 tag-萝莉-页码")
    var tag: String by value("tag-")

    @ValueName("help")
    @ValueDescription("帮助")
    var help: String by value("帮助")

    @ValueName("lowPoly")
    @ValueDescription("晶格化命令")
    var lowPoly: String by value("晶格化")
}