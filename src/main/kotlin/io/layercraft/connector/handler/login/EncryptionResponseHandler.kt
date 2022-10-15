package io.layercraft.connector.handler.login

import com.google.gson.Gson
import io.layercraft.connector.codec
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.packets.login.clientbound.LoginProperty
import io.layercraft.translator.packets.login.clientbound.LoginSuccess
import io.layercraft.translator.packets.login.serverbound.EncryptionResponse
import reactor.netty.channel.ChannelOperations
import java.net.URI
import java.net.http.HttpClient
import java.util.*

object EncryptionResponseHandler : LocalPacketHandler<EncryptionResponse> {

    private val httpClient = HttpClient.newBuilder().build()
    private val gson = Gson()

    override fun handle(packet: EncryptionResponse, operations: ChannelOperations<*, *>, connection: Connection) {

        println("EncryptionResponseHandler")

        val decryptedSharedSecret = EncryptionUtils.decryptBytesRSA(packet.sharedSecret)
        connection.sharedSecret = decryptedSharedSecret
        println(packet.hasVerifyToken)
        if (packet.hasVerifyToken) {
            val decryptedVerifyToken = EncryptionUtils.decryptBytesRSA(packet.verifyToken!!)
            if (!decryptedVerifyToken.contentEquals(connection.verifyToken)) {
                operations.channel().close()
                return
            }
        }
        val sha = EncryptionUtils.genSha1Hash(decryptedSharedSecret)

        // GET https://sessionserver.mojang.com/session/minecraft/hasJoined?username=username&serverId=hash&ip=ip
        //Use HTTPClient to get the response from the Mojang API
        val request = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=${connection.username}&serverId=$sha"))
            .build()
        val httpResponse = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())

        val json = httpResponse.body()

        //Parse the JSON response
        val jsonResponse = gson.fromJson(json, MojangResponse::class.java)

        val list = ArrayList<LoginProperty>()
        jsonResponse.properties.forEach{
            println("Property: $it")
            list.add(LoginProperty(it.name, it.value, true, it.signature))
        }

        //UUID add dashes
        val id = jsonResponse.id.replace(Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "$1-$2-$3-$4-$5")

        println("UUID: $id")

        val uuid = UUID.fromString(id)

        val response = LoginSuccess(
            uuid,
            jsonResponse.name,
            list
        )

        operations.sendMcPacket(codec, response).then().subscribe()

        connection.packetState = PacketState.PLAY
    }
}

data class MojangResponse(
    val id: String,
    val name: String,
    val properties: List<MojangProperty>
)

data class MojangProperty(
    val name: String,
    val value: String,
    val signature: String
)
