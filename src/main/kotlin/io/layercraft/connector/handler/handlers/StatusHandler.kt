package io.layercraft.connector.handler.handlers

import io.layercraft.connector.codec
import io.layercraft.connector.handler.Handler
import io.layercraft.connector.sendMcPacket
import io.layercraft.translator.packets.status.clientbound.StatusResponse
import io.layercraft.translator.packets.status.serverbound.StatusRequest
import reactor.netty.channel.ChannelOperations

class StatusHandler : Handler<StatusRequest> {

    override fun handle(packet: StatusRequest, operations: ChannelOperations<*, *>) {

        val json = "{\"version\": {\"name\": \"Layercraft Alpha\",\"protocol\": 760},\"players\": {\"max\": 100,\"online\": 0,\"sample\": []},\"description\": {\"text\": \"Hello world to Layercraft Connector\"},\"previewsChat\": false,\"enforcesSecureChat\": false}"

        val response = StatusResponse(json)
        operations.sendMcPacket(codec, response).then().subscribe()
    }
}
