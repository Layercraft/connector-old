package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.layercraft.connector.handler.LocalHandler
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.packets.PacketDirection
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.utils.mc
import reactor.core.publisher.Flux
import reactor.netty.DisposableServer
import reactor.netty.channel.ChannelOperations
import reactor.netty.tcp.TcpServer


object Server {

    private val tcpServer: TcpServer = TcpServer.create()
        .port(25565)
        .handle { inbound, outbound ->
            val channelOperations = inbound as ChannelOperations<*, *>
            val id = channelOperations.channel().id()
            inbound.receive()
                .asKtorInput()
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
                    val packetId = it.mc.readVarInt()
                    val packetState = status.getOrDefault(id.asLongText(), PacketState.HANDSHAKE)
                    val packet: Packet? = TranslatorAPI.decodeFromInputWithCodec(it, codec, PacketDirection.SERVERBOUND, packetState, packetId)

                    println("C -> S: $packet")
                    if (status[id.asLongText()] != PacketState.PLAY) {
                        if (packet != null) {
                            LocalHandler.getHandler(packet)?.handle(packet, channelOperations)
                        }
                    }
                }
                .doOnError {
                    println("Error: ${it.message}")
                }
                .then()
        }

        .doOnConnection {
            val id = it.channel().id().asLongText()
            status[id] = PacketState.HANDSHAKE

            println("Established connection with ${it.channel().remoteAddress()}")
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
