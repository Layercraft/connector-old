package io.layercraft.connector

import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.codec.MinecraftCodecs
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.UUID

val SERVER_UUID: UUID = UUID.randomUUID()
val SERVER_ID = SERVER_UUID.toString().replace("-", "").substring(0, 20)
val CODEC: MinecraftCodec = MinecraftCodecs.V1_19_3
const val VERSION: String = "0.0.1"
const val MAX_PACKET_SIZE: Int = 2097151 // 3 bytes varint 2097151

val koinModule = module {
    single { Server() }
    single { EncryptionUtils() }
}

fun main() {
    val koin = startKoin {
        modules(koinModule)
    }

    val server = koin.koin.get<Server>()
    server.start()
    server.block()
}
