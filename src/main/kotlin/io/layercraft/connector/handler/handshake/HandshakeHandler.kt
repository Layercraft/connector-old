package io.layercraft.connector.handler.handshake

import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.utils.Connection
import io.layercraft.packetlib.packets.PacketState
import io.layercraft.packetlib.packets.v1_19_2.handshaking.serverbound.SetProtocolPacket
import reactor.netty5.channel.ChannelOperations

object HandshakeHandler: LocalPacketHandler<SetProtocolPacket> {
    override fun handle(packet: SetProtocolPacket, operations: ChannelOperations<*, *>, connection: Connection) {
        connection.packetState = when (packet.nextState) {
            2 -> PacketState.LOGIN
            1 -> PacketState.STATUS
            else -> error("Invalid packet state")
        }

        connection.host = packet.serverHost
    }
}
