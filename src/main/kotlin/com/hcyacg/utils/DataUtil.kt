package com.hcyacg.utils

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.NumberFormat

/**
 * @Author Nekoer
 * @Date 2020/8/12 12:18
 * @Desc 对文本进行各种操作
 */
class DataUtil {
    companion object {
        private val logger by logger()
        fun getImageLink(chain: MessageChain): String? {
            chain.forEach {
                if (it is Image) {
                    return getImageLinkFromImage(it)
                }
            }
            return null
        }

        fun getImageLinkFromImage(image: Image): String {
            val pic = image.toString()
            return if (pic.contains("overflow:image")) {
                image.imageId
            } else {
                val picUri = image.imageId.replace("-", "")
                "https://gchat.qpic.cn/gchatpic_new/0/0-0-${picUri}/0?"
            }
        }

        fun urlEncode(url: String): String {
            return URLEncoder.encode(url, "UTF-8")
        }

        fun getSubString(text: String, left: String?, right: String?): String {
            val result: String
            var zLen: Int
            if (left == null || left.isEmpty()) {
                zLen = 0
            } else {
                zLen = text.indexOf(left)
                if (zLen > -1) {
                    zLen += left.length
                } else {
                    zLen = 0
                }
            }
            var yLen = text.indexOf(right!!, zLen)
            if (yLen < 0 || right.isEmpty()) {
                yLen = text.length
            }
            result = text.substring(zLen, yLen)
            return result
        }

        @Throws(UnsupportedEncodingException::class)
        fun getFileNameByURL(data: String): String {
            val resultURL = StringBuilder()
            //遍历字符串
            for (element in data) {
                //只对汉字处理
                val encode = URLEncoder.encode(element.toString() + "", "UTF-8")
                resultURL.append(encode)
            }
            return resultURL.toString()
        }


        fun getPercentFormat(d: Double, IntegerDigits: Int, FractionDigits: Int): String? {
            val nf = NumberFormat.getPercentInstance()
            nf.maximumIntegerDigits = IntegerDigits //小数点前保留几位
            nf.minimumFractionDigits = FractionDigits // 小数点后保留几位
            return nf.format(d)
        }
    }
}

