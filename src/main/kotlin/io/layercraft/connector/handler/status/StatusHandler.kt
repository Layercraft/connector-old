package io.layercraft.connector.handler.status

import io.layercraft.connector.CODEC
import io.layercraft.connector.SERVERID
import io.layercraft.connector.VERSION
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.packetlib.packets.v1_19_2.status.clientbound.ServerInfoPacket
import io.layercraft.packetlib.packets.v1_19_2.status.serverbound.PingStartPacket
import reactor.netty5.channel.ChannelOperations

object StatusHandler : LocalPacketHandler<PingStartPacket> {

    override fun handle(packet: PingStartPacket, operations: ChannelOperations<*, *>, connection: Connection) {

        val json = """
            {"version":{"name":"Layercraft-$VERSION","protocol":${CODEC.protocolVersion.protocolNumber}},"players":{"max":100,"online":0},"description":{"text":"Connector: $SERVERID"},"previewsChat":false,"enforcesSecureChat":false}
        """.trimIndent().trim()

        val response = ServerInfoPacket(json)
        operations.sendMcPacket(CODEC, response).then().subscribe()
    }
}


