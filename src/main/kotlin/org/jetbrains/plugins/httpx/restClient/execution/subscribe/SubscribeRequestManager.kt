package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.nats.client.Nats
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import org.jetbrains.plugins.httpx.restClient.execution.publish.AbstractMqttCallback
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
        val topic: String = request.topic!!
        try {
            val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
                replay = 1000,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
            var nc: io.nats.client.Connection? = null
            val disposeConnection = Disposable {
                nc?.close()
            }
            val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("nats-result.txt")).withConnectionDisposable(disposeConnection)
            nc = Nats.connect(request.uri.toString())
            val dispatcher = nc.createDispatcher {
                val body = it.data.toString(StandardCharsets.UTF_8)
                shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(body + "\n\n"))
            }
            dispatcher.subscribe(topic)
            return SubscribeResponse(textStream)
        } catch (e: java.lang.Exception) {
            return SubscribeResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }

}

