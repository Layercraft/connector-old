package io.layercraft.connector

import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.codec.MinecraftCodecs
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.UUID

val SERVERID: UUID = UUID.randomUUID()
val CODEC: MinecraftCodec = MinecraftCodecs.V1_19_2
const val VERSION: String = "0.0.1"

val koinModule = module {
    single { Server() }
}

fun main() {
    val koin = startKoin {
        modules(koinModule)
    }

    val server = koin.koin.get<Server>()
    server.start()
    server.block()
}
