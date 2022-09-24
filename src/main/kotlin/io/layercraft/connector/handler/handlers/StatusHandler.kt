package io.layercraft.connector.handler.handlers

import io.ktor.utils.io.core.*
import io.layercraft.connector.codec
import io.layercraft.connector.handler.Handler
import io.layercraft.connector.sendMcPacket
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodecs
import io.layercraft.translator.packets.status.clientbound.StatusResponse
import io.layercraft.translator.packets.status.serverbound.StatusRequest
import reactor.core.publisher.Mono
import reactor.netty.NettyOutbound

class StatusHandler : Handler<StatusRequest> {

    override fun handle(packet: StatusRequest, outbound: NettyOutbound, hash: Int) {

        val json = "{\"version\": {\"name\": \"2.0.0-ALPHA\",\"protocol\": 761},\"players\": {\"max\": 10000000000000000,\"online\": 10000000000000000,\"sample\": []},\"description\": {\"text\": \"Hello world\"},\"previewsChat\": true,\"enforcesSecureChat\": true}"

        val response = StatusResponse(json)

        outbound.sendMcPacket(codec, response).then().subscribe()
    }
}
