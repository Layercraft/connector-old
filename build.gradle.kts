import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"

    application
}

group = "io.layercraft.connector"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url="https://repo.spring.io/milestone")
    maven {
        url = uri("https://maven.pkg.github.com/Layercraft/repo")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("io.layercraft.connector:translator-api:0.0.8")

    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")x

    implementation("io.projectreactor:reactor-core:3.5.0-M6")
    implementation("io.projectreactor:reactor-tools:3.5.0-M6")
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.0-M6")

    implementation("io.netty:netty-resolver-dns-native-macos:4.1.82.Final:osx-aarch_64")

    implementation("io.ktor:ktor-io-jvm:2.1.1")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.projectreactor:reactor-test:3.5.0-M6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

application {
    mainClass.set("io.layercraft.connector.ApplicationKt")
}
