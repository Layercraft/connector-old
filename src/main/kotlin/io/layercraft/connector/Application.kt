package io.layercraft.connector

import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.codec.MinecraftCodecs
import io.layercraft.translator.packets.PacketState

val codec: MinecraftCodec = MinecraftCodecs.V_1_19_2

val status: HashMap<String, PacketState> = HashMap()
val compression: HashMap<String, Boolean> = HashMap<String, Boolean>()

fun main(args: Array<String>) {
    Server.start()
}
