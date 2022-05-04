package com.hcyacg.utils

import com.hcyacg.initial.Setting
import net.mamoe.mirai.utils.MiraiLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CacheUtil {
    private val logger = MiraiLogger.Factory.create(this::class.java)

    enum class Type{
        PIXIV,YANDE,LOLICON,KONACHAN,NONSUPPORT
    }

    fun saveToLocal(infoStream: ByteArrayOutputStream, type:Type,imageName:String){
        try{
            var temp:String? = "temp"
            temp = when (type){
                Type.PIXIV -> {
                    "pixiv"
                }
                Type.YANDE -> {
                    "yande"
                }
                Type.LOLICON -> {
                    "lolicon"
                }
                Type.KONACHAN -> {
                    "konachan"
                }
                Type.NONSUPPORT -> {
                    null
                }
            }

            if (temp.isNullOrEmpty()){
                return
            }

            val directory = File(Setting.config.cache.directory)
            val imageDir = File(directory.path + File.separator + temp)
            if (!imageDir.exists()){
                imageDir.mkdirs()
            }
            val out = FileOutputStream(directory.path + File.separator + temp+ File.separator +imageName)
            out.write(infoStream.toByteArray())
            out.close()
        }catch (e:Exception){
            logger.warning(e)
        }
    }

}
