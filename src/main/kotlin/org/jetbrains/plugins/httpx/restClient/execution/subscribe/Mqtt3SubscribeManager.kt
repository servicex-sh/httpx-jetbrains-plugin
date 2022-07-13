package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri
import org.jetbrains.plugins.httpx.restClient.execution.subscribe.SubscribeRequestManager.Companion.formatReceivedMessage
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
object Mqtt3SubscribeManager {

    fun subscribeMqtt(request: SubscribeRequest): CommonClientResponse {
        try {
            val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
                replay = 1000,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
            var mqttClient: MqttClient? = null
            val disposeConnection = Disposable {
                mqttClient?.disconnect()
            }
            val textStream = CommonClientResponseBody.TextStream(shared, TextBodyFileHint.textBodyFileHint("mqtt3-result.txt")).withConnectionDisposable(disposeConnection)
            val uri = getMqttUri(request.uri!!)
            val clientId = "httpx-plugin-${UUID.randomUUID()}"
            mqttClient = MqttClient(uri, clientId, MemoryPersistence())
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {

                }

                override fun messageArrived(topic: String?, message: MqttMessage) {
                    val body = String(message.payload, StandardCharsets.UTF_8)
                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Content.Chunk(formatReceivedMessage(body)))

                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }

            })
            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
            }
            mqttClient.connect(connOpts)
            mqttClient.subscribe(request.topic, 1)
            return SubscribeResponse(textStream)
        } catch (e: Exception) {
            return SubscribeResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        }
    }
}