import java.util.*

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.0.0"
    id("com.gradleup.shadow") version "9.2.2"
    //id("deployment-tasks")

    application
}

group = "org.pafoid.kboggle"
version = "1.0.0"
application {
    mainClass.set("org.pafoid.kboggle.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks.shadowJar {
    archiveFileName.set("boggle-server-${project.version}.jar")
}

tasks.register("generateRunScript") {
    group = "deployment"
    dependsOn(tasks.shadowJar)

    doLast {
        val jarName = tasks.shadowJar.get().archiveFileName.get()
        val outputDir = tasks.shadowJar.get().destinationDirectory.get().asFile

        // Windows batch file
        val batchTemplate = file("src/main/resources/start.bat.template").readText()
        val batchContent = batchTemplate.replace("\$jarName", jarName)
        File(outputDir, "start.bat").writeText(batchContent)

        // Optional: Linux/Mac script
        val shellTemplate = file("src/main/resources/start.sh.template").readText()
        val shellContent = shellTemplate.replace("\$jarName", jarName)
        File(outputDir, "start.sh").apply {
            writeText(shellContent)
            setExecutable(true)
        }

        println("Generated run scripts in $outputDir")
    }
}


tasks.register("deploy") {
    group = "deployment"
    dependsOn(tasks.shadowJar, "generateRunScript")

    // Read local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

    val deployDir = localProperties.getProperty("deploy.dir")

    if (deployDir != null) {
        copy {
            from(tasks.shadowJar.get().destinationDirectory) {
                include("*.*")
            }
            into(deployDir)
        }

        doLast {
            println("✅ Copied to: $deployDir")
        }
    } else {
        doLast {
            println("⚠️Error: deploy.dir not set in local.properties")
        }
    }
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
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.jwt)


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