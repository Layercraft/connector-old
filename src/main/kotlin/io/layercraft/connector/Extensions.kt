package io.layercraft.connector

import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.packetlib.TranslatorAPI
import io.layercraft.packetlib.codec.MinecraftCodec
import io.layercraft.packetlib.packets.Packet
import io.layercraft.packetlib.utils.bytebuffer.MinecraftByteBufferSerialize
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.netty5.NettyOutbound
import reactor.netty5.channel.ChannelOperations
import java.nio.ByteBuffer

private val logger = LoggerFactory.getLogger(Server::class.java)

fun ChannelOperations<*, *>.sendMcPacket(codec: MinecraftCodec, packet: Packet): NettyOutbound = this.sendMcPacketReactive(codec, Mono.just(packet))

fun ChannelOperations<*, *>.sendMcPacketReactive(codec: MinecraftCodec, packet: Mono<Packet>): NettyOutbound {
    if (!this.channel().isActive) {
        logger.error("Channel is not active")
        return this
    }

    return this.sendByteArray(
        packet
            .map {
                val buffer = ByteBuffer.allocate(MAXPACKETSIZE)
                val serializer = MinecraftByteBufferSerialize(buffer)
                logger.info("S -> C: ${it.toString().replace("\n", " ")}")
                val codecPacket = codec.getCodecPacketFromPacket(it)!!
                serializer.writeVarInt(codecPacket.packetId)
                TranslatorAPI.encodeToOutputWithCodec(codec, serializer, it)

                val content = ByteArray(buffer.position())
                buffer.get(0, content, 0, content.size)

                buffer.clear()
                serializer.writeVarInt(content.size)
                serializer.writeBytes(content)

                buffer.position(0)

                val cipherContext = ConnectionsUtils.connection(this).cipherContext
                cipherContext?.encrypt?.update(buffer, buffer.duplicate())

                buffer.flip()

                return@map buffer.array()
            },
    )
}
