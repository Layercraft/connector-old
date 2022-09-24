package io.layercraft.connector.handler.handlers

import io.ktor.utils.io.core.*
import io.layercraft.connector.codec
import io.layercraft.connector.handler.Handler
import io.layercraft.connector.sendMcPacket
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodecs
import io.layercraft.translator.packets.status.serverbound.PingRequest
import reactor.core.publisher.Mono
import reactor.netty.NettyOutbound

class PingHandler: Handler<PingRequest> {
    override fun handle(packet: PingRequest, outbound: NettyOutbound, hash: Int) {
        println("Ping request received from $hash")
        val response = PingRequest(packet.payload)

        outbound.sendMcPacket(codec, response).then().subscribe()
        //Close connection
    }
}
