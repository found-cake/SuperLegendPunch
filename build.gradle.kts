plugins {
    kotlin("jvm") version "2.3.10"
}

group = "kr.foundcake.super_legend_punch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
    jvmToolchain(25)
}

tasks.named<ProcessResources>("processResources") {
    from("$rootDir/LICENSE") {
        into("META-INF") // 루트 경로에 포함
    }
}
