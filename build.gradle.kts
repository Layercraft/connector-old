/*import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc*/
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"

    application

    // id("com.google.protobuf") version "0.8.19"
    id("idea")
}

group = "io.layercraft.connector"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

// Versions
val projectReactorVersion = "3.5.1"
val koinVersion = "3.3.2"
val springVersion = "5.3.13"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
    maven(url = "https://repo.spring.io/milestone")
}

dependencies {
    implementation("com.github.layercraft:packetlib:0.0.36")

    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Netty
    implementation("io.projectreactor:reactor-core:$projectReactorVersion")
    implementation("io.projectreactor:reactor-tools:$projectReactorVersion")
    testImplementation("io.projectreactor:reactor-test:$projectReactorVersion")

    implementation("io.projectreactor.netty:reactor-netty5:2.0.0-M3")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.86.Final:osx-aarch_64")

    // Google
    implementation("com.google.code.gson:gson:2.10")

    // RabbitMQ
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.5")

    // Koin
    implementation("io.insert-koin:koin-core:$koinVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.0.1") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    implementation("org.springframework.boot:spring-boot-starter-logging:3.0.1")

    // Tests
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xuse-k2")
    }
}

application {
    mainClass.set("io.layercraft.connector.ApplicationKt")
}
/*

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
        sourceDirs.plusAssign(file("build/generated/source/proto/main/"))
    }
}
*/
