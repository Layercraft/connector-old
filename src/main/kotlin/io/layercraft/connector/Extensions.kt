package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.utils.mc
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.NettyOutbound
import reactor.netty.channel.ChannelOperations

internal fun ByteBufFlux.asKtorInput(): Flux<Input> {
    return this.asInputStream().map { ByteReadPacket(it.asInput().readBytes()) }
}

fun ChannelOperations<*, *>.sendMcPacket(codec: MinecraftCodec, packet: Packet): NettyOutbound = this.sendMcPacketReactive(codec, Mono.just(packet))

fun ChannelOperations<*, *>.sendMcPacketReactive(codec: MinecraftCodec, packet: Mono<Packet>): NettyOutbound {
    val packetPublish = packet
        .doOnNext {
            println("S -> C: ${it.toString().replace("\n", " ")}")
        }
        .map {
            val packetWrite = BytePacketBuilder()
            codec.getCodecPacketFromPacket(it)?.let { codecPacket -> packetWrite.mc.writeVarInt(codecPacket.packetId) }
            TranslatorAPI.encodeToOutputWithCodec(it, codec, packetWrite)
            packetWrite.build().readBytes()
        }
        .map {
            val packetBuilder = BytePacketBuilder()
            packetBuilder.mc.writeVarInt(it.size)
            packetBuilder.writeFully(it)
            packetBuilder.build().readBytes()
        }
        .map {
            val sharedSecret = ConnectionsUtils.connection(this).sharedSecret

            if (sharedSecret != null) {
                println("Not null")
                EncryptionUtils.encryptBytesAES(it, sharedSecret)
            } else {
                it
            }
        }

    return this.sendByteArray(packetPublish)
}

