package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.aliyun.eventbridge.EventBridge
import com.aliyun.eventbridge.models.Config
import com.aliyun.eventbridge.util.EventBuilder
import com.aliyun.mns.client.CloudAccount
import com.aliyun.mns.model.Message
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.queryParameters
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import io.lettuce.core.RedisClient
import io.nats.client.Nats
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.pulsar.client.api.PulsarClient
import org.apache.pulsar.client.api.TypedMessageBuilder
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.eclipse.paho.mqttv5.common.packet.UserProperty
import org.jetbrains.plugins.httpx.json.JsonUtils.objectMapper
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Aliyun.readAliyunAccessToken
import org.jetbrains.plugins.httpx.restClient.execution.aws.AWS
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.RabbitFlux
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI
import java.util.*


@Suppress("UnstableApiUsage", "MemberVisibilityCanBePrivate")
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
        val host = request.uri!!.host
        if (schema.startsWith("mqtt5")) {
            return sendMqtt5Message(request)
        } else if (schema.startsWith("mqtt")) {
            return Mqtt3PublisherManager.sendMqtt3Message(request)
        } else if (schema.startsWith("redis")) {
            return sendRedisMessage(request)
        } else if (schema.startsWith("nats")) {
            return sendNatsMessage(request)
        } else if (schema.startsWith("kafka")) {
            return sendKafka(request)
        } else if (schema.startsWith("pulsar")) {
            return sendPulsarMessage(request)
        } else if (schema.startsWith("amqp")) {
            return sendRabbitMQ(request)
        } else if (schema.startsWith("mns") || (host.contains(".mns.") && host.endsWith(".aliyuncs.com"))) {
            return sendMnsMessage(request)
        } else if (schema.startsWith("eventbridge")) {
            if (host.endsWith(".aliyuncs.com")) {
                return publishAliyunEventBridge(request)
            }
        } else if (schema.startsWith("arn")) {
            val awsUri = request.uri.toString()
            if (awsUri.startsWith("arn:aws:sqs:")) {
                return sendAwsSqsMessage(request)
            } else if (awsUri.startsWith("arn:aws:events:")) {
                return sendAwsEventBridgeMessage(request)
            } else if (awsUri.startsWith("arn:aws:sns:")) {
                return sendAwsSnsMessage(request)
            }
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
        // kafka client use Class.forName(trimmed, true, Utils.getContextOrKafkaClassLoader()) to get the Class object
        Thread.currentThread().contextClassLoader = null
        val sender = KafkaSender.create(SenderOptions.create<String?, String>(props))
        val senderRecord = SenderRecord.create<String?, String, Any?>(
            request.topic, partition, System.currentTimeMillis(),
            key, request.textToSend, null
        )
        request.getMsgHeaders().forEach { (name, value) ->
            senderRecord.headers().add(name, value.toByteArray())
        }
        senderRecord.headers().add("Content-Type", request.headers.getOrDefault("Content-Type", "text/plain").toByteArray())
        return sender.send(Mono.just(senderRecord))
            .map {
                PublishResponse()
            }
            .onErrorResume {
                Mono.just(PublishResponse(CommonClientResponseBody.Empty(), "Error", it.message))
            }
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
            .resourceManagementScheduler(Schedulers.immediate())
        val rabbitSender = RabbitFlux.createSender(senderOptions)
        val contentType: String = request.headers.getOrDefault("Content-Type", "text/plain")
        val amqpHeaders: MutableMap<String, Any> = HashMap()
        request.getMsgHeaders().forEach { (name, value) ->
            amqpHeaders[name] = value
        }
        val basicProperties = AMQP.BasicProperties.Builder().headers(amqpHeaders).contentType(contentType).build()
        return rabbitSender
            .send(Mono.just(OutboundMessage("", request.topic!!, basicProperties, request.bodyBytes())))
            .map {
                PublishResponse()
            }.onErrorResume {
                Mono.just(PublishResponse(CommonClientResponseBody.Empty(), "Error", it.message))
            }
            .doFinally {
                rabbitSender.close()
            }.defaultIfEmpty(PublishResponse())
            .block()!!
    }

    private fun sendRedisMessage(request: PublishRequest): CommonClientResponse {
        try {
            val redisClient = RedisClient.create(request.uri.toString())
            redisClient.connectPubSub().use {
                it.sync().publish(request.topic, request.textToSend)
            }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
        return PublishResponse()
    }

    private fun sendNatsMessage(request: PublishRequest): CommonClientResponse {
        try {
            Nats.connect(request.uri.toString()).use { nc ->
                request.topic!!.split("[,;]".toRegex()).forEach {
                    if (it.isNotEmpty()) {
                        nc.publish(it, request.bodyBytes())
                    }
                }
            }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
        return PublishResponse()
    }

    private fun sendMqtt5Message(request: PublishRequest): CommonClientResponse {
        var mqttClient: MqttClient? = null
        try {
            val uri = getMqttUri(request.uri!!)
            mqttClient = MqttClient(uri, "httpx-plugin", MemoryPersistence())
            val connOpts = MqttConnectionOptions().apply {
                isCleanStart = true
            }
            mqttClient.connect(connOpts)
            val mqttMessage = MqttMessage(request.bodyBytes()).apply {
                properties = MqttProperties().apply {
                    contentType = request.contentType
                }
                request.getMsgHeaders().forEach { (name, value) ->
                    properties.userProperties.add(UserProperty(name, value))
                }
            }
            mqttClient.publish(request.topic, mqttMessage)
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        } finally {
            mqttClient?.disconnect()
        }
        return PublishResponse()
    }

    fun sendAwsSnsMessage(request: PublishRequest): CommonClientResponse {
        val awsBasicCredentials = AWS.awsBasicCredentials(request.getHeader("Authorization"))
            ?: return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Cannot find AWS AK info, please check Authorization header!")
        val topicArn: String = request.uri.toString()
        val regionId = getAwsRegionId(request, topicArn)
        SnsClient.builder()
            .region(Region.of(regionId))
            .credentialsProvider { awsBasicCredentials }
            .build().use { snsClient ->
                val snsRequest = software.amazon.awssdk.services.sns.model.PublishRequest.builder()
                    .message(request.textToSend)
                    .topicArn(topicArn)
                    .build()
                val result = snsClient.publish(snsRequest)
                val response = result.sdkHttpResponse()
                if (!response.isSuccessful) {
                    return PublishResponse(CommonClientResponseBody.Empty(), "Error", response.statusText().get())
                }
            }
        return PublishResponse()
    }

    fun sendAwsEventBridgeMessage(request: PublishRequest): CommonClientResponse {
        val awsBasicCredentials = AWS.awsBasicCredentials(request.getHeader("Authorization"))
            ?: return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Cannot find AWS AK info, please check Authorization header!")
        val eventBusArn: String = request.uri.toString()
        val regionId = getAwsRegionId(request, eventBusArn)
        try {
            EventBridgeClient.builder()
                .region(Region.of(regionId))
                .credentialsProvider { awsBasicCredentials }
                .build().use { eventBrClient ->
                    val cloudEvent = objectMapper.readValue(request.bodyBytes(), Map::class.java)
                    //validate cloudEvent
                    val source = cloudEvent["source"] as String?
                        ?: return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Please supply source field in json body!")
                    val datacontenttype = cloudEvent["datacontenttype"] as String?
                    if (datacontenttype != null && !datacontenttype.startsWith("application/json")) {
                        System.err.println("datacontenttype value should be 'application/json'!")
                        return PublishResponse(CommonClientResponseBody.Empty(), "Error", "acontenttype value should be 'application/json'!")
                    }
                    val data = cloudEvent["data"]
                    if (data == null) {
                        System.err.println("data field should be supplied in json body!")
                        return PublishResponse(CommonClientResponseBody.Empty(), "Error", "data field should be supplied in json body!")
                    }
                    val jsonData: String = if (data is Map<*, *> || data is List<*>) {
                        objectMapper.writeValueAsString(data)
                    } else {
                        data.toString()
                    }
                    val reqEntry = PutEventsRequestEntry.builder()
                        .resources(eventBusArn)
                        .source(source)
                        .detailType(datacontenttype)
                        .detail(jsonData)
                        .build()
                    val eventsRequest = PutEventsRequest.builder()
                        .entries(reqEntry)
                        .build()
                    val result = eventBrClient.putEvents(eventsRequest)
                    for (resultEntry in result.entries()) {
                        return if (resultEntry.eventId() != null) {
                            PublishResponse(CommonClientResponseBody.Empty(), "OK", null, resultEntry.eventId())
                        } else {
                            PublishResponse(CommonClientResponseBody.Empty(), "Error", resultEntry.errorCode())
                        }
                    }
                }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
        return PublishResponse()
    }

    fun sendAwsSqsMessage(request: PublishRequest): CommonClientResponse {
        val awsBasicCredentials = AWS.awsBasicCredentials(request.getHeader("Authorization"))
            ?: return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Cannot find AWS AK info, please check Authorization header!")
        val queueArn: String = request.uri.toString()
        val regionId = getAwsRegionId(request, queueArn)
        val parts = queueArn.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sqsRegionId = parts[3]
        val sqsQueueId = parts[4]
        val sqsName = parts[5]
        val queueUrl = "https://sqs.$sqsRegionId.amazonaws.com/$sqsQueueId/$sqsName"
        SqsClient.builder()
            .region(Region.of(regionId))
            .credentialsProvider { awsBasicCredentials }
            .build().use { sqsClient ->
                val sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(request.textToSend)
                    .build()
                val response = sqsClient.sendMessage(sendMsgRequest)
                return PublishResponse(CommonClientResponseBody.Empty(), "OK", null, response.messageId())
            }
    }

    private fun getAwsRegionId(httpRequest: PublishRequest, resourceArn: String?): String? {
        var regionId: String? = httpRequest.getHeader("X-Region-Id")
        if (regionId == null && resourceArn != null) {
            val parts = resourceArn.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size > 3) {
                regionId = parts[3]
            }
        }
        if (regionId == null) {
            regionId = Region.US_EAST_1.id()
        }
        return regionId
    }

    fun sendMnsMessage(request: PublishRequest): CommonClientResponse {
        val cloudAccount = readAliyunAccessToken(request.getBasicAuthorization())
            ?: return PublishResponse(
                CommonClientResponseBody.Empty(),
                "Error",
                "Please supply access key Id/Secret in Authorization header as : `Authorization: Basic keyId:secret`"
            )
        return try {
            val mnsClient = CloudAccount(cloudAccount.accessKeyId, cloudAccount.accessKeySecret, "https://" + request.uri!!.host).mnsClient
            val queueRef = mnsClient.getQueueRef(request.topic)
            val message = queueRef.putMessage(Message(request.bodyBytes()))
            PublishResponse(CommonClientResponseBody.Empty(), "OK", null, message.messageId)
        } catch (e: java.lang.Exception) {
            PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }

    fun publishAliyunEventBridge(request: PublishRequest): CommonClientResponse {
        val cloudAccount = readAliyunAccessToken(request.getBasicAuthorization())
            ?: return PublishResponse(
                CommonClientResponseBody.Empty(),
                "Error",
                "Please supply access key Id/Secret in Authorization header as : `Authorization: Basic keyId:secret`"
            )
        try {
            val eventBus = request.topic
            val cloudEvent = objectMapper.readValue(request.bodyBytes(), Map::class.java)
            //validate cloudEvent
            val source = cloudEvent["source"] as String?
            if (source == null) {
                System.err.println("Please supply source field in json body!")
                return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Please supply source field in json body!")

            }
            val datacontenttype = cloudEvent["datacontenttype"] as String?
            if (datacontenttype != null && !datacontenttype.startsWith("application/json")) {
                return PublishResponse(CommonClientResponseBody.Empty(), "Error", "datacontenttype value should be 'application/json'!")
            }
            val data = cloudEvent["data"]
            if (data == null) {
                System.err.println("data field should be supplied in json body!")
                return PublishResponse(CommonClientResponseBody.Empty(), "Error", "data field should be supplied in json body!")
            }
            val jsonData: String = if (data is Map<*, *> || data is List<*>) {
                objectMapper.writeValueAsString(data)
            } else {
                data.toString()
            }
            var eventId = cloudEvent["id"] as String?
            if (eventId == null) {
                eventId = UUID.randomUUID().toString()
            }
            val authConfig = Config()
            authConfig.accessKeyId = cloudAccount.accessKeyId
            authConfig.accessKeySecret = cloudAccount.accessKeySecret
            authConfig.endpoint = request.uri!!.host
            val eventBridgeClient: EventBridge = com.aliyun.eventbridge.EventBridgeClient(authConfig)
            val event = EventBuilder.builder()
                .withId(eventId)
                .withSource(URI.create(source))
                .withType(cloudEvent["type"] as String?)
                .withSubject(cloudEvent["subject"] as String?)
                .withTime(Date())
                .withJsonStringData(jsonData)
                .withAliyunEventBus(eventBus)
                .build()
            val putEventsResponse = eventBridgeClient.putEvents(listOf(event))
            val resultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(putEventsResponse)
            return PublishResponse(CommonClientResponseBody.Text(resultJson), "OK", null, eventId)
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }

    private fun sendPulsarMessage(request: PublishRequest): CommonClientResponse {
        try {
            PulsarClient.builder().serviceUrl(request.uri!!.toString()).build().use { client ->
                client.newProducer().topic(request.topic).create().use { producer ->
                    val builder: TypedMessageBuilder<ByteArray> = producer.newMessage().value(request.bodyBytes())
                        .property("Content-Type", request.headers.getOrDefault("Content-Type", "text/plan"))
                    request.getMsgHeaders().forEach { (name, value) ->
                        builder.property(name, value)
                    }
                    val msgId = builder.send()
                    return PublishResponse(CommonClientResponseBody.Empty(), "OK", null, msgId.toString())
                }
            }
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }

}
