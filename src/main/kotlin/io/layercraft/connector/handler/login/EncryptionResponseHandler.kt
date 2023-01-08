package io.layercraft.connector.handler.login

import com.fasterxml.jackson.annotation.JsonProperty
import io.layercraft.connector.CODEC
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.packets.PacketState
import io.layercraft.packetlib.packets.v1_19_2.login.clientbound.SuccessPacket
import io.layercraft.packetlib.packets.v1_19_2.login.serverbound.EncryptionBeginPacket
import io.layercraft.packetlib.packets.v1_19_2.play.clientbound.LoginPacket
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty5.channel.ChannelOperations
import java.util.UUID

object EncryptionResponseHandler : LocalPacketHandler<EncryptionBeginPacket> {

    private val webClient = WebClient.create("https://sessionserver.mojang.com")

    override fun handle(packet: EncryptionBeginPacket, operations: ChannelOperations<*, *>, connection: Connection) {

        val decryptedSharedSecret = EncryptionUtils.decryptBytesRSA(packet.sharedSecret)
        connection.sharedSecret = decryptedSharedSecret
        if (packet.hasVerifyToken) {
            val verifyToken = packet.verifyToken!!

            val decryptedVerifyToken = EncryptionUtils.decryptBytesRSA(verifyToken)
            if (!decryptedVerifyToken.contentEquals(verifyToken)) {
                operations.channel().close()
                return
            }
        } else {
            val salt = packet.salt!!
            val messageSignature = packet.messageSignature!!


        }
        val serverHash = EncryptionUtils.genSha1Hash(decryptedSharedSecret)

        //GET https://sessionserver.mojang.com/session/minecraft/hasJoined?username=username&serverId=hash&ip=ip
        //Use HTTPClient to get the response from the Mojang API
        /*webClient
            .get()
            .uri {
                it
                    .path("session/minecraft/hasJoined")
                    .queryParam("username", connection.username)
                    .queryParam("serverId", serverHash)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(MojangResponse::class.java)
            .subscribe { webResponse ->
                val list = webResponse.properties.map { SuccessPacketProperties(it.name, it.value, it.signature != null, it.signature) }

                //UUID add dashes
                val uuid = UUID.fromString(webResponse.id.replace(Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "$1-$2-$3-$4-$5"))


                val response = SuccessPacket(
                    uuid,
                    webResponse.name,
                    emptyList(),
                )

                operations.sendMcPacket(CODEC, response).then().subscribe()

                connection.packetState = PacketState.PLAY

                val packet = LoginPacket(
                    entityId = 1,
                    isHardcore = false,
                    gameMode = 0u,
                    previousGameMode = 0,
                    worldNames = listOf("world"),
                    dimensionCodec = byteArrayOf(),
                    worldType = "default",
                    worldName = "world",
                    hashedSeed = 0L,
                    maxPlayers = 100,
                    viewDistance = 10,
                    simulationDistance = 10,
                    reducedDebugInfo = false,
                    enableRespawnScreen = true,
                    isDebug = true,
                    isFlat = false,
                    hasDeath = false,
                    dimensionName = "minecraft:overworld",
                    location = null,
                )

                operations.sendMcPacket(CODEC, packet).then().subscribe()
            }*/

        Thread.sleep(5000)

        val response = SuccessPacket(
            UUID.fromString("4cbd42a0-4c2e-453b-8ca8-be32c14bdd2c"),
            "Newspicel",
            emptyList(),
        )

        operations.sendMcPacket(CODEC, response).then().subscribe()

        connection.packetState = PacketState.PLAY

        val login = LoginPacket(
            entityId = 1,
            isHardcore = false,
            gameMode = 0u,
            previousGameMode = 0,
            worldNames = listOf("world"),
            dimensionCodec = byteArrayOf(),
            worldType = "default",
            worldName = "world",
            hashedSeed = 0L,
            maxPlayers = 100,
            viewDistance = 10,
            simulationDistance = 10,
            reducedDebugInfo = false,
            enableRespawnScreen = true,
            isDebug = true,
            isFlat = false,
            hasDeath = false,
            dimensionName = "minecraft:overworld",
            location = null,
        )

        operations.sendMcPacket(CODEC, login).then().subscribe()
    }
}

data class MojangResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("properties") val properties: List<MojangProperty>
)

data class MojangProperty(
    @JsonProperty("name") val name: String,
    @JsonProperty("value") val value: String,
    @JsonProperty("signature") val signature: String?
)
