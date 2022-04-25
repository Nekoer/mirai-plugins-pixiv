package com.hcyacg

import com.hcyacg.Pixiv.save
import com.hcyacg.initial.Setting
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object Helper {

    suspend fun load(event:GroupMessageEvent){
        val message = At(event.sender).plus("\n")
            .plus("查看排行榜 ${Setting.command.showRank}day|week|month-页码").plus("\n")
            .plus("查看图片详情 ${Setting.command.getDetailOfId}pixivId").plus("\n")
            .plus("查找图片 ${Setting.command.picToSearch}你要找的图片").plus("\n")
            .plus("查看作者 ${Setting.command.findUserWorksById}作者id").plus("\n")
            .plus("查找番剧 ${Setting.command.searchInfoByPic}番剧截图").plus("\n")
            .plus("看涩图 ${Setting.command.setu}").plus("\n")
            .plus("看萝莉 ${Setting.command.lolicon} 查询条件 r18,查询条件最多三个,例：少女|黑丝|猫耳;如果有r18则必须放在最后，开启18禁模式").plus("\n")
            .plus("通过tag查找排行榜 ${Setting.command.tag}关键词-页码").plus("\n")
            .plus("===============").plus("\n")
            .plus("涩图开关").plus("\n")
            .plus(" ·pixiv:${Setting.config.setuEnable.pixiv}").plus("\n")
            .plus(" ·yande:${Setting.config.setuEnable.yande}").plus("\n")
            .plus(" ·konachan:${Setting.config.setuEnable.konachan}").plus("\n")
            .plus(" ·lolicon:${Setting.config.setuEnable.lolicon}").plus("\n")
            .plus(" ·local:${Setting.config.setuEnable.localImage} 本地图库只要撤回时长不是0,默认撤回").plus("\n")
            .plus("图片是否开启缓存:${Setting.config.cache.enable}").plus("\n")
            .plus("缓存路径是否设置:${Setting.config.cache.directory.isNotBlank()}").plus("\n")
            .plus("本地图库是否设置:${Setting.config.localImagePath.isNotBlank()}").plus("\n")
            .plus("===============").plus("\n")
            .plus("本群权限").plus("\n")
            .plus(" ·涩图:${Setting.groups.indexOf(event.group.id.toString()) > -1}").plus("\n")
            .plus(" ·撤回时长:${Setting.config.recall}ms").plus("\n")
            .plus("===============").plus("\n")
            .plus("管理员命令").plus("\n")
            .plus(" ·切换缓存开关").plus("\n")
            .plus(" ·切换涩图开关").plus("\n")
            .plus(" ·(开启|关闭)(pixiv|yande|lolicon|local|konachan) 例: 开启pixiv").plus("\n")
        event.subject.sendMessage(message)
    }

    suspend fun setuEnable(event:GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())){
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        if (Setting.groups.contains(event.group.id.toString())){
            Setting.groups.remove("${event.group.id}")
        }else if (!Setting.groups.contains(event.group.id.toString())){
            Setting.groups.add("${event.group.id}")
        }
        Setting.save()
        event.subject.sendMessage(At(event.sender).plus("\n").plus("本群涩图已${if (Setting.groups.contains(event.group.id.toString())){"开启"}else{"关闭"}}"))
    }


    suspend fun enableLocal(event:GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())){
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }
        Setting.config.cache.enable = !Setting.config.cache.enable

        Setting.save()
        event.subject.sendMessage(At(event.sender).plus("\n").plus("缓存图片已${if (Setting.config.cache.enable){"开启"}else{"关闭"}}"))
    }

    suspend fun enableSetu(event:GroupMessageEvent){
        if (!Setting.admins.contains(event.sender.id.toString())){
            event.subject.sendMessage(At(event.sender).plus("\n").plus("您没有权限设置"))
            return
        }

        var message = event.message.contentToString()
        val state = message.contains("开启")
        println(state)
        message = message.replace("开启","").replace("关闭","")
        event.subject.sendMessage(changeSetu(state,message))
    }

    private fun changeSetu(state:Boolean,key:String):Message{
        when (key){
            "pixiv" -> {
                Setting.config.setuEnable.pixiv = state
            }
            "yande" -> {
                Setting.config.setuEnable.yande = state
            }
            "local" -> {
                Setting.config.setuEnable.localImage = state
            }
            "lolicon" -> {
                Setting.config.setuEnable.lolicon = state
            }
            "konachan" -> {
                Setting.config.setuEnable.konachan = state
            }
        }

        Setting.save()
        return PlainText("开关已切换").plus("\n")
            .plus("pixiv:${Setting.config.setuEnable.pixiv}").plus("\n")
            .plus("yande:${Setting.config.setuEnable.yande}").plus("\n")
            .plus("local:${Setting.config.setuEnable.localImage}").plus("\n")
            .plus("lolicon:${Setting.config.setuEnable.lolicon}").plus("\n")
            .plus("konachan:${Setting.config.setuEnable.konachan}")
    }
}