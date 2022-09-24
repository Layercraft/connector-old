package io.layercraft.connector.handler.handlers

import io.layercraft.connector.handler.Handler
import io.layercraft.connector.status
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.packets.handshake.data.HandshakeNextState
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import reactor.netty.Connection
import reactor.netty.NettyOutbound
import reactor.netty.tcp.TcpServer

class HandshakeHandler: Handler<Handshake> {
    override fun handle(packet: Handshake, outbound: NettyOutbound, hash: Int) {
        println("Changing state to ${packet.nextState}")
        status[hash] = when (packet.nextState) {
            HandshakeNextState.LOGIN -> PacketState.LOGIN
            HandshakeNextState.STATUS -> PacketState.STATUS
        }
    }
}
