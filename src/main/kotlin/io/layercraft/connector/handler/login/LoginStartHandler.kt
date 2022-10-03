package io.layercraft.connector.handler.login

import io.layercraft.connector.codec
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.serverID
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.ConnectionsUtils
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.translator.packets.login.clientbound.EncryptionRequest
import io.layercraft.translator.packets.login.serverbound.LoginStart
import reactor.netty.channel.ChannelOperations
import java.security.PublicKey
import java.util.*

object LoginStartHandler: LocalPacketHandler<LoginStart> {
    override fun handle(packet: LoginStart, operations: ChannelOperations<*, *>, connection: Connection) {

        //Bytearray to RSA Public Key
        if (packet.hasSigData){
            val publicKey: PublicKey = EncryptionUtils.byteArrayToPublicKey(packet.publicKey!!, packet.signature!!)
        }

        connection.username = packet.name
        if (packet.hasPlayerUUID) connection.mcUUID = packet.playerUUID!!

        val response = EncryptionRequest(
            serverID.toString().substring(0, 20),
            EncryptionUtils.publickey,
            connection.verifyToken
        )

        operations.sendMcPacket(codec, response).then().subscribe()
    }
}
