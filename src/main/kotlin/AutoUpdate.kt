package com.hcyacg

import com.hcyacg.Pixiv.save
import com.hcyacg.entity.GithubReleaseItem
import com.hcyacg.initial.Github
import com.hcyacg.utils.DownloadUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.rootDir
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.*
import java.io.File
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


/**
 * 自动更新
 */
object AutoUpdate {
    const val githubUrl = "https://api.github.com/repos/Nekoer/mirai-plugins-pixiv/releases?per_page=1"
    private val headers = Headers.Builder()
    private val requestBody: RequestBody? = null
    private val logger = MiraiLogger.Factory.create(this::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 加载数据并轮询 每小时查询一次
     */
    fun load() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val data =
                    RequestUtil.request(RequestUtil.Companion.Method.GET, githubUrl, requestBody, headers.build())

                val githubRelease = data?.let { json.decodeFromJsonElement<List<GithubReleaseItem>>(it) } ?: return

                /**
                 * 判断是否有内容
                 */
                if (githubRelease.isEmpty()) {
                    return
                }
                /**
                 * 首先判断是否是默认值 0
                 */

                if (githubRelease[0].id!! != Github.versionId) {
                    /**
                     * 如果本地version-id与github的不同，则说明可能有新版本；继续进行tag和插件版本进行对比
                     */
                    if (!githubRelease[0].tagName.contentEquals(Pixiv.version.toString())) {

                        /**
                         * 对比后进行下载
                         */


                        val temp = if (githubRelease[0].assets?.get(0)?.browserDownloadUrl!!.indexOf("?") > -1) {
                            githubRelease[0].assets?.get(0)?.browserDownloadUrl!!.substring(
                                0,
                                githubRelease[0].assets?.get(0)?.browserDownloadUrl!!.indexOf("?")
                            ).split("/").last()
                        } else {
                            githubRelease[0].assets?.get(0)?.browserDownloadUrl!!.split("/").last()
                        }

                        logger.info("$temp 更新开始")
                        DownloadUtil.download(githubRelease[0].assets?.get(0)?.browserDownloadUrl!!.replace(
                            "https://github.com/",
                            "https://download.fastgit.org/"
                        ),
                            MiraiConsole.pluginManager.pluginsFolder.path,
                            object : DownloadUtil.OnDownloadListener {
                                override fun onDownloadSuccess() {
                                    logger.info("$temp 更新完成")

                                    /**
                                     * 插件下载完后覆盖本地的version-id并保存
                                     */
                                    Github.versionId = githubRelease[0].id!!
                                    Github.save()

                                    /**
                                     * 启动mcl后关闭本程序
                                     */
                                    logger.info("请删除旧版本插件并重启程序")
                                }

                                override fun onDownloading(progress: Int) {
                                    logger.info("$temp 已下载 $progress %")
                                }

                                override fun onDownloadFailed() {
                                    logger.warning("$temp 更新失败")
                                }
                            }
                        )

                    }
                }
            }
        }, Date(), 60 * 60 * 1000L)
    }

}