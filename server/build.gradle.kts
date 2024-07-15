plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.0.0"

    application
}

group = "org.pafoid.kboggle"
version = "1.0.0"
application {
    mainClass.set("org.pafoid.kboggle.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.statuspages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.websockets)


    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

repositories {
    mavenCentral()
}