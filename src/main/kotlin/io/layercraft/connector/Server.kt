package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.layercraft.connector.handler.LocalHandler
import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.packets.PacketDirection
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.utils.mc
import reactor.core.publisher.Flux
import reactor.netty.DisposableServer
import reactor.netty.channel.ChannelOperations
import reactor.netty.tcp.TcpServer
import kotlin.system.measureTimeMillis


object Server {

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val tcpServer: TcpServer = TcpServer.create()
        .port(25565)
        .handle { inbound, outbound ->
            val channelOperations = inbound as ChannelOperations<*, *>
            val connection = ConnectionsUtils.connection(channelOperations)
            inbound.receive()
                .asKtorInput()
                .map {
                    val sharedSecret = connection.sharedSecret
                    if (sharedSecret != null) {
                        println("Decrypting")
                        ByteReadPacket(EncryptionUtils.decryptBytesAES(it.readBytes(), sharedSecret))
                    } else {
                        it
                    }
                }
                .flatMap {
                    var list: Array<Input> = emptyArray()
                    loop@ do {
                        if (it.endOfInput) break@loop
                        val packetLength = it.mc.readVarInt()
                        if (packetLength > it.remaining) return@flatMap Flux.error(Exception("Packet length is greater than remaining bytes"))
                        list += ByteReadPacket(it.readBytes(packetLength))
                    } while (it.remaining > 0)
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

                    val packetId = it.mc.readVarInt()
                    val packetState = connection.packetState
                    val packet: Packet? = TranslatorAPI.decodeFromInputWithCodec(it, codec, PacketDirection.SERVERBOUND, packetState, packetId)

                    println("C -> S: ${packet.toString().replace("\n", " ")}")
                    if (packetState != PacketState.PLAY) {
                        if (packet != null) {
                            val handler = LocalHandler.getHandler(packet)
                            if (handler != null) {
                                val time = measureTimeMillis {
                                    handler.handle(packet, channelOperations, connection)
                                }
                                println("Handler-${handler::class.simpleName} time: $time ms")
                            } else {
                                println("No handler for packet: $packet")
                            }
                        }
                    }
                }
                .doOnError {
                    it.printStackTrace()
                    println("Error: ${it.message}")
                }
                .then()
        }

        .doOnConnection {
            if (it is ChannelOperations<*, *>) {
                val connection = ConnectionsUtils.connection(it)

                //Add Listener to remove connection from ConnectionsUtils
                it.onDispose {
                    ConnectionsUtils.removeConnection(it.channel().id())
                }

                println("Connection established: ${it.channel().remoteAddress()} (${connection.uuid})")
            } else {
                println("Error: ${it.javaClass}")
                it.channel().close()
            }
        }
        .doOnBound {
            println("Server started on port ${it.port()}")
        }
        .doOnUnbound {
            println("Server stopped")
        }

    fun start() {
        tcpServer.warmup().block()

        val disposableServer: DisposableServer = tcpServer.bindNow()

        disposableServer.onDispose().block()
    }
}
