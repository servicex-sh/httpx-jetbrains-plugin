package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI


@Suppress("UnstableApiUsage")
class PublishRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, private val headers: Map<String, String>) :
    CommonClientRequest {
    var topic: String? = null
    var uri: URI? = null
    var contentType: String? = null

    init {
        topic = URL?.trim()?.trim('/')
        contentType = headers.getOrDefault("Content-Type", "text/plain")
        uri = headers["Host"]?.let {
            URI.create(it)
        }
    }

    fun isLegal(): Boolean {
        return topic != null && uri != null
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: ByteArray(0)
    }

}