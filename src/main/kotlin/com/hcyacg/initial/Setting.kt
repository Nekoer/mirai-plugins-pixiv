package com.hcyacg.initial


import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

object Setting : AutoSavePluginConfig("Setting") {

    @ValueName("admins")
    @ValueDescription("插件管理员")
    var admins: MutableList<String> by value()

    @ValueName("groups")
    @ValueDescription("可以使用涩图的QQ群")
    var groups: MutableList<String> by value()

}