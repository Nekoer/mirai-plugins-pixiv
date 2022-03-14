package com.hcyacg

import com.hcyacg.anime.Agefans
import com.hcyacg.config.Config
import com.hcyacg.details.PicDetails
import com.hcyacg.details.UserDetails
import com.hcyacg.initial.Configuration.Companion.init
import com.hcyacg.initial.Setting
import com.hcyacg.rank.Rank
import com.hcyacg.rank.Tag
import com.hcyacg.science.Style2paints
import com.hcyacg.search.Ascii2d
import com.hcyacg.search.Saucenao
import com.hcyacg.search.SearchPicCenter
import com.hcyacg.search.Trace
import com.hcyacg.sexy.SexyCenter
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import net.mamoe.mirai.utils.MiraiLoggerWithSwitch
import java.util.concurrent.LinkedBlockingQueue
import java.util.regex.Pattern

object Pixiv : KotlinPlugin(
    JvmPluginDescription(
        id = "com.hcyacg.pixiv",
        name = "pixiv插画",
        version = "1.6.3",
    ) {
        author("Nekoer")
        info("""pixiv插画""")
    }
) {
    private val pluginLogger = MiraiLogger.Factory.create(this::class.java)
    override fun onEnable() {
        Setting.reload()
        Setting.save()

        globalEventChannel().subscribeGroupMessages {
            //测试成功
            val getDetailOfId: Pattern = Pattern.compile("(?i)^(${Setting.command.getDetailOfId})([0-9]*[1-9][0-9]*)-([0-9]*[1-9][0-9]*)\$")
            content { getDetailOfId.matcher(message.contentToString()).find() } quoteReply { PicDetails.getDetailOfId(this, pluginLogger) }

            //测试成功
            val rank: Pattern = Pattern.compile("(?i)^(${Setting.command.showRank})(day|week|month|setu)-([0-9]*[1-9][0-9]*)\$")
            content { rank.matcher(message.contentToString()).find() } quoteReply { Rank.showRank(this, pluginLogger) }

            //测试成功
            val findUserWorksById: Pattern = Pattern.compile("(?i)^(${Setting.command.findUserWorksById})([0-9]*[1-9][0-9]*)\$")
            content { findUserWorksById.matcher(message.contentToString()).find() } quoteReply { UserDetails.findUserWorksById(this, pluginLogger) }
            //测试成功
            val searchInfoByPic: Pattern = Pattern.compile("(?i)^(${Setting.command.searchInfoByPic}).+$")
            content { searchInfoByPic.matcher(message.contentToString()).find() } quoteReply { Trace.searchInfoByPic(this, pluginLogger) }

            val setu: Pattern = Pattern.compile("(?i)^(${Setting.command.setu})$")
            content { setu.matcher(message.contentToString()).find() } reply { SexyCenter.init(this, pluginLogger) }

            //测试成功
            val tag: Pattern = Pattern.compile("(?i)^(${Setting.command.tag})([\\s\\S]*)-([0-9]*[1-9][0-9]*)\$")
            content { tag.matcher(message.contentToString()).find() } quoteReply { Tag.init(this, pluginLogger) }
            //测试成功
            val picToSearch: Pattern = Pattern.compile("(?i)^(${Setting.command.picToSearch}).+$")
            content { picToSearch.matcher(message.contentToString()).find() } quoteReply { SearchPicCenter.forward(this, pluginLogger) }


//            val coloring: Pattern = Pattern.compile("(?i)^(上色)$")
//            content { coloring.matcher(message.contentToString()).find() } quoteReply { Style2paints.coloring(this, pluginLogger) }
//
//            Agefans().startTask()
        }

    }

    override fun PluginComponentStorage.onLoad() {
        /**
         * 初始化插件
         */
        init()
    }

}