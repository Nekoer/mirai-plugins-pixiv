package com.hcyacg.rank

import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import com.hcyacg.utils.RequestUtil
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody

/**
 * 排行榜总处理中心
 */
class TotalProcessing {
    private val headers = Headers.Builder().add("token", Setting.config.token.acgmx)
    private val requestBody: RequestBody? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)
    /**
     * 动态拼接参数并返回数据
     */
    fun dealWith(type: String, mode: String, page: Int, perPage: Int, date: String) : JSONObject? {
        return try{
            RequestUtil.requestObject(
                RequestUtil.Companion.Method.GET,
                "https://api.acgmx.com/public/ranking?ranking_type=${type}&mode=${mode}&date=$date&per_page=$perPage&page=$page",
                requestBody,
                headers.build(),
                logger
            )
        }catch (e:Exception){
            e.printStackTrace()
            null
        }

    }


}