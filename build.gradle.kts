val logback_version: String by project
val koog_agents_version: String by project
val dotenv_kotlin_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "io.github.maikotrindade"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("ch.qos.logback:logback-classic:${logback_version}")

    implementation("ai.koog:koog-agents:$koog_agents_version")
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenv_kotlin_version")
}
