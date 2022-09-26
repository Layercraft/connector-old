package io.layercraft.connector.handler

import io.layercraft.connector.handler.handshake.HandshakeHandler
import io.layercraft.connector.handler.login.LoginStartHandler
import io.layercraft.connector.handler.status.PingHandler
import io.layercraft.connector.handler.status.StatusHandler
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import io.layercraft.translator.packets.login.serverbound.LoginStart
import io.layercraft.translator.packets.status.serverbound.PingRequest
import io.layercraft.translator.packets.status.serverbound.StatusRequest
import reactor.netty.channel.ChannelOperations
import kotlin.reflect.KClass

object LocalHandler {
    // HANDSHAKE, LOGIN and STATUS are handled locally from the connector

    private val list: HashMap<KClass<*>, LocalPacketHandler<out Packet>> = hashMapOf(
        Handshake::class to HandshakeHandler,
        StatusRequest::class to StatusHandler,
        PingRequest::class to PingHandler,
        LoginStart::class to LoginStartHandler
    )

    fun <T: Packet> getHandler(packet: T): LocalPacketHandler<T>? {
        return list[packet::class] as LocalPacketHandler<T>?
    }
}

interface LocalPacketHandler<T: Packet> {
    fun handle(packet: T, operations: ChannelOperations<*, *>)
}
