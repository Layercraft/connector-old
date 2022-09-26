package io.layercraft.connector.handler.status

import io.layercraft.connector.codec
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.translator.packets.status.serverbound.PingRequest
import reactor.netty.channel.ChannelOperations

object PingHandler: LocalPacketHandler<PingRequest> {
    override fun handle(packet: PingRequest, operations: ChannelOperations<*, *>) {
        val response = PingRequest(packet.payload)
        operations.sendMcPacket(codec, response).then().subscribe()
        operations.channel().close()
    }
}
