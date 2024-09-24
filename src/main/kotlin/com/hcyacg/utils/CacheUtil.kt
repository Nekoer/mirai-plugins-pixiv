package com.hcyacg.utils

import com.hcyacg.initial.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CacheUtil {
    private val logger by logger()

    enum class Type{
        PIXIV,YANDE,LOLICON,KONACHAN,NONSUPPORT
    }

    fun saveToLocal(infoStream: ByteArrayOutputStream, type:Type,imageName:String){
        try{
            val temp:String? = when (type){
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

            val directory = File(Config.cache.directory)
            val imageDir = File(directory.path + File.separator + temp)
            if (!imageDir.exists()){
                imageDir.mkdirs()
            }
            val out = FileOutputStream(directory.path + File.separator + temp+ File.separator +imageName)
            out.write(infoStream.toByteArray())
            out.close()
        }catch (e:Exception){
            logger.error { e.message }
        }
    }

}
