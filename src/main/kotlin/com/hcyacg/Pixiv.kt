package com.hcyacg

import com.hcyacg.details.PicDetails
import com.hcyacg.details.UserDetails
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.initial.Github
import com.hcyacg.initial.Setting
import com.hcyacg.lowpoly.LowPoly
import com.hcyacg.rank.Rank
import com.hcyacg.rank.Tag
import com.hcyacg.search.SearchPicCenter
import com.hcyacg.search.Trace
import com.hcyacg.sexy.LoliconCenter
import com.hcyacg.sexy.SexyCenter
import com.hcyacg.sexy.WarehouseCenter
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object Pixiv : KotlinPlugin(
    JvmPluginDescription(
        id = "com.hcyacg.pixiv",
        name = "pixiv插画",
        version = "1.7.6",
    ) {
        author("Nekoer")
        info("""pixiv插画""")
    }
) {

    override fun onDisable() {
        Setting.save()
        Config.save()
        Command.save()
        Github.save()
    }

    override fun onEnable() {
        Setting.reload()
        Config.reload()
        Command.reload()
        Github.reload()
//        AutoUpdate.load()


        val searchImageWaitMap = ConcurrentHashMap<String, Long>()
        val searchAnimeWaitMap = ConcurrentHashMap<String, Long>()
        val nsfwWaitMap = ConcurrentHashMap<String, Long>()

        globalEventChannel().subscribeGroupMessages {
            content { group.id == 104967737L } reply {
                println(this.message)
                println(this.message.contentToString())
            }

            //测试成功
            val getDetailOfId: Pattern =
                Pattern.compile("(?i)^(${Command.getDetailOfId})([0-9]*[1-9][0-9]*)|-([0-9]*[1-9][0-9]*)\$")
            content { getDetailOfId.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) } quoteReply {
                PicDetails.load(
                    this
                )
            }

            //测试成功
            val rank: Pattern =
                Pattern.compile("(?i)^(${Command.showRank})(daily|weekly|monthly|rookie|original|male|female|daily_r18|weekly_r18|male_r18|female_r18|r18g)-([0-9]*[1-9][0-9]*)\$")
            content { rank.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) } quoteReply { Rank.showRank(this) }

            //测试成功
            val findUserWorksById: Pattern =
                Pattern.compile("(?i)^(${Command.findUserWorksById})([0-9]*[1-9][0-9]*)|-([0-9]*[1-9][0-9]*)\$")
            content {
                findUserWorksById.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString())
            } quoteReply { UserDetails.findUserWorksById(this) }
            //测试成功
            val searchInfoByPic: Pattern = Pattern.compile("(?i)^(${Command.searchInfoByPic})(?:\\n?.+)?\$")
            content { searchInfoByPic.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString())
                    &&  (searchAnimeWaitMap["${this.group.id}-${this.sender.id}"]?: 0) < System.currentTimeMillis()
            } quoteReply {
                if (DataUtil.getImageLink(this.message) == null) {
                    searchAnimeWaitMap["${this.group.id}-${this.sender.id}"] = System.currentTimeMillis() + Config.waitTime * 1000
                    "请在${Config.waitTime}秒内发送搜番图片"
                } else {
                    Trace.searchInfoByPic(this)
                }
            }
            content {
                (searchAnimeWaitMap["${this.group.id}-${this.sender.id}"] ?: 0) >= System.currentTimeMillis()
            } reply {
                searchAnimeWaitMap["${this.group.id}-${this.sender.id}"] = 0
                Trace.searchInfoByPic(this)
            }

            //(?i)^[@]\d+[ ][${Command.detect}](\n)?.+|(${Command.detect})?.+$
            //(?i)^(${Command.detect})(\n)?.+$
            //(?i)^(${Command.detect})(?:\n?.+)?$
            val detect: Pattern = Pattern.compile("(?i)^(${Command.detect})(?:\\n?.+)?\$")
            content {
                detect.matcher(message.contentToString()).find()
                        && !Setting.black.contains(group.id.toString())
                        && (nsfwWaitMap["${this.group.id}-${this.sender.id}"] ?: 0) < System.currentTimeMillis()
            } reply {
                if (DataUtil.getImageLink(this.message) == null) {
                    nsfwWaitMap["${this.group.id}-${this.sender.id}"] = System.currentTimeMillis() + Config.waitTime * 1000
                    "请在${Config.waitTime}秒内发送检测图片"
                } else {
                    Nsfw.load(this)
                }
            }
            content {
                (nsfwWaitMap["${this.group.id}-${this.sender.id}"] ?: 0) >= System.currentTimeMillis()
            } reply {
                nsfwWaitMap["${this.group.id}-${this.sender.id}"] = 0
                Nsfw.load(this)
            }

            val setu: Pattern = Pattern.compile("(?i)^(${Command.setu})\$")
            content { setu.matcher(message.contentToString()).find()  && !Setting.black.contains(group.id.toString()) } reply { SexyCenter.init(this) }

            val setuTag: Pattern = Pattern.compile("(?i)^(${Command.setu})[ ]\\S* ?(r18)?$")
            content { setuTag.matcher(message.contentToString()).find()  && !Setting.black.contains(group.id.toString())} reply { SexyCenter.yandeTagSearch(this) }

            //测试成功
            val tag: Pattern = Pattern.compile("(?i)^(${Command.tag})([\\s\\S]*)-([0-9]*[1-9][0-9]*)\$")
            content { tag.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) } quoteReply { Tag.init(this) }
            //测试成功
            val picToSearch: Pattern = Pattern.compile("(?i)^(${Command.picToSearch})(?:\\n?.+)?\$")
            content {
                picToSearch.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) &&
                        (searchImageWaitMap["${this.group.id}-${this.sender.id}"]?: 0) < System.currentTimeMillis()
            } quoteReply {
                if (DataUtil.getImageLink(this.message) == null) {
                    searchImageWaitMap["${this.group.id}-${this.sender.id}"] = System.currentTimeMillis() + Config.waitTime * 1000
                    "请在${Config.waitTime}秒内发送搜图图片"
                } else {
                    SearchPicCenter.forward(this)
                }
            }
            content {
                (searchImageWaitMap["${this.group.id}-${this.sender.id}"] ?: 0) >= System.currentTimeMillis()
            } reply {
                searchImageWaitMap["${this.group.id}-${this.sender.id}"] = 0
                SearchPicCenter.forward(this)
            }

            val lolicon: Pattern = Pattern.compile("(?i)^(${Command.lolicon})( ([^ ]*)( (r18))?)?\$")
            content { lolicon.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) } quoteReply { LoliconCenter.load(this) }

            content { Command.help.contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply { Helper.load(this) }

            content { "切换涩图开关".contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply { Helper.setuEnable(this) }
            content { "切换缓存开关".contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply { Helper.enableLocal(this) }
            content { "切换转发开关".contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply { Helper.enableForward(this) }
            content { "ban".contentEquals(message.contentToString()) or "unban".contentEquals(message.contentToString())} quoteReply { Helper.black(this) }
            content { "切换图片转发开关".contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply {
                Helper.enableImageToForward(
                    this
                )
            }
            content { "切换晶格化开关".contentEquals(message.contentToString()) && !Setting.black.contains(group.id.toString()) } quoteReply {
                Helper.enableLowPoly(
                    this
                )
            }
            content { message.contentToString().contains("设置撤回") && !Setting.black.contains(group.id.toString()) } quoteReply {
                Helper.changeRecall(
                    this
                )
            }

            content { message.contentToString().contains("设置loli图片大小") && !Setting.black.contains(group.id.toString()) } quoteReply {
                Helper.setLoliconSize(
                    this
                )
            }

            content { message.contentToString().contains("suki") && !Setting.black.contains(group.id.toString()) } quoteReply {
                WarehouseCenter.init(
                    this
                )
            }

            val vip = Pattern.compile("(?i)^(购买)(月费|季度|半年|年费)会员\$")
            content { vip.matcher(message.contentToString()).find() && !Setting.black.contains(group.id.toString()) } quoteReply {Vip.buy(this)}

            val enableSetu = Pattern.compile("(?i)^(关闭|开启)(pixiv|yande|lolicon|local|konachan)\$")
            content { enableSetu.matcher(message.contentToString()).find()  && !Setting.black.contains(group.id.toString())} quoteReply { Helper.enableSetu(this) }

            val enableSearch = Pattern.compile("(?i)^(关闭|开启)(ascii2d|google|saucenao|yandex|iqdb)\$")
            content { enableSearch.matcher(message.contentToString()).find()  && !Setting.black.contains(group.id.toString())} quoteReply { Helper.enableSearch(this) }


            val lowPoly = Pattern.compile("(?i)^(${Command.lowPoly}).+\$")
            content { lowPoly.matcher(message.contentToString()).find()  && !Setting.black.contains(group.id.toString())} quoteReply {
                val picUri = DataUtil.getImageLink(this.message)
                if (picUri == null) {
                    "请输入正确的命令 ${Command.lowPoly}图片"
                }
                val byte = ImageUtil.getImage(picUri!!, CacheUtil.Type.NONSUPPORT).toByteArray()
                val toExternalResource = LowPoly.generate(
                    ByteArrayInputStream(byte),
                    200,
                    1F,
                    true,
                    "png",
                    false,
                    200
                ).toByteArray().toExternalResource()


                withContext(Dispatchers.IO){
                    subject.sendImage(toExternalResource)
                }
                toExternalResource.close()
            }
//            content { "test".contentEquals(message.contentToString()) } quoteReply {PicDetails.getUgoira()}

//            val coloring: Pattern = Pattern.compile("(?i)^(上色)$")
//            content { coloring.matcher(message.contentToString()).find() } quoteReply { Style2paints.coloring(this, pluginLogger) }
//
        }

        //获取到退群事件，删除groups中的相同群号
        globalEventChannel().subscribeAlways<BotLeaveEvent>{
            Setting.groups.remove(it.groupId.toString())
            Setting.save()
            Setting.reload()
        }

    }

}