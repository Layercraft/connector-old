package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.utils.mc
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.NettyOutbound

internal fun ByteBufFlux.asKtorInput(): Flux<Input> {
    return this.asInputStream().map { ByteReadPacket(it.asInput().readBytes()) }
}

fun NettyOutbound.sendMcPacket(codec: MinecraftCodec, packet: Packet): NettyOutbound {
    println("S -> C: $packet")
    val packetWrite = BytePacketBuilder()
    codec.getCodecPacketFromPacket(packet)?.let { packetWrite.mc.writeVarInt(it.packetId) }
    TranslatorAPI.encodeToOutputWithCodec(packet, codec, packetWrite)
    val bytes = packetWrite.build().readBytes()

    val packetBuilder = BytePacketBuilder()
    packetBuilder.mc.writeVarInt(bytes.size)
    packetBuilder.writeFully(bytes)

    return sendByteArray(Mono.just(packetBuilder.build().readBytes()))
}

