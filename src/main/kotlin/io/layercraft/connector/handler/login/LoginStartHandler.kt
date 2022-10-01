package io.layercraft.connector.handler.login

import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.login.playerLogin
import io.layercraft.translator.packets.login.serverbound.LoginStart
import reactor.netty.channel.ChannelOperations

object LoginStartHandler: LocalPacketHandler<LoginStart> {
    override fun handle(packet: LoginStart, operations: ChannelOperations<*, *>) {
        playerLogin {
            this.username = "test"
            this.uuid = "test"
        }
    }
}
