import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.9.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.16.0"

    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "com.hcyacg"
version = "1.7.8"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}


dependencies {
    implementation("org.apache.commons:commons-lang3:3.17.0")
    
//    val overflowVersion = "2.16.0-a1e9d27-SNAPSHOT"
//    compileOnly("top.mrxiaom:overflow-core-api:$overflowVersion")
//    testConsoleRuntime("top.mrxiaom:overflow-core:$overflowVersion")

    implementation("commons-codec:commons-codec:1.17.1")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.madgag:animated-gif-lib:1.4")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

    compileOnly("org.bytedeco:javacv-platform:1.5.10")
//    compileOnly
    implementation(kotlin("stdlib-jdk8"))

}

noArg {
    annotation("com.hcyacg.anno.NoArgOpenDataClass")
}

allOpen{
    annotation("com.hcyacg.anno.NoArgOpenDataClass")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"

}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

mavenCentralPublish {
    artifactId = "pixiv"
    groupId = "com.hcyacg"
    projectName = "mirai plugins pixiv"

    //developer(1,"Nekoer","hcyacg@vip.qq.com","","","","")
//    workingDir = rootProject.buildDir.resolve("pub").apply { mkdirs() }

    // description from project.description by default
    //githubProject("Nekoer", "mirai-plugins-pixiv")
    singleDevGithubProject("Nekoer", "mirai-plugins-pixiv")
    useCentralS01()
    licenseFromGitHubProject("AGPL-3.0", "master")

    publication {
        artifact(tasks.getByName("buildPlugin"))
        artifact(tasks.getByName("buildPluginLegacy"))
    }
}