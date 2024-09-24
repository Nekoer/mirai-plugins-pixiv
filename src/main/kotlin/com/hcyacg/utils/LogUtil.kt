package com.hcyacg.utils

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlin.reflect.full.companionObject

fun <T : Any> T.logger(): Lazy<KLogger> {
    // 使logger的名字始终和最外层的类一致，即使是在companion object中初始化属性
    val ofClass = this.javaClass
    val clazz = ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass

    return lazy { KotlinLogging.logger(clazz.name) }
}


fun <T : Any> T.json(): Lazy<Json> {
    return lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }
}