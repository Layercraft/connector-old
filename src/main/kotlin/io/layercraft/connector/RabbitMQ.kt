package io.layercraft.connector

import com.rabbitmq.client.ConnectionFactory

class RabbitMQ {
    // @Bean
    fun connect() {
        println("Connecting to RabbitMQ")
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        channel.queueDeclare("hello", false, false, false, null)
    }
}
