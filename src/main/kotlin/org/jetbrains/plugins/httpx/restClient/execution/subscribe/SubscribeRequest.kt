package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientBodyFileHint
import com.intellij.httpClient.execution.common.CommonClientRequest
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import java.net.URI

@Suppress("UnstableApiUsage")
class SubscribeRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, private val headers: Map<String, String>) :
    CommonClientRequest {
    var topic: String? = null
    var uri: URI? = null
    var contentType: String? = null
    var acceptType: String? = null;

    init {
        topic = URL?.trim()?.trim('/')
        contentType = headers.getOrDefault("Content-Type", "text/plain")
        acceptType = headers.getOrDefault("Accept", "text/plain")
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

    fun getMessageBodyFiletHint(fileBaseName: String): CommonClientBodyFileHint {
        return if (acceptType!!.contains("json")) {
            JsonBodyFileHint.jsonBodyFileHint("${fileBaseName}.json")
        } else {
            TextBodyFileHint.textBodyFileHint("${fileBaseName}.txt")
        }
    }

}