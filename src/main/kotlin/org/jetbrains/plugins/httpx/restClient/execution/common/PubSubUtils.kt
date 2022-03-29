package org.jetbrains.plugins.httpx.restClient.execution.common

import java.net.URI

fun getMqttUri(mqttURI: URI): String {
    val schema = mqttURI.scheme
    var brokerUrl = mqttURI.toString()
    brokerUrl = if (schema.contains("+")) {
        brokerUrl.substring(brokerUrl.indexOf("+") + 1)
    } else if (schema == "mqtt5") {
        brokerUrl.replace("mqtt5://", "tcp://")
    } else {
        brokerUrl.replace("mqtt://", "tcp://")
    }
    return brokerUrl
}