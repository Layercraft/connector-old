package io.layercraft.connector.handler.status

import io.layercraft.connector.codec
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.serverID
import io.layercraft.connector.utils.Connection
import io.layercraft.translator.packets.status.clientbound.StatusResponse
import io.layercraft.translator.packets.status.serverbound.StatusRequest
import reactor.netty.channel.ChannelOperations

object StatusHandler : LocalPacketHandler<StatusRequest> {

    override fun handle(packet: StatusRequest, operations: ChannelOperations<*, *>, connection: Connection) {

        val json = "{\"version\": {\"name\": \"Layercraft Alpha\",\"protocol\": 760},\"players\": {\"max\": 100,\"online\": 0,\"sample\": []},\"description\": {\"text\": \"Connector:\n$serverID\"},\"previewsChat\": false,\"enforcesSecureChat\": false}"

        val response = StatusResponse(json)
        operations.sendMcPacket(codec, response).then().subscribe()
    }
}
