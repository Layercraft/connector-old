package io.layercraft.connector

import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.packetlib.TranslatorAPI
import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.packets.Packet
import io.layercraft.packetlib.utils.bytebuffer.MinecraftByteBufferSerialize
import io.layercraft.packetlib.utils.stream.MinecraftOutputStreamSerialize
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.netty5.NettyOutbound
import reactor.netty5.channel.ChannelOperations
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

private val logger = LoggerFactory.getLogger(Server::class.java)

fun ChannelOperations<*, *>.sendMcPacket(codec: MinecraftCodec, packet: Packet): NettyOutbound = this.sendMcPacketReactiveStream(codec, Mono.just(packet))

fun ChannelOperations<*, *>.sendMcPacketReactiveByteBuffer(codec: MinecraftCodec, packet: Mono<Packet>): NettyOutbound {
    if (!this.channel().isActive) {
        logger.error("Channel is not active")
        return this
    }

    return this.sendByteArray(
        packet
            .map {
                logger.info("S -> C: ${it.toString().replace("\n", " ")}")

                val buffer: ByteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE)
                val serializer = MinecraftByteBufferSerialize(buffer)
                val codecPacket = codec.getCodecPacketFromPacket(it)!!
                serializer.writeVarInt(codecPacket.packetId)
                TranslatorAPI.encodeToOutputWithCodec(codec, serializer, it)

                val content = ByteArray(buffer.position())
                buffer.get(0, content, 0, content.size)

                buffer.clear()
                serializer.writeVarInt(content.size)
                serializer.writeBytes(content)

                buffer.flip()

                val cipherContext = ConnectionsUtils.connection(this).cipherContext
                cipherContext?.encrypt?.update(buffer, buffer.duplicate())

                buffer.flip()

                return@map buffer.array()
            },
    )
}


fun ChannelOperations<*, *>.sendMcPacketReactiveStream(codec: MinecraftCodec, packet: Mono<Packet>): NettyOutbound {
    if (!this.channel().isActive) {
        logger.error("Channel is not active")
        return this
    }

    return this.sendByteArray(
        packet
            .map {
                logger.info("S -> C: ${it.toString().replace("\n", " ")}")
                val byteArrayOutputStream = ByteArrayOutputStream()
                val dataOutputStream = DataOutputStream(byteArrayOutputStream)
                val serializer = MinecraftOutputStreamSerialize(dataOutputStream)
                val codecPacket = codec.getCodecPacketFromPacket(it)!!
                serializer.writeVarInt(codecPacket.packetId)
                TranslatorAPI.encodeToOutputWithCodec(codec, serializer, it)

                return@map byteArrayOutputStream.toByteArray()
            }
            .map {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val dataOutputStream = DataOutputStream(byteArrayOutputStream)
                val serializer = MinecraftOutputStreamSerialize(dataOutputStream)

                serializer.writeVarInt(it.size)
                serializer.writeBytes(it)

                val cipherContext = ConnectionsUtils.connection(this).cipherContext
                if (cipherContext != null) {
                    return@map cipherContext.encrypt.update(byteArrayOutputStream.toByteArray())
                } else {
                    return@map byteArrayOutputStream.toByteArray()
                }
            },
    )
}
