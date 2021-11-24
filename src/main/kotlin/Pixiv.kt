package com.hcyacg

import com.hcyacg.config.Config
import com.hcyacg.details.PicDetails
import com.hcyacg.details.UserDetails
import com.hcyacg.initial.Configuration.Companion.init
import com.hcyacg.rank.Rank
import com.hcyacg.rank.Tag
import com.hcyacg.search.Ascii2d
import com.hcyacg.search.Saucenao
import com.hcyacg.search.SearchPicCenter
import com.hcyacg.search.Trace
import com.hcyacg.sexy.SexyCenter
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.content
import java.util.concurrent.LinkedBlockingQueue

object Pixiv : KotlinPlugin(
    JvmPluginDescription(
        id = "com.hcyacg.pixiv",
        name = "pixiv插画",
        version = "1.5.6",
    ) {
        author("Nekoer")
        info("""pixiv插画""")
    }
) {

    override fun onEnable() {
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> { event ->

            if (event.message.content.indexOf(Config.getDetailOfId.toString())>= 0){
                PicDetails().getDetailOfId(event, logger)
            }


            if (event.message.content.indexOf(Config.showRank.toString()) >= 0){
                Rank().showRank(event, logger)
            }

            if (event.message.content.indexOf(Config.findUserWorksById.toString()) >= 0){
                UserDetails().findUserWorksById(event, logger)
            }

            if (event.message.content.indexOf(Config.searchInfoByPic.toString()) >= 0){
                Trace().searchInfoByPic(event, logger)
            }

            if (event.message.content.indexOf(Config.setu.toString()) >= 0){
                SexyCenter().init(event, logger)
            }

            if (event.message.content.indexOf(Config.tag.toString()) >= 0){
                Tag().init(event, logger)
            }

            if (event.message.toString().indexOf(Config.picToSearch.toString())>= 0){
                SearchPicCenter().forward(event, logger)
            }


        }

    }

    override fun PluginComponentStorage.onLoad() {
        /**
         * 初始化插件
         */
        init()
    }

}