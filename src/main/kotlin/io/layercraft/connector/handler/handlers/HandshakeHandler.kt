package io.layercraft.connector.handler.handlers

import io.layercraft.connector.handler.Handler
import io.layercraft.connector.status
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.packets.handshake.data.HandshakeNextState
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import reactor.netty.channel.ChannelOperations

class HandshakeHandler: Handler<Handshake> {
    override fun handle(packet: Handshake, operations: ChannelOperations<*, *>) {
        status[operations.channel().id().asLongText()] = when (packet.nextState) {
            HandshakeNextState.LOGIN -> PacketState.LOGIN
            HandshakeNextState.STATUS -> PacketState.STATUS
        }
    }
}
