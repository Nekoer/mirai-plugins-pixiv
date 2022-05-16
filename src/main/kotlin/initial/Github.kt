package com.hcyacg.initial

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

object Github :AutoSavePluginData("Github"){
    @ValueName("version-id")
    var versionId :Int by value(0)
}