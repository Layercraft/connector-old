package io.layercraft.connector

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.layercraft.connector.handler.LocalHandler
import io.layercraft.translator.TranslatorAPI
import io.layercraft.translator.codec.MinecraftCodec
import io.layercraft.translator.codec.MinecraftCodecs
import io.layercraft.translator.packets.PacketDirection
import io.layercraft.translator.packets.PacketState
import io.layercraft.translator.packets.handshake.data.HandshakeNextState
import io.layercraft.translator.packets.handshake.serverbound.Handshake
import io.layercraft.translator.utils.MinecraftVarIntUtils
import io.layercraft.translator.utils.MinecraftVarIntUtils.readVarInt
import io.layercraft.translator.utils.mc
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.netty.ByteBufFlux
import reactor.netty.DisposableServer
import reactor.netty.tcp.TcpServer
import reactor.netty.transport.logging.AdvancedByteBufFormat
import reactor.tools.agent.ReactorDebugAgent
import java.nio.ByteBuffer
import java.util.logging.Logger
import kotlin.experimental.and



fun main(args: Array<String>) {
    Server.start()
}
