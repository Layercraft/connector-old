package io.layercraft.connector.handler.login

import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.translator.packets.login.serverbound.LoginStart
import reactor.netty.channel.ChannelOperations

object LoginStartHandler: LocalPacketHandler<LoginStart> {
    override fun handle(packet: LoginStart, operations: ChannelOperations<*, *>) {

    }
}
