package com.hcyacg.initial

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.hcyacg.config.Config.acgmx
import com.hcyacg.config.Config.admins
import com.hcyacg.config.Config.findUserWorksById
import com.hcyacg.config.Config.getDetailOfId
import com.hcyacg.config.Config.groups
import com.hcyacg.config.Config.host
import com.hcyacg.config.Config.picToSearch
import com.hcyacg.config.Config.port
import com.hcyacg.config.Config.recall
import com.hcyacg.config.Config.saucenao
import com.hcyacg.config.Config.showRank
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream

class Configuration {
    companion object {
        private val systemPath: String = System.getProperty("user.dir")
        private val fileDirectory: File =
            File(systemPath + File.separator + "config" + File.separator + "com.hcyacg.pixiv")
        private val file: File = File(fileDirectory.path + File.separator + "setting.json")
        var projectJson: JSONObject = JSONObject.parseObject("{}")
        private val logger = MiraiLogger.create("Bot")

        /**
         * 初始化插件各项配置
         */
        fun init() {
            /**
             * 不存在配置文件将自动创建
             */
            if (!fileDirectory.exists() || !file.exists()) {
                fileDirectory.mkdirs()
                file.createNewFile()
                val resourceAsStream: InputStream? =
                    Configuration::class.java.classLoader.getResourceAsStream("setting.json")
                resourceAsStream?.let { file.writeBytes(it.readAllBytes()) }
                logger.warning("初始化配置文件,请在config/com.hcyacg.pixiv/setting.json配置相关参数")
            } else {
                /**
                 * 添加配置
                 */
                projectJson = JSONObject.parseObject(file.readText())
                //admins{}内容
                admins = JSON.parseArray(projectJson.getString("admins"))
                for ((index, e) in admins.withIndex()) {
                    admins[index] = e.toString()
                }

                groups = JSON.parseArray(projectJson.getString("groups"))
                for ((index, e) in groups.withIndex()) {
                    groups[index] = e.toString()
                }
                //配置中config{}内容
                val config: JSONObject = JSONObject.parseObject(projectJson.getString("config"))
                val token: JSONObject = JSONObject.parseObject(config.getString("token"))
                acgmx = token.getString("acgmx")
                saucenao = token.getString("saucenao")

                recall = config.getLong("recall")
                //设置http请求代理
                val proxy: JSONObject = JSONObject.parseObject(config.getString("proxy"))
                host = proxy.getString("host")
                port = proxy.getIntValue("port")
                //配置中command{}内容
                val command: JSONObject = JSONObject.parseObject(projectJson.getString("command"))
                getDetailOfId = command.getString("getDetailOfId")
                picToSearch = command.getString("picToSearch")
                showRank = command.getString("showRank")
                findUserWorksById = command.getString("findUserWorksById")

            }
        }
    }
}