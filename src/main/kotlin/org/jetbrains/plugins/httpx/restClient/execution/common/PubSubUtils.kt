package org.jetbrains.plugins.httpx.restClient.execution.common

import java.net.URI

data class UriAndSubject(val uri: String, val subject: String)

fun getMqttUri(mqttURI: URI): String {
    val schema = mqttURI.scheme
    var brokerUrl = mqttURI.toString()
    brokerUrl = if (schema.contains("+")) {
        brokerUrl.substring(brokerUrl.indexOf("+") + 1)
    } else {
        brokerUrl.replace("mqtt://", "tcp://")
    }
    return brokerUrl
}