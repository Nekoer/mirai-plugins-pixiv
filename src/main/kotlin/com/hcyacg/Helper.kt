package com.hcyacg

import com.hcyacg.Pixiv.save
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

/**
 * 帮助列表以及开关设置
 */
object Helper {

    suspend fun load(event: GroupMessageEvent) {
        val message = At(event.sender).plus("\n")
            .plus(" ·查看排行榜 ${Command.showRank}daily|weekly|monthly|rookie|original|male|female|daily_r18|weekly_r18|male_r18|female_r18|r18g-页码").plus("\n")
            .plus(" ·查看图片详情 ${Command.getDetailOfId}pixivId").plus("\n")
            .plus(" ·查找图片 ${Command.picToSearch}你要找的图片").plus("\n")
            .plus(" ·查看作者 ${Command.findUserWorksById}作者id").plus("\n")
            .plus(" ·查找番剧 ${Command.searchInfoByPic}番剧截图").plus("\n")
            .plus(" ·看涩图 ${Command.setu}").plus("\n")
            .plus(" ·看涩图 +tag ${Command.setu} loli").plus("\n")
            .plus(" ·看萝莉 ${Command.lolicon} 查询条件 r18,查询条件最多三个用&分割，|为或者,例：少女|姐姐&黑丝|白丝&猫耳|狗耳;如果有r18则必须放在最后，开启18禁模式").plus("\n")
            .plus(" ·通过tag查找排行榜 ${Command.tag}关键词-页码").plus("\n")
            .plus(" ·检测图片的涩情程度并打上标签 检测[图片]").plus("\n")
            .plus("===============").plus("\n")
            .plus("涩图开关").plus("\n")
            .plus(" ·pixiv:${Config.enable.sexy.pixiv}").plus("\n")
            .plus(" ·yande:${Config.enable.sexy.yande}").plus("\n")
            .plus(" ·konachan:${Config.enable.sexy.konachan}").plus("\n")
            .plus(" ·lolicon:${Config.enable.sexy.lolicon}").plus("\n")
            .plus(" ·local:${Config.enable.sexy.localImage} 本地图库只要撤回时长不是0,默认撤回").plus("\n")
            .plus("搜索引擎开关").plus("\n")
            .plus(" ·ascii2d:${Config.enable.search.ascii2d}").plus("\n")
            .plus(" ·google:${Config.enable.search.google}").plus("\n")
            .plus(" ·iqdb:${Config.enable.search.iqdb}").plus("\n")
            .plus(" ·saucenao:${Config.enable.search.saucenao}").plus("\n")
            .plus(" ·yandex:${Config.enable.search.yandex}").plus("\n")

            .plus("图片是否开启缓存:${Config.cache.enable}").plus("\n")
            .plus("缓存路径是否设置:${Config.cache.directory.isNotBlank()}").plus("\n")
            .plus("本地图库是否设置:${Config.localImagePath.isNotBlank()}").plus("\n")
            .plus("转发消息是否开启:${Config.forward.rankAndTagAndUserByForward}").plus("\n")
            .plus("图片转发是否开启:${Config.forward.imageToForward}").plus("\n")
            .plus("涩图晶格化是否开启:${Config.lowPoly}").plus("\n")
            .plus("===============").plus("\n")
            .plus("本群权限").plus("\n")
            .plus(" ·涩图:${Setting.groups.indexOf(event.group.id.toString()) > -1}").plus("\n")
            .plus(" ·撤回时长:${Config.recall}ms").plus("\n")
            .plus("===============").plus("\n")
            .plus("管理员命令").plus("\n")
            .plus(" ·切换缓存开关").plus("\n")
            .plus(" ·切换涩图开关").plus("\n")
            .plus(" ·切换转发开关").plus("\n")
            .plus(" ·切换图片转发开关").plus("\n")
            .plus(" ·切换晶格化开关").plus("\n")
            .plus(" ·(开启|关闭)(pixiv|yande|lolicon|local|konachan) 例: 开启pixiv").plus("\n")
            .plus(" ·(开启|关闭)(ascii2d|google|saucenao|yandex|iqdb) 例: 开启ascii2d").plus("\n")
        event.subject.sendMessage(message)
    }

