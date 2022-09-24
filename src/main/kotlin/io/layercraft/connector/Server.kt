package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.layercraft.connector.handler.LocalHandler
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.codec.MinecraftCodecs
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.packets.PacketDirection
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.utils.mc
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.DisposableServer
import reactor.netty.NettyOutbound
import reactor.netty.tcp.TcpServer

val codec: MinecraftCodec = MinecraftCodecs.V_1_19_2

val status: HashMap<Int, PacketState> = HashMap()

object Server {

    private val tcpServer: TcpServer = TcpServer.create()
        .port(25565)
        .handle { inbound, outbound ->
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
                    Flux.fromArray(list)
                }
                .doOnNext {
                    val packetId = it.mc.readVarInt()
                    val packetState = status.getOrDefault(inbound.hashCode(), PacketState.HANDSHAKE)
                    val packet: Packet? = TranslatorAPI.decodeFromInputWithCodec(it, codec, PacketDirection.SERVERBOUND, packetState, packetId)

                    println("C -> S: $packet")
                    if (status[inbound.hashCode()] != PacketState.PLAY) {
                        if (packet != null) {
                            LocalHandler.getHandler(packet)?.handle(packet, outbound, inbound.hashCode())
                        }
                    }
                }
                .doOnError {
                    println("Error: ${it.message}")
                }
                .then()
        }

        .doOnConnection { println("Connection established") }
        .doOnBound { println("Bound to port") }
        .doOnUnbound { println("Unbound from port") }

    fun start() {
        tcpServer.warmup().block()

        val disposableServer: DisposableServer = tcpServer.bindNow()

        disposableServer.onDispose().block()
    }

}

private fun ByteBufFlux.asKtorInput(): Flux<Input> {
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
