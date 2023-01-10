package io.layercraft.connector.handler.status

import io.layercraft.connector.CODEC
import io.layercraft.connector.SERVER_UUID
import io.layercraft.connector.VERSION
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.packetlib.packets.v1_19_3.status.clientbound.ServerInfoPacket
import io.layercraft.packetlib.packets.v1_19_3.status.serverbound.PingStartPacket
import reactor.netty5.channel.ChannelOperations

object StatusHandler : LocalPacketHandler<PingStartPacket> {

    override fun handle(packet: PingStartPacket, operations: ChannelOperations<*, *>, connection: Connection) {
        val json = """
            {"version":{"name":"Layercraft-$VERSION","protocol":${CODEC.protocolVersion.protocolNumber}},"players":{"max":250,"online":10},"description":{"text":"Connector: $SERVER_UUID"},"previewsChat":false,"enforcesSecureChat":false}
        """.trimIndent().trim()

        val response = ServerInfoPacket(json)
        operations.sendMcPacket(CODEC, response).then().subscribe()
    }
}
