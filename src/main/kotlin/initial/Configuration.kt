package com.hcyacg.initial

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.io.File

class Configuration {
    companion object {
        private val systemPath: String = System.getProperty("user.dir")
        private val fileDirectory: File =
            File(systemPath + File.separator + "config" + File.separator + "com.hcyacg.pixiv")
        private val file: File = File(fileDirectory.path + File.separator + "setting.json")
        var projectJson: JSONObject = JSONObject.parseObject("{}")


        /**
         * 初始化插件各项配置
         */
        fun init() {

            if (file.exists()){
                projectJson = JSONObject.parseObject(file.readText())
                //admins{}内容
                JSON.parseArray(projectJson.getString("admins")).forEach {
                    Setting.admins.add(it.toString())
                }

                JSON.parseArray(projectJson.getString("groups")).forEach {
                    Setting.groups.add(it.toString())
                }

                //配置中config{}内容
                val config: JSONObject = JSONObject.parseObject(projectJson.getString("config"))
                val token: JSONObject = JSONObject.parseObject(config.getString("token"))

                Setting.config.token.acgmx = token.getString("acgmx")
                Setting.config.token.saucenao = token.getString("saucenao")

                Setting.config.recall = config.getLong("recall")
                //设置http请求代理
                val proxy: JSONObject = JSONObject.parseObject(config.getString("proxy"))
                if (null != proxy.getString("host")){
                    Setting.config.proxy.host = proxy.getString("host")
                }
                if (null != proxy.getString("port")){
                    Setting.config.proxy.port = proxy.getIntValue("port")
                }
                //配置中command{}内容
                val command: JSONObject = JSONObject.parseObject(projectJson.getString("command"))
                Setting.command.getDetailOfId = command.getString("getDetailOfId")
                Setting.command.picToSearch = command.getString("picToSearch")
                Setting.command.showRank = command.getString("showRank")
                Setting.command.findUserWorksById = command.getString("findUserWorksById")
                Setting.command.searchInfoByPic = command.getString("searchInfoByPic")
                Setting.command.setu = command.getString("setu")
                Setting.command.tag = command.getString("tag")

                file.delete()
            }

        }
    }
}