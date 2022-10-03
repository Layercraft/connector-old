package io.layercraft.connector

import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.codec.MinecraftCodecs
import java.util.*

val serverID: UUID = UUID.randomUUID()

val codec: MinecraftCodec = MinecraftCodecs.V_1_19_2

fun main() {
    Server.start()
}
