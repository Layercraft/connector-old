package io.layercraft.connector

import io.layercraft.connector.handler.LocalHandler
import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.packetlib.TranslatorAPI
import io.layercraft.packetlib.packets.Packet
import io.layercraft.packetlib.packets.PacketDirection
import io.layercraft.packetlib.packets.PacketState
import io.layercraft.packetlib.serialization.MinecraftProtocolDeserializeInterface
import io.layercraft.packetlib.utils.bytebuffer.MinecraftByteBufferDeserialize
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.netty5.DisposableServer
import reactor.netty5.NettyInbound
import reactor.netty5.NettyOutbound
import reactor.netty5.channel.ChannelOperations
import reactor.netty5.tcp.TcpServer
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis

class Server {

    private val logger = LoggerFactory.getLogger(Server::class.java)
    private lateinit var disposableServer: DisposableServer

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val tcpServer: TcpServer = TcpServer.create()
        .port(25565)
        .handle { inbound, outbound -> handle(inbound, outbound) }
        .doOnConnection {
            if (it is ChannelOperations<*, *>) {
                val connection = ConnectionsUtils.connection(it)

                // Add Listener to remove connection from ConnectionsUtils
                it.onDispose {
                    logger.info("Connection closed: ${it.channel().remoteAddress()} (${connection.uuid})")
                    ConnectionsUtils.removeConnection(it.channel().id())
                }

                logger.info("Connection established: ${it.channel().remoteAddress()} (${connection.uuid})")
            } else {
                logger.error("Error: ${it.javaClass}")
                it.channel().close()
            }
        }
        .doOnBound {
            logger.info("Server started on port ${it.port()}")
        }
        .doOnUnbound {
            logger.info("Server stopped")
        }

    fun start() {
        tcpServer.warmup().block()

        disposableServer = tcpServer.bindNow()

        // disposableServer.onDispose().block()
    }

    fun block() {
        disposableServer.onDispose().block()
    }

    private fun handle(inbound: NettyInbound, outbound: NettyOutbound): Publisher<Void> {
        val channelOperations = inbound as ChannelOperations<*, *>
        val connection = ConnectionsUtils.connection(channelOperations)
        return inbound.receive()
            .asByteBuffer()
            .log()
            .map {
                val cipherContext = connection.cipherContext

                if (cipherContext != null) {
                    cipherContext.decrypt.update(it, it.duplicate())
                    println("Running cipher")
                }

                it
            }
            .flatMap {
                var list: Array<ByteBuffer> = emptyArray()
                loop@ do {
                    if (!it.hasRemaining()) break@loop
                    val packetLength = readVarIntFromByteBuffer(it)
                    if (packetLength > it.remaining()) return@flatMap Flux.error(Exception("Packet length is greater than remaining bytes $packetLength > ${it.remaining()}"))
                    val bytebuffer = ByteBuffer.allocate(packetLength)
                    it.get(bytebuffer.array(), 0, packetLength)
                    list += bytebuffer
                } while (it.remaining() > 0)
                return@flatMap Flux.fromArray(list)
            }
            .doOnNext {
                /*if (compression[id.asLongText()] == true) {
                    val uncompressedLength = it.mc.readVarInt()
                    val compressed = it.readBytes(it.remaining.toInt())
                    //zlib decompress
                    val uncompressed = ByteArray(uncompressedLength)
                    val inflater = Inflater()
                    inflater.setInput(compressed)
                    inflater.inflate(uncompressed, 0, uncompressedLength)
                    inflater.end()
                    it.release()
                    //it = ByteReadPacket(uncompressed)
                }*/

                val packetId = readVarIntFromByteBuffer(it)
                val packetState = connection.packetState
                val deserialize: MinecraftProtocolDeserializeInterface<*> = MinecraftByteBufferDeserialize(it)
                val packet: Packet? = TranslatorAPI.decodeFromInputWithCodec(CODEC, deserialize, PacketDirection.SERVERBOUND, packetState, packetId)

                if (packet == null) {
                    logger.warn("Unknown packet id $packetId in state $packetState")
                    return@doOnNext
                }

                logger.info("C -> S: ${packet.toString().replace("\n", " ")}")
                if (packetState != PacketState.PLAY) {
                    val handler = LocalHandler.getHandler(packet)
                    if (handler != null) {
                        val time = measureTimeMillis {
                            handler.handle(packet, channelOperations, connection)
                        }
                        logger.debug("Handled ${handler.javaClass.simpleName} in $time ms")
                    } else {
                        logger.debug("No handler for ${packet.javaClass.simpleName}")
                    }
                }
            }
            .doOnError { logger.error("Error:", it) }
            .then()
    }

    private fun readVarIntFromByteBuffer(byteBuffer: ByteBuffer): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = byteBuffer.get().toInt()
            i = i or (k and 127 shl j++ * 7)
            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }
            if (k and 128 != 128) {
                break
            }
        }
        return i
    }
}
