package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import java.net.URI

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
        }
        return PublishResponse(CommonClientResponseBody.Text("Schema not support!"), "401", "Unknown schema")
    }

    private fun sendMqttMessage(request: PublishRequest): CommonClientResponse {
        var sampleClient: MqttClient? = null
        try {
            val uri = getMqttUri(request.uri!!)
            sampleClient = MqttClient(uri, "httpx-plugin", MemoryPersistence())
            val connOpts = MqttConnectionOptions()
            connOpts.isCleanStart = true
            sampleClient.connect(connOpts)
            sampleClient.publish(request.topic, MqttMessage(request.bodyBytes()))
            return PublishResponse()
        } catch (e: Exception) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", e.stackTraceToString())
        } finally {
            if (sampleClient != null) {
                try {
                    sampleClient.disconnect()
                } catch (ignore: MqttException) {
                }
            }
        }
    }


    private fun getMqttUri(mqttURI: URI): String {
        val schema = mqttURI.scheme
        var brokerUrl = mqttURI.toString()
        brokerUrl = if (schema.contains("+")) {
            brokerUrl.substring(brokerUrl.indexOf("+") + 1)
        } else {
            brokerUrl.replace("mqtt://", "tcp://")
        }
        return brokerUrl
    }


}

data class UriAndSubject(val uri: String, val subject: String)