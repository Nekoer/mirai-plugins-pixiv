package com.hcyacg.config


import com.hcyacg.entity.AgefansItem
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 配置文件中各键值对参数
 */
object Config {
    val agefans: CopyOnWriteArrayList<AgefansItem> = CopyOnWriteArrayList()
    val isSend: CopyOnWriteArrayList<AgefansItem> = CopyOnWriteArrayList()
}