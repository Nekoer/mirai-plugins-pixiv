package com.hcyacg.utils

import com.hcyacg.initial.Config
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.codec.binary.Base64
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ImageUtil {
    companion object {
        private val client = OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS).readTimeout(60000,
            TimeUnit.MILLISECONDS)
        private val headers = Headers.Builder().add("referer", "https://i.acgmx.com")
            .add(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36 Edg/84.0.522.59"
            )
        private val logger by logger()


        /**
         * 将图片链接读取到内存转换成ByteArrayOutputStream，方便操作
         */
        fun getImage(imageUri: String, type: CacheUtil.Type): ByteArrayOutputStream {
            val infoStream = ByteArrayOutputStream()
            val host = Config.proxy.host
            val port = Config.proxy.port

            try{

                val temp = if(imageUri.indexOf("?") > -1){
                    imageUri.substring(0, imageUri.indexOf("?")).split("/").last()
                }else{
                    imageUri.split("/").last()
                }

//                println("temp: $temp")
//                println("imageUri: $imageUri")
                val request = Request.Builder().url(imageUri).headers(headers.build()).get().build()
                val response: Response  = if (host.isBlank() || port == -1){
                    client.build().newCall(request).execute()
                }else{
                    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
                    client.proxy(proxy).build().newCall(request).execute()
                }

                val `in` = response.body?.byteStream()


                val buffer = ByteArray(2048)
                var len: Int
                if (`in` != null) {
                    while (`in`.read(buffer).also { len = it } > 0) {
                        infoStream.write(buffer, 0, len)
                    }
                }
                infoStream.write((Math.random() * 100).toInt() + 1)
                infoStream.close()

                if (Config.cache.enable){
                    CacheUtil.saveToLocal(infoStream,type,temp)
                }

                return infoStream
            }catch (e:Exception){
                logger.warn { "${imageUri}获取失败,请检查网络" }
                e.printStackTrace()
                return infoStream
            }
        }

        /**
         * 将图片链接读取到内存转换成ByteArrayOutputStream，方便操作
         */
        fun getVideo(videoUri: String): InputStream? {

//            val request: Request = Request.Builder().url(imageUri.replace("i.pximg.net","i.pixiv.cat")).get().build()
            val request: Request = Request.Builder().url(videoUri).get().build()
            return client.build().newCall(request).execute().body?.byteStream()
        }


        /**
         * 旋转角度
         * @param src 源图片
         * @param angel 角度
         * @return 目标图片
         */
        fun  rotate(src: Image, angel:Int): ByteArrayOutputStream
        {
            val srcWidth: Int = src.getWidth(null)
            val srcHeight : Int = src.getHeight (null)
            val rectDes : Rectangle = calcRotatedSize ( Rectangle ( Dimension (
                srcWidth, srcHeight)), angel)


            var res: BufferedImage? = null
            res = BufferedImage (rectDes.width, rectDes.height,BufferedImage.TYPE_INT_RGB)
            val g2: Graphics2D = res.createGraphics ()
            // transform(这里先平移、再旋转比较方便处理；绘图时会采用这些变化，绘图默认从画布的左上顶点开始绘画，源图片的左上顶点与画布左上顶点对齐，然后开始绘画，修改坐标原点后，绘画对应的画布起始点改变，起到平移的效果；然后旋转图片即可)

            //平移（原理修改坐标系原点，绘图起点变了，起到了平移的效果，如果作用于旋转，则为旋转中心点）
            g2.translate((rectDes.width - srcWidth) / 2, (rectDes.height - srcHeight) / 2)


            //旋转（原理transalte(dx,dy)->rotate(radians)->transalte(-dx,-dy);修改坐标系原点后，旋转90度，然后再还原坐标系原点为(0,0),但是整个坐标系已经旋转了相应的度数 ）
            g2.rotate(Math.toRadians(angel.toDouble()), srcWidth.toDouble() / 2, srcHeight.toDouble() / 2)

//        //先旋转（以目标区域中心点为旋转中心点，源图片左上顶点对准目标区域中心点，然后旋转）
//        g2.translate(rect_des.width/2,rect_des.height/ 2);
//        g2.rotate(Math.toRadians(angel));
//        //再平移（原点恢复到源图的左上顶点处（现在的右上顶点处），否则只能画出1/4）
//        g2.translate(-src_width/2,-src_height/2);


            g2.drawImage(src, null, null)
            return imageToBytes(res,"PNG")
        }


        /**
         * 转换BufferedImage 数据为byte数组
         *
         * Image对象
         * @param format
         * image格式字符串.如"gif","png"
         * @return byte数组
         */
        private fun imageToBytes(bImage:BufferedImage, format:String):ByteArrayOutputStream {
            val out = ByteArrayOutputStream()

            try {
                ImageIO.write(bImage, format, out)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return out
        }

        /**
         * 计算转换后目标矩形的宽高
         * @param src 源矩形
         * @param angel 角度
         * @return 目标矩形
         */
        private fun calcRotatedSize(src: Rectangle, angel: Int): Rectangle {
            val cos = abs(cos(Math.toRadians(angel.toDouble())))
            val sin = abs(sin(Math.toRadians(angel.toDouble())))
            val desWidth = (src.width * cos).toInt() + (src.height * sin).toInt()
            val desHeight = (src.height * cos).toInt() + (src.width * sin).toInt()
            return Rectangle(Dimension(desWidth, desHeight))
        }

        fun generateImage(imgData: String): ByteArray? { // 对字节数组字符串进行Base64解码并生成图片
            var file = imgData
            try {
                // Base64解码
                if (file.contains("data:")) {
                    val start: Int = file.indexOf(",")
                    file = file.substring(start + 1)
                }

                file = file.replace("\r|\n", "")
                file = file.trim()
                return Base64.decodeBase64(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
    }
}