package com.hcyacg.initial

import com.hcyacg.initial.entity.Command
import com.hcyacg.initial.entity.Config
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

object Setting : AutoSavePluginConfig("Setting") {

    @ValueName("admins")
    var admins: MutableList<String> by value()
    @ValueName("groups")
    var groups: MutableList<String> by value()
    @ValueName("command")
    var command: Command by value()
    @ValueName("config")
    var config: Config by value()

}