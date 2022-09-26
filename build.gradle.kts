import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "1.7.10"
    application

    id("com.google.protobuf") version "0.8.19"
    id("idea")
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

    //Netty
    implementation("io.projectreactor:reactor-core:3.5.0-M6")
    implementation("io.projectreactor:reactor-tools:3.5.0-M6")
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.0-M6")

    // If Platform is osx-aarch_64, use io.netty:netty-resolver-dns-native-macos:4.1.82.Final:osx-aarch_64
    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
        implementation("io.netty:netty-resolver-dns-native-macos:4.1.82.Final:osx-aarch_64")
    }

    //Ktor
    implementation("io.ktor:ktor-io-jvm:2.1.1")

    //Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:3.21.6")
    protobuf(files("universal-packets/io/layercraft/"))

    //Tests
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.projectreactor:reactor-test:3.5.0-M6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

application {
    mainClass.set("io.layercraft.connector.ApplicationKt")
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.21.6"
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
        }
    }
}

idea {
    module {
        sourceDirs.plusAssign(file("build/generated/source/proto/main/kotlin"))
    }
}
