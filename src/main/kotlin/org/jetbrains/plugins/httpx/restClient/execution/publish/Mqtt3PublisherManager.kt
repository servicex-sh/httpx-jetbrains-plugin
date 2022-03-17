package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.jetbrains.plugins.httpx.restClient.execution.common.getMqttUri

@Suppress("UnstableApiUsage")
object Mqtt3PublisherManager {

    fun sendMqtt3Message(request: PublishRequest): CommonClientResponse {
        var mqttClient: MqttClient? = null
        try {
            val uri = getMqttUri(request.uri!!)
            mqttClient = MqttClient(uri, "httpx-plugin", MemoryPersistence())
            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
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