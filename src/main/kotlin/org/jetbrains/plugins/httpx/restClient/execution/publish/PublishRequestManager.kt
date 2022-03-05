package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.queryParameters
import com.rabbitmq.client.ConnectionFactory
import io.nats.client.Nats
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.RabbitFlux
import redis.clients.jedis.Jedis
import java.util.*

@Suppress("UnstableApiUsage")
class PublishRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun publish(request: PublishRequest): CommonClientResponse {
        if (!request.isLegal()) {
            return PublishResponse(
                CommonClientResponseBody.Text("request format not correct!"),
                "401", "Code format error, please check Host header"
            )
        }
        val schema = request.uri!!.scheme
        if (schema.startsWith("mqtt")) {
            return sendMqttMessage(request)
        } else if (schema.startsWith("redis")) {
            return sendRedisMessage(request)
        } else if (schema.startsWith("nats")) {
            return sendNatsMessage(request)
        } else if (schema.startsWith("kafka")) {
            return sendKafka(request)
        } else if (schema.startsWith("amqp")) {
            return sendRabbitMQ(request)
        }
        return PublishResponse(CommonClientResponseBody.Text("Schema not support!"), "401", "Unknown schema")
    }

    private fun sendKafka(request: PublishRequest): CommonClientResponse {
        val kafkaURI = request.uri!!
        val props = Properties()
        var port = kafkaURI.port
        if (port <= 0) {
            port = 9092
        }
        val topic = kafkaURI.path.substring(1)
        var partition: Int? = null
        var key: String? = null
        val params: Map<String, String> = kafkaURI.queryParameters
        if (params.containsKey("key")) {
            key = params["key"]
        }
        if (params.containsKey("partition")) {
            partition = Integer.valueOf(params["partition"])
        }
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaURI.host + ":" + port
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        val sender = KafkaSender.create(SenderOptions.create<String?, String>(props))
        return sender.send(
            Mono.just(
                SenderRecord.create<String?, String, Any?>(
                    topic, partition, System.currentTimeMillis(),
                    key, request.textToSend, null
                )
            )
        ).map {
            PublishResponse()
        }.onErrorReturn(PublishResponse(CommonClientResponseBody.Empty(), "Error"))
            .doFinally {
                sender.close()
            }.blockLast()!!
    }

    private fun sendRabbitMQ(request: PublishRequest): CommonClientResponse {
        val connectionFactory = ConnectionFactory()
        connectionFactory.useNio()
        connectionFactory.setUri(request.uri)
        val senderOptions = reactor.rabbitmq.SenderOptions()
            .connectionFactory(connectionFactory)
            .resourceManagementScheduler(Schedulers.boundedElastic())
        val rabbitSender = RabbitFlux.createSender(senderOptions)
        return rabbitSender
            .send(Mono.just(OutboundMessage("", request.topic!!, request.bodyBytes())))
            .map {
                PublishResponse()
            }.onErrorReturn(PublishResponse(CommonClientResponseBody.Empty(), "Error"))
            .doFinally {
                rabbitSender.close()
            }.block()!!
    }

    private fun sendRedisMessage(request: PublishRequest): CommonClientResponse {
        try {
            Jedis(request.uri).use { jedis ->
                jedis.publish(request.topic, request.textToSend)
            }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
        return PublishResponse()
    }

    private fun sendNatsMessage(request: PublishRequest): CommonClientResponse {
        try {
            Nats.connect(request.uri.toString()).use { nc ->
                nc.publish(request.topic, request.bodyBytes())
            }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
        return PublishResponse()
    }

    private fun sendMqttMessage(request: PublishRequest): CommonClientResponse {
        var mqttClient: MqttClient? = null
        try {
            val uri = getMqttUri(request.uri!!)
            mqttClient = MqttClient(uri, "httpx-plugin", MemoryPersistence())
            val connOpts = MqttConnectionOptions().apply {
                isCleanStart = true
            }
            mqttClient.connect(connOpts)
            mqttClient.publish(request.topic, MqttMessage(request.bodyBytes()))
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        } finally {
            mqttClient?.disconnect()
        }
        return PublishResponse()
    }

}
