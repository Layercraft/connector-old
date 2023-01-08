package io.layercraft.connector

import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.TranslatorAPI
import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.packets.Packet
import io.layercraft.packetlib.utils.stream.MinecraftOutputStreamSerialize
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.netty5.NettyOutbound
import reactor.netty5.channel.ChannelOperations
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

private val logger = LoggerFactory.getLogger(Server::class.java)

fun ChannelOperations<*, *>.sendMcPacket(codec: MinecraftCodec, packet: Packet): NettyOutbound = this.sendMcPacketReactive(codec, Mono.just(packet))

fun ChannelOperations<*, *>.sendMcPacketReactive(codec: MinecraftCodec, packet: Mono<Packet>): NettyOutbound =
    this.sendByteArray(
        packet
        .map {
            val byteStream = ByteArrayOutputStream()
            val dataStream = DataOutputStream(byteStream)
            val serializer = MinecraftOutputStreamSerialize(dataStream)
            logger.info("S -> C: ${it.toString().replace("\n", " ")}")
            codec.getCodecPacketFromPacket(it)?.let { codecPacket -> serializer.writeVarInt(codecPacket.packetId) }
            TranslatorAPI.encodeToOutputWithCodec(codec, serializer, it)
            return@map byteStream.toByteArray()
        }
        .map {
            val byteStream = ByteArrayOutputStream()
            val dataStream = DataOutputStream(byteStream)
            val serializer = MinecraftOutputStreamSerialize(dataStream)
            serializer.writeVarInt(it.size)
            serializer.writeBytes(it)
            val bytes = byteStream.toByteArray()

            val sharedSecret = ConnectionsUtils.connection(this).sharedSecret

            if (sharedSecret != null) {
                EncryptionUtils.encryptBytesAES(bytes, sharedSecret)
            } else {
                bytes
            }
        })
