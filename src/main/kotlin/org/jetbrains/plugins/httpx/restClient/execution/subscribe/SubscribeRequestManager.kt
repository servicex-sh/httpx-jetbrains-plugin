package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.queryParameters
import com.rabbitmq.client.ConnectionFactory
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.nats.client.Nats
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.PulsarClient
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import org.jetbrains.plugins.httpx.restClient.execution.publish.AbstractMqttCallback
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Receiver
import reactor.rabbitmq.ReceiverOptions
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("UnstableApiUsage")
class SubscribeRequestManager(private val project: Project) : Disposable {

    companion object {
        fun formatReceivedMessage(body: String): String {
            val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            return "//=====${timestamp}=========\n${body}\n\n"
        }
    }

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
        if (schema.startsWith("mqtt5")) {
            return subscribeMqtt5(request)
        } else if (schema.startsWith("mqtt")) {
            return Mqtt3SubscribeManager.subscribeMqtt(request)
        } else if (schema.startsWith("nats")) {
            return subscribeNats(request)
        } else if (schema.startsWith("amqp")) {
            return subscribeRabbitmq(request)
        } else if (schema.startsWith("kafka")) {
            return subscribeKafka(request)
        } else if (schema.startsWith("pulsar")) {
            return subscribePulsar(request)
        } else if (schema.startsWith("redis")) {
            return subscribeRedis(request)
        }
        return SubscribeResponse(CommonClientResponseBody.Text("Schema not support!"), "401", "Unknown schema")
    }

    private fun subscribeMqtt5(request: SubscribeRequest): CommonClientResponse {
        try {
            val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
                replay = 1000,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
            var mqttClient: MqttClient? = null
            val disposeConnection = Disposable {
                mqttClient?.disconnect()
            }
            val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("mqtt5-messages")).withConnectionDisposable(disposeConnection)
            val uri = getMqttUri(request.uri!!)
            val clientId = "httpx-plugin-" + UUID.randomUUID()
            mqttClient = MqttClient(uri, clientId, MemoryPersistence())
            mqttClient.setCallback(object : AbstractMqttCallback() {
                override fun messageArrived(topic: String, message: MqttMessage) {
                    val body = String(message.payload, StandardCharsets.UTF_8)
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(body)))
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
        val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("nats-messages")).withConnectionDisposable(disposeConnection)
        try {
            nc = Nats.connect(request.uri.toString())
            val dispatcher = nc.createDispatcher {
                val body = it.data.toString(StandardCharsets.UTF_8)
                shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(body)))
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
        var pubSubConnection: StatefulRedisPubSubConnection<String, String>? = null
        val disposeConnection = Disposable {
            pubSubConnection?.close()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("redis-messages")).withConnectionDisposable(disposeConnection)
        try {
            val redisClient = RedisClient.create(request.uri.toString())
            pubSubConnection = redisClient.connectPubSub()
            val reactiveSubscriber = pubSubConnection.reactive()
            reactiveSubscriber.subscribe(request.topic).subscribe()
            reactiveSubscriber.observeChannels()
                .doOnNext {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(it.message)))
                }
                .doOnError {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(it))
                }
                .subscribe()
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
        val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("amqp-messages")).withConnectionDisposable(disposeConnection)
        try {
            val connectionFactory = ConnectionFactory()
            connectionFactory.setUri(request.uri)
            val receiverOptions = ReceiverOptions()
                .connectionFactory(connectionFactory)
                .connectionSubscriptionScheduler(Schedulers.immediate())
            receiver = RabbitFlux.createReceiver(receiverOptions)
            receiver.consumeAutoAck(request.topic!!)
                .doOnError {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(it))
                    receiver?.close()
                }
                .subscribe {
                    val data = String(it.body, StandardCharsets.UTF_8)
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(data)))
                }
        } catch (e: Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }

    private fun subscribePulsar(request: SubscribeRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var pulsarClient: PulsarClient? = null
        val disposeConnection = Disposable {
            pulsarClient?.close()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("pulsar-messages")).withConnectionDisposable(disposeConnection)
        try {
            pulsarClient = PulsarClient.builder().serviceUrl(request.uri.toString()).build()
            pulsarClient.newConsumer()
                .topic(request.topic)
                .subscriptionName("httpx-cli-${UUID.randomUUID()}")
                .messageListener { consumer: Consumer<ByteArray?>, msg: Message<ByteArray?> ->
                    try {
                        val data = String(msg.data, StandardCharsets.UTF_8)
                        consumer.acknowledge(msg)
                        shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(data)))
                    } catch (e: Exception) {
                        consumer.negativeAcknowledge(msg)
                        shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
                    }
                }
                .subscribe()
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
            try {
                fluxDisposable?.dispose()
            } catch (ignore: Exception) {

            }
        }
        val textStream = CommonClientResponseBody.TextStream(shared, request.getMessageBodyFiletHint("kafka-messages")).withConnectionDisposable(disposeConnection)
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
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${kafkaURI.host}:${port}"
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        // kafka client use Class.forName(trimmed, true, Utils.getContextOrKafkaClassLoader()) to get the Class object
        Thread.currentThread().contextClassLoader = null
        val receiverOptions = reactor.kafka.receiver.ReceiverOptions.create<String, String>(props).subscription(setOf(request.topic))
        try {
            val kafkaReceiver: KafkaReceiver<String, String> = KafkaReceiver.create(receiverOptions)
            fluxDisposable = kafkaReceiver.receive()
                .doOnError {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(it))
                    fluxDisposable?.dispose()
                }
                .subscribe {
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(formatReceivedMessage(it.value())))
                }
        } catch (e: Exception) {
            shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
        }
        return SubscribeResponse(textStream)
    }


}

