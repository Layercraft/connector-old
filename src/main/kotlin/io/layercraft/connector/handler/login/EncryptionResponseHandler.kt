package io.layercraft.connector.handler.login

import com.fasterxml.jackson.annotation.JsonProperty
import io.layercraft.connector.CODEC
import io.layercraft.connector.handler.LocalPacketHandler
import io.layercraft.connector.sendMcPacket
import io.layercraft.connector.utils.CipherContext
import io.layercraft.connector.utils.Connection
import io.layercraft.connector.utils.EncryptionUtils
import io.layercraft.packetlib.packets.PacketState
import io.layercraft.packetlib.packets.v1_19_3.login.clientbound.CompressPacket
import io.layercraft.packetlib.packets.v1_19_3.login.clientbound.DisconnectPacket
import io.layercraft.packetlib.packets.v1_19_3.login.clientbound.SuccessPacket
import io.layercraft.packetlib.packets.v1_19_3.login.clientbound.SuccessPacketProperties
import io.layercraft.packetlib.packets.v1_19_3.login.serverbound.EncryptionBeginPacket
import io.layercraft.packetlib.packets.v1_19_3.play.clientbound.LoginPacket
import org.koin.java.KoinJavaComponent.inject
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty5.channel.ChannelOperations
import java.util.UUID
import javax.crypto.Cipher

object EncryptionResponseHandler : LocalPacketHandler<EncryptionBeginPacket> {

    private val webClient = WebClient.create("https://sessionserver.mojang.com")
    private val encryptionUtils: EncryptionUtils by inject(EncryptionUtils::class.java)

    override fun handle(packet: EncryptionBeginPacket, operations: ChannelOperations<*, *>, connection: Connection) {
        val decryptedSharedSecret = encryptionUtils.decryptByteToSecretKey(packet.sharedSecret)
        connection.sharedSecret = decryptedSharedSecret
        val decrypt = encryptionUtils.getCipher(Cipher.DECRYPT_MODE, decryptedSharedSecret)
        val encrypt = encryptionUtils.getCipher(Cipher.ENCRYPT_MODE, decryptedSharedSecret)

        val cipherContext = CipherContext(decrypt, encrypt)
        connection.cipherContext = cipherContext

        val serverHash = encryptionUtils.genSha1Hash(decryptedSharedSecret)
        val verifyToken = connection.verifyToken

        val decryptedVerifyToken = encryptionUtils.decryptBytesRSA(packet.verifyToken)
        if (!decryptedVerifyToken.contentEquals(verifyToken)) {
            println("Verify token is not equal to decrypted verify token")
            println(decryptedVerifyToken.contentToString())
            println(verifyToken.contentToString())
            operations.sendMcPacket(CODEC, DisconnectPacket("Verify token is not equal to decrypted verify token")).then().subscribe()
            operations.channel().close()
            return
        }

        val compression = CompressPacket(-1)
        operations.sendMcPacket(CODEC, compression).then().subscribe()

        // GET https://sessionserver.mojang.com/session/minecraft/hasJoined?username=username&serverId=hash&ip=ip
        // Use HTTPClient to get the response from the Mojang API
        webClient
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

                // UUID add dashes
                val uuid = UUID.fromString(webResponse.id.replace(Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "$1-$2-$3-$4-$5"))

                val response = SuccessPacket(
                    uuid,
                    webResponse.name,
                    list,
                )

                operations.sendMcPacket(CODEC, response).then().subscribe()

                connection.packetState = PacketState.PLAY

                val send = LoginPacket(
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

                operations.sendMcPacket(CODEC, send).then().subscribe()
            }
    }
}

data class MojangResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("properties") val properties: List<MojangProperty>,
)

data class MojangProperty(
    @JsonProperty("name") val name: String,
    @JsonProperty("value") val value: String,
    @JsonProperty("signature") val signature: String?,
)
