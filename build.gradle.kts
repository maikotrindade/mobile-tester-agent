val kotlin_version: String by project
val logback_version: String by project
val koog_agents_version: String by project
val dotenv_kotlin_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("io.ktor.plugin") version "3.1.3"
}

group = "io.github.maikotrindade"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.1.3")
    implementation("io.ktor:ktor-server-openapi:3.1.3")
    implementation("io.ktor:ktor-server-swagger:3.1.3")
    implementation("io.ktor:ktor-server-call-logging:3.1.3")
    implementation("io.ktor:ktor-server-netty:3.1.3")
    implementation("io.ktor:ktor-server-config-yaml:3.1.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")

    implementation("ch.qos.logback:logback-classic:${logback_version}")

    implementation("ai.koog:koog-agents:$koog_agents_version")
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenv_kotlin_version")
}
