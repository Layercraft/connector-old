package io.layercraft.connector.handler.login

import io.layercraft.connector.CODEC
import io.layercraft.connector.SERVER_ID
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.packets.v1_19_3.login.clientbound.EncryptionBeginPacket
import io.layercraft.packetlib.packets.v1_19_3.login.serverbound.LoginStartPacket
import org.koin.java.KoinJavaComponent
import reactor.netty5.channel.ChannelOperations

object LoginStartHandler : LocalPacketHandler<LoginStartPacket> {

    private val encryptionUtils: EncryptionUtils by KoinJavaComponent.inject(EncryptionUtils::class.java)

    override fun handle(packet: LoginStartPacket, operations: ChannelOperations<*, *>, connection: Connection) {
        connection.username = packet.username
        if (packet.hasPlayerUUID) connection.mcUUID = packet.playerUUID!!

        val response = EncryptionBeginPacket(
            SERVER_ID,
            encryptionUtils.publicKey.encoded,
            connection.verifyToken,
        )

        operations.sendMcPacket(CODEC, response).then().subscribe()
    }
}
