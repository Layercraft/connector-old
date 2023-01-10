package io.layercraft.connector

import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.codec.MinecraftCodecs
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.UUID

val SERVERUUID: UUID = UUID.randomUUID()
val SERVERID = SERVERUUID.toString().replace("-", "").substring(0, 20)
val CODEC: MinecraftCodec = MinecraftCodecs.V1_19_3
const val VERSION: String = "0.0.1"
const val MAXPACKETSIZE: Int = 209715 // 3 bytes varint 2097151

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
