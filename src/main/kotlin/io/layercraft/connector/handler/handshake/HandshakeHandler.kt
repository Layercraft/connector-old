package io.layercraft.connector.handler.handshake

import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.utils.Connection
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.packets.handshake.data.HandshakeNextState
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import reactor.netty.channel.ChannelOperations

object HandshakeHandler: LocalPacketHandler<Handshake> {
    override fun handle(packet: Handshake, operations: ChannelOperations<*, *>, connection: Connection) {
        connection.packetState = when (packet.nextState) {
            HandshakeNextState.LOGIN -> PacketState.LOGIN
            HandshakeNextState.STATUS -> PacketState.STATUS
        }

        connection.host = packet.address
    }
}
