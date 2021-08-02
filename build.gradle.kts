plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.7-M2"
}

group = "com.hcyacg"
version = "1.4"
//version = "1.4-FFmpeg"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/gradle-plugin")

    mavenCentral()
}
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.2.2")
    implementation("com.alibaba:fastjson:1.2.76")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    compileOnly("com.madgag:animated-gif-lib:1.4")
    compileOnly("org.bytedeco:javacv-platform:1.5.5")

}
