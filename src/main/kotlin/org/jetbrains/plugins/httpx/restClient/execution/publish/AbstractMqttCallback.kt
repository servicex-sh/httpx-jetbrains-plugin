package org.jetbrains.plugins.httpx.restClient.execution.publish

import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties

/**
 * abstract MqttCallback
 *
 * @author linux_china
 */
abstract class AbstractMqttCallback : MqttCallback {
    override fun disconnected(disconnectResponse: MqttDisconnectResponse) {}
    override fun mqttErrorOccurred(exception: MqttException) {}

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
    }

    override fun deliveryComplete(token: IMqttToken) {}
    override fun connectComplete(reconnect: Boolean, serverURI: String) {}
    override fun authPacketArrived(reasonCode: Int, properties: MqttProperties) {}
}