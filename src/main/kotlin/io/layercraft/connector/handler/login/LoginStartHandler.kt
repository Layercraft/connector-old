package io.layercraft.connector.handler.login

import io.layercraft.connector.CODEC
import io.layercraft.connector.SERVERID
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.packets.v1_19_2.login.clientbound.EncryptionBeginPacket
import io.layercraft.packetlib.packets.v1_19_2.login.serverbound.LoginStartPacket
import reactor.netty5.channel.ChannelOperations
import java.security.PublicKey

object LoginStartHandler: LocalPacketHandler<LoginStartPacket> {
    override fun handle(packet: LoginStartPacket, operations: ChannelOperations<*, *>, connection: Connection) {

        //Bytearray to RSA Public Key
        if (packet.hasSignature){
            val publicKey = packet.publicKey!!
            val signature = packet.signature!!

            val key: PublicKey = EncryptionUtils.byteArrayToPublicKey(publicKey, signature)
        }

        connection.username = packet.username
        if (packet.hasPlayerUUID) connection.mcUUID = packet.playerUUID!!

        val response = EncryptionBeginPacket(
            SERVERID.toString().substring(0, 20),
            EncryptionUtils.publickey,
            connection.verifyToken
        )

        operations.sendMcPacket(CODEC, response).then().subscribe()
    }
}
