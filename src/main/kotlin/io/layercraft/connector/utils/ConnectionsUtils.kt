package io.layercraft.connector.utils

import io.layercraft.translator.packets.PacketState
import io.netty.channel.ChannelId
import reactor.netty.channel.ChannelOperations
import java.util.*

object ConnectionsUtils {

    private val connectionUUIDS = HashMap<String, UUID>()

    internal val connections = HashMap<UUID, Connection>()

    /*    internal val packetState = HashMap<UUID, PacketState>()
        internal val verifyToken = HashMap<UUID, ByteArray>()
        internal val sharedSecret = HashMap<UUID, ByteArray>()
        internal val username = HashMap<UUID, String>()
        internal val uuids = HashMap<UUID, UUID>()
        internal val ips = HashMap<UUID, String>()*/

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
    var packetState: PacketState = PacketState.HANDSHAKE,
    val verifyToken: ByteArray = EncryptionUtils.generateVerifyToken(),
    var sharedSecret: ByteArray? = null,
    var username: String? = null,
    var mcUUID: UUID? = null,
    var host: String? = null
)
