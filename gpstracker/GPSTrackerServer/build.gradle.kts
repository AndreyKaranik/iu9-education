import org.gradle.kotlin.dsl.accessors.warnAboutDiscontinuedJsonProjectSchema

plugins {
    kotlin("jvm") version "1.9.0"
    idea
    application
    id("io.ktor.plugin") version "2.3.7"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.valiktor:valiktor-core:0.12.0")

    implementation("com.sun.mail:javax.mail:1.6.2")

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}