package io.layercraft.connector.handler

import io.layercraft.connector.handler.handlers.HandshakeHandler
import io.layercraft.connector.handler.handlers.PingHandler
import io.layercraft.connector.handler.handlers.StatusHandler
import io.layercraft.translator.packets.Packet
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import io.layercraft.translator.packets.status.serverbound.PingRequest
import io.layercraft.translator.packets.status.serverbound.StatusRequest
import reactor.netty.channel.ChannelOperations
import kotlin.reflect.KClass

object LocalHandler {
    // HANDSHAKE, LOGIN and STATUS are handled locally from the connector

    private val list: HashMap<KClass<*>, Handler<out Packet>> = hashMapOf(
        Handshake::class to HandshakeHandler(),
        StatusRequest::class to StatusHandler(),
        PingRequest::class to PingHandler()
    )

    fun <T: Packet> getHandler(packet: T): Handler<T>? {
        return list[packet::class] as Handler<T>?
    }
}

interface Handler<T: Packet> {
    fun handle(packet: T, operations: ChannelOperations<*, *>)
}
