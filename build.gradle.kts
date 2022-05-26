import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion


    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.11.0-RC2"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "com.hcyacg"
version = "1.7.2"

repositories {
//    mavenLocal()
//    maven("https://maven.aliyun.com/repository/gradle-plugin")
//    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
}
dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.jsoup:jsoup:1.14.3")

    implementation("com.madgag:animated-gif-lib:1.4")
    compileOnly("org.bytedeco:javacv-platform:1.5.7")
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
//    workingDir = rootProject.buildDir.resolve("pub").apply { mkdirs() }

    // description from project.description by default
    githubProject("Nekoer", "mirai-plugins-pixiv")

    useCentralS01()
    licenseFromGitHubProject("licenseAGPL-3.0", "master")

    publication {
        artifact(tasks.getByName("buildPlugin"))
        artifact(tasks.getByName("buildPluginLegacy"))
    }
}