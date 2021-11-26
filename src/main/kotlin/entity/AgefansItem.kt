package com.hcyacg.entity
import com.alibaba.fastjson.annotation.JSONField

data class AgefansItem(
    @JSONField(name = "id")
    val id: String,
    @JSONField(name = "isnew")
    val isnew: Boolean,
    @JSONField(name = "mtime")
    val mtime: String,
    @JSONField(name = "name")
    val name: String,
    @JSONField(name = "namefornew")
    val namefornew: String,
    @JSONField(name = "wd")
    val wd: Int
)