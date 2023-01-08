package io.layercraft.connector.handler

import io.layercraft.connector.handler.handshake.HandshakeHandler
import io.layercraft.connector.handler.login.EncryptionResponseHandler
import io.layercraft.connector.handler.login.LoginStartHandler
import io.layercraft.connector.handler.status.PingHandler
import io.layercraft.connector.handler.status.StatusHandler
import io.layercraft.connector.utils.Connection
import io.layercraft.packetlib.packets.Packet
import io.layercraft.packetlib.packets.v1_19_2.handshaking.serverbound.SetProtocolPacket
import io.layercraft.packetlib.packets.v1_19_2.login.serverbound.EncryptionBeginPacket
import io.layercraft.packetlib.packets.v1_19_2.login.serverbound.LoginStartPacket
import io.layercraft.packetlib.packets.v1_19_2.status.serverbound.PingPacket
import io.layercraft.packetlib.packets.v1_19_2.status.serverbound.PingStartPacket
import org.slf4j.LoggerFactory
import reactor.netty5.channel.ChannelOperations
import kotlin.reflect.KClass

object LocalHandler {

    val logger = LoggerFactory.getLogger(LocalHandler::class.java)

    // HANDSHAKE, LOGIN and STATUS are handled locally from the connector

    private val list: HashMap<KClass<*>, LocalPacketHandler<out Packet>> = hashMapOf(
        SetProtocolPacket::class to HandshakeHandler,
        PingStartPacket::class to StatusHandler,
        PingPacket::class to PingHandler,
        LoginStartPacket::class to LoginStartHandler,
        EncryptionBeginPacket::class to EncryptionResponseHandler
    )

    fun <T: Packet> getHandler(packet: T): LocalPacketHandler<T>? {
        if (list.containsKey(packet::class)) {
            @Suppress("UNCHECKED_CAST")
            return list[packet::class] as LocalPacketHandler<T>?
        }
        return null
    }
}

interface LocalPacketHandler<T: Packet> {
    fun handle(packet: T, operations: ChannelOperations<*, *>, connection: Connection)
}
