package com.hcyacg

import com.hcyacg.details.PicDetails
import com.hcyacg.details.UserDetails
import com.hcyacg.initial.Setting
import com.hcyacg.rank.Rank
import com.hcyacg.rank.Tag
import com.hcyacg.search.SearchPicCenter
import com.hcyacg.search.Trace
import com.hcyacg.sexy.LoliconCenter
import com.hcyacg.sexy.SexyCenter
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import java.util.regex.Pattern

object Pixiv : KotlinPlugin(
    JvmPluginDescription(
        id = "com.hcyacg.pixiv",
        name = "pixiv插画",
        version = "1.7.0",
    ) {
        author("Nekoer")
        info("""pixiv插画""")
    }
) {

    override fun onEnable() {
        Setting.reload()
        Setting.save()

        globalEventChannel().subscribeGroupMessages {
            //测试成功
            val getDetailOfId: Pattern =
                Pattern.compile("(?i)^(${Setting.command.getDetailOfId})([0-9]*[1-9][0-9]*)|-([0-9]*[1-9][0-9]*)\$")
            content { getDetailOfId.matcher(message.contentToString()).find() } quoteReply {
                PicDetails.getDetailOfId(
                    this
                )
            }

            //测试成功
            val rank: Pattern =
                Pattern.compile("(?i)^(${Setting.command.showRank})(day|week|month|setu)-([0-9]*[1-9][0-9]*)\$")
            content { rank.matcher(message.contentToString()).find() } quoteReply { Rank.showRank(this) }

            //测试成功
            val findUserWorksById: Pattern =
                Pattern.compile("(?i)^(${Setting.command.findUserWorksById})([0-9]*[1-9][0-9]*)|-([0-9]*[1-9][0-9]*)\$")
            content {
                findUserWorksById.matcher(message.contentToString()).find()
            } quoteReply { UserDetails.findUserWorksById(this) }
            //测试成功
            val searchInfoByPic: Pattern = Pattern.compile("(?i)^(${Setting.command.searchInfoByPic}).+$")
            content { searchInfoByPic.matcher(message.contentToString()).find() } quoteReply {
                Trace.searchInfoByPic(
                    this
                )
            }

            val setu: Pattern = Pattern.compile("(?i)^(${Setting.command.setu})[ ]?[\\S]*[ ]?(r18)?\$")
            content { setu.matcher(message.contentToString()).find() } reply { SexyCenter.init(this) }

            //测试成功
            val tag: Pattern = Pattern.compile("(?i)^(${Setting.command.tag})([\\s\\S]*)-([0-9]*[1-9][0-9]*)\$")
            content { tag.matcher(message.contentToString()).find() } quoteReply { Tag.init(this) }
            //测试成功
            val picToSearch: Pattern = Pattern.compile("(?i)^(${Setting.command.picToSearch}).+$")
            content {
                picToSearch.matcher(message.contentToString()).find()
            } quoteReply { SearchPicCenter.forward(this) }

            val lolicon: Pattern = Pattern.compile("(?i)^(${Setting.command.lolicon})( ([^ ]*)( (r18))?)?\$")
            content { lolicon.matcher(message.contentToString()).find() } quoteReply { LoliconCenter.load(this) }

            content { "帮助".contentEquals(message.contentToString()) || "/help".contentEquals(message.contentToString()) } quoteReply { Helper.load(this) }

            content { "切换涩图开关".contentEquals(message.contentToString()) } quoteReply { Helper.setuEnable(this) }
            content { "切换缓存开关".contentEquals(message.contentToString()) } quoteReply { Helper.enableLocal(this) }

            val enableSetu = Pattern.compile("(?i)^(关闭|开启)(pixiv|yande|lolicon|local|konachan)\$")
            content { enableSetu.matcher(message.contentToString()).find() } quoteReply { Helper.enableSetu(this) }

//            content { "test".contentEquals(message.contentToString()) } quoteReply {PicDetails.getUgoira()}

//            val coloring: Pattern = Pattern.compile("(?i)^(上色)$")
//            content { coloring.matcher(message.contentToString()).find() } quoteReply { Style2paints.coloring(this, pluginLogger) }
//
        }

    }

}