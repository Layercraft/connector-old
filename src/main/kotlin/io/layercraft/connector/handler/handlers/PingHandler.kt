package io.layercraft.connector.handler.handlers

import io.layercraft.connector.codec
import io.layercraft.connector.handler.Handler
import io.layercraft.connector.sendMcPacket
import io.layercraft.translator.packets.status.serverbound.PingRequest
import reactor.netty.channel.ChannelOperations

class PingHandler: Handler<PingRequest> {
    override fun handle(packet: PingRequest, operations: ChannelOperations<*, *>) {
        val response = PingRequest(packet.payload)
        operations.sendMcPacket(codec, response).then().subscribe()
        operations.channel().close()
    }
}
