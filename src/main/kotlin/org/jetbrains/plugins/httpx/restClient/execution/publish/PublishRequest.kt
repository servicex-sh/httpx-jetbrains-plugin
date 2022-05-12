package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
class PublishRequest(
    override val URL: String?,
    override val httpMethod: String?,
    override val textToSend: String?,
    private val topicText: String?,
    private val headers: Map<String, String>
) :
    CommonClientRequest {
    var topic: String? = null
    var uri: URI? = null
    val contentType: String

    init {
        topic = topicText?.trim()?.trim('/')
        contentType = headers.getOrDefault("Content-Type", "text/plain")
        uri = if (headers.containsKey("URI")) {
            URI.create(headers["URI"]!!)
        } else {
            URI.create(headers["Host"]!!)
        }
    }

    fun isLegal(): Boolean {
        return topic != null && uri != null
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: ByteArray(0)
    }

    fun getHeader(name: String): String? {
        return headers[name]
    }

    fun getBasicAuthorization(): List<String>? {
        val header = headers["Authorization"]
        if (header != null && header.startsWith("Basic ")) {
            var clearText = header.substring(6).trim()
            if (!(clearText.contains(' ') || clearText.contains(':'))) {
                clearText = String(Base64.getDecoder().decode(clearText), StandardCharsets.UTF_8)
            }
            return clearText.split("[:\\s]".toRegex())
        }
        return null
    }

}