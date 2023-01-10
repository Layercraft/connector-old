package io.layercraft.connector.handler.status

import io.layercraft.connector.CODEC
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.packetlib.packets.v1_19_3.status.serverbound.PingPacket
import reactor.netty5.channel.ChannelOperations

object PingHandler : LocalPacketHandler<PingPacket> {
    override fun handle(packet: PingPacket, operations: ChannelOperations<*, *>, connection: Connection) {
        val response = io.layercraft.packetlib.packets.v1_19_3.status.clientbound.PingPacket(packet.time)
        operations.sendMcPacket(CODEC, response).then().subscribe()
        Thread.sleep(1000)
        operations.channel().close()
    }
}