    /**
     * 本群涩图权限设置
     */
    suspend fun setuEnable(event: GroupMessageEvent) {
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        if (Setting.groups.contains(event.group.id.toString())) {
            Setting.groups.remove("${event.group.id}")
        } else if (!Setting.groups.contains(event.group.id.toString())) {
            Setting.groups.add("${event.group.id}")
        }
        Setting.save()
        event.subject.sendMessage(
            At(event.sender).plus("\n").plus(
                "本群涩图已${
                    if (Setting.groups.contains(event.group.id.toString())) {
                        "开启"
                    } else {
                        "关闭"
                    }
                }"
            )
        )
    }


    /**
     * 本地缓存设置
     */
    suspend fun enableLocal(event: GroupMessageEvent) {
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }
        Config.cache.enable = !Config.cache.enable

        Config.save()
        event.subject.sendMessage(
            At(event.sender).plus("\n").plus(
                "缓存图片已${
                    if (Config.cache.enable) {
                        "开启"
                    } else {
                        "关闭"
                    }
                }"
            )
        )
    }


    /**
     * 转发消息开关
     */
    suspend fun enableForward(event: GroupMessageEvent) {
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        Config.forward.rankAndTagAndUserByForward = !Config.forward.rankAndTagAndUserByForward
        Config.save()
        event.subject.sendMessage(
            At(event.sender).plus("\n").plus(
                "转发消息${
                    if (Config.forward.rankAndTagAndUserByForward) {
                        "开启"
                    } else {
                        "关闭"
                    }
                }"
            )
        )

    }

    /**
     * 图片转发消息开关
     */
    suspend fun enableImageToForward(event: GroupMessageEvent) {
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        Config.forward.imageToForward = !Config.forward.imageToForward
        Config.save()
        event.subject.sendMessage(
            At(event.sender).plus("\n").plus(
                "图片转发消息${
                    if (Config.forward.imageToForward) {
                        "开启"
                    } else {
                        "关闭"
                    }
                }"
            )
        )

    }

    /**
     * 晶格化涩图开关
     */
    suspend fun enableLowPoly(event: GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        Config.lowPoly = !Config.lowPoly
        Config.save()
        event.subject.sendMessage(
            At(event.sender).plus("\n").plus(
                "涩图晶格化${
                    if (Config.lowPoly) {
                        "开启"
                    } else {
                        "关闭"
                    }
                }"
            )
        )
    }


    /**
     * 涩图库开关
     */
    suspend fun enableSetu(event: GroupMessageEvent) {
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        var message = event.message.contentToString()
        val state = message.contains("开启")
        message = message.replace("开启", "").replace("关闭", "")
        event.subject.sendMessage(changeSetu(state, message))
    }

    /**
     * 根据状态来设置涩图库开关
     */
    private fun changeSetu(state: Boolean, key: String): Message {
        when (key) {
            "pixiv" -> {
                Config.enable.sexy.pixiv = state
            }
            "yande" -> {
                Config.enable.sexy.yande = state
            }
            "local" -> {
                Config.enable.sexy.localImage = state
            }
            "lolicon" -> {
                Config.enable.sexy.lolicon = state
            }
            "konachan" -> {
                Config.enable.sexy.konachan = state
            }
        }

        Config.save()
        return PlainText("开关已切换").plus("\n")
            .plus("pixiv:${Config.enable.sexy.pixiv}").plus("\n")
            .plus("yande:${Config.enable.sexy.yande}").plus("\n")
            .plus("local:${Config.enable.sexy.localImage}").plus("\n")
            .plus("lolicon:${Config.enable.sexy.lolicon}").plus("\n")
            .plus("konachan:${Config.enable.sexy.konachan}")
    }


    /**
     * 搜索引擎开关
     */
    suspend fun enableSearch(event: GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())) {
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        var message = event.message.contentToString()
        val state = message.contains("开启")
        message = message.replace("开启", "").replace("关闭", "")
        event.subject.sendMessage(changeSearch(state, message))
    }

    /**
     * 修改各个搜索引擎的开关
     */
    private fun changeSearch(state: Boolean, key: String): Message {
        when (key) {
            "google" -> {
                Config.enable.search.google = state
            }
            "ascii2d" -> {
                Config.enable.search.ascii2d = state
            }
            "iqdb" -> {
                Config.enable.search.iqdb = state
            }
            "saucenao" -> {
                Config.enable.search.saucenao = state
            }
            "yandex" -> {
                Config.enable.search.yandex = state
            }
        }

        Config.save()
        return PlainText("开关已切换").plus("\n")
            .plus("ascii2d:${Config.enable.search.ascii2d}").plus("\n")
            .plus("google:${Config.enable.search.google}").plus("\n")
            .plus("iqdb:${Config.enable.search.iqdb}").plus("\n")
            .plus("saucenao:${Config.enable.search.saucenao}").plus("\n")
            .plus("yandex:${Config.enable.search.yandex}")
    }
}