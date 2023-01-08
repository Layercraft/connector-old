package io.layercraft.connector.utils

import io.layercraft.packetlib.packets.PacketState
import io.netty5.channel.ChannelId
import reactor.netty5.channel.ChannelOperations
import java.util.UUID

object ConnectionsUtils {

    private val connectionUUIDS = HashMap<String, UUID>()
    private val connections = HashMap<UUID, Connection>()

    fun connectionId(channelId: ChannelId): UUID {
        return connectionUUIDS.getOrPut(channelId.asLongText()) { UUID.randomUUID() }
    }

    fun connection(uuid: UUID): Connection? {
        return connections[uuid]
    }

    fun connection(channelId: ChannelId): Connection? {
        return connections[connectionId(channelId)]
    }

    fun connection(operations: ChannelOperations<*, *>): Connection {
        return connections.getOrPut(connectionId(operations.channel().id())) {
            Connection(
                connectionId(operations.channel().id()),
                operations
            )
        }
    }

    fun removeConnection(channelId: ChannelId) {
        this.connectionUUIDS.remove(channelId.asLongText())
        this.connections.remove(connectionId(channelId))
    }
}

data class Connection(
    val uuid: UUID,
    val operations: ChannelOperations<*, *>,
    var packetState: PacketState = PacketState.HANDSHAKING,
    val verifyToken: ByteArray = EncryptionUtils.generateVerifyToken(),
    var sharedSecret: ByteArray? = null,
    var username: String? = null,
    var mcUUID: UUID? = null,
    var host: String? = null
)
