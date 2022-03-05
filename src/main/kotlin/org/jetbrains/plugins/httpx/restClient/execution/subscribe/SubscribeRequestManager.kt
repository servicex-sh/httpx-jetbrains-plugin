package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.queryParameters
import com.rabbitmq.client.ConnectionFactory
import io.nats.client.Nats
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import org.jetbrains.plugins.httpx.restClient.execution.publish.AbstractMqttCallback
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Receiver
import reactor.rabbitmq.ReceiverOptions
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
class SubscribeRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun subscribe(request: SubscribeRequest): CommonClientResponse {
        if (!request.isLegal()) {
            return SubscribeResponse(
                CommonClientResponseBody.Text("request format not correct!"),
                "401", "Code format error, please check Host header"
            )
        }
        val schema = request.uri!!.scheme
        if (schema.startsWith("mqtt")) {
            return subscribeMqtt(request)
        } else if (schema.startsWith("nats")) {
            return subscribeNats(request)
        } else if (schema.startsWith("amqp")) {
            return subscribeRabbitmq(request)
        } else if (schema.startsWith("kafka")) {
            return subscribeKafka(request)
        } else if (schema.startsWith("redis")) {
            return subscribeRedis(request)
        }
        return SubscribeResponse(CommonClientResponseBody.Text("Schema not support!"), "401", "Unknown schema")
    }

    private fun subscribeMqtt(request: SubscribeRequest): CommonClientResponse {
        try {
            val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
                replay = 1000,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
            var mqttClient: MqttClient? = null
            val disposeConnection = Disposable {
                mqttClient?.disconnectForcibly()
            }
            val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("nats-result.txt")).withConnectionDisposable(disposeConnection)
            val uri = getMqttUri(request.uri!!)
            val clientId = "httpx-plugin-" + UUID.randomUUID()
            mqttClient = MqttClient(uri, clientId, MemoryPersistence())
            mqttClient.setCallback(object : AbstractMqttCallback() {
                override fun messageArrived(topic: String, message: MqttMessage) {
                    val body = String(message.payload, StandardCharsets.UTF_8)
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(body + "\n\n"))
                }
            })
            val connOpts = MqttConnectionOptions().apply {
                isCleanStart = true
            }
            mqttClient.connect(connOpts)
            mqttClient.subscribe(request.topic, 1)
            return SubscribeResponse(textStream)
        } catch (e: Exception) {
            return SubscribeResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }

    private fun subscribeNats(request: SubscribeRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var nc: io.nats.client.Connection? = null
        val disposeConnection = Disposable {
            nc?.close()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("nats-result.txt")).withConnectionDisposable(disposeConnection)
        try {
            nc = Nats.connect(request.uri.toString())
            val dispatcher = nc.createDispatcher {
                val body = it.data.toString(StandardCharsets.UTF_8)
                shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(body + "\n\n"))
            }
            dispatcher.subscribe(request.topic!!)
        } catch (e: java.lang.Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }

    private fun subscribeRedis(request: SubscribeRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var jedis: Jedis? = null
        val disposeConnection = Disposable {
            jedis?.close()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("redis-result.txt")).withConnectionDisposable(disposeConnection)
        try {
            jedis = Jedis(request.uri.toString())
            jedis.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(message + "\n\n"))
                }
            }, request.topic)
        } catch (e: Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }

    private fun subscribeRabbitmq(request: SubscribeRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var receiver: Receiver? = null
        val disposeConnection = Disposable {
            receiver?.close()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("rabbitmq-result.txt")).withConnectionDisposable(disposeConnection)
        try {
            val connectionFactory = ConnectionFactory()
            connectionFactory.setUri(request.uri)
            val receiverOptions = ReceiverOptions()
                .connectionFactory(connectionFactory)
                .connectionSubscriptionScheduler(Schedulers.boundedElastic())
            receiver = RabbitFlux.createReceiver(receiverOptions)
            receiver.consumeAutoAck(request.topic!!)
                .doOnError {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(it))
                    receiver?.close()
                }
                .subscribe {
                    val data = String(it.body, StandardCharsets.UTF_8)
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(data + "\n\n"))
                }
        } catch (e: Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }

    private fun subscribeKafka(request: SubscribeRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var fluxDisposable: reactor.core.Disposable? = null
        val disposeConnection = Disposable {
            fluxDisposable?.dispose();
        }
        val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("kafka-result.txt")).withConnectionDisposable(disposeConnection)
        val props = Properties()
        val kafkaURI = request.uri!!
        var port: Int = kafkaURI.port
        if (port <= 0) {
            port = 9092
        }
        val params: Map<String, String> = kafkaURI.queryParameters
        var groupId: String? = "httpx-plugin-" + UUID.randomUUID()
        if (params.containsKey("group")) {
            groupId = params["group"]
        }
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaURI.getHost() + ":" + port
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        val receiverOptions = reactor.kafka.receiver.ReceiverOptions.create<String, String>(props).subscription(setOf(request.topic))
        try {
            val kafkaReceiver: KafkaReceiver<String, String> = KafkaReceiver.create(receiverOptions)
            fluxDisposable = kafkaReceiver.receive()
                .doOnError {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(it))
                    fluxDisposable?.dispose()
                }
                .subscribe {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(it.value() + "\n\n"))
                }
        } catch (e: Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }

}

