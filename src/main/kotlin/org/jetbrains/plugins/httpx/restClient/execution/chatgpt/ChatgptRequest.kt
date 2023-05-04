package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

import com.intellij.httpClient.execution.common.CommonClientRequest
import org.jetbrains.plugins.httpx.json.JsonUtils
import java.net.URI


@Suppress("UnstableApiUsage")
class ChatgptRequest(
    override val URL: String?,
    override val httpMethod: String,
    override val textToSend: String?,
    val headers: Map<String, String>
) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "text/markdown")
        body = textToSend ?: ""
    }

    fun getHeadValue(name: String): String? {
        for (header in headers) {
            if (header.key.toLowerCase() == name) {
                return header.value
            }
        }
        return null
    }

    fun bodyBytes(): ByteArray {
        val chatRequest = mutableMapOf<String, Any>()
        chatRequest["model"] = getHeadValue("X-Model") ?: "gpt-3.5-turbo"
        chatRequest["temperature"] = (getHeadValue("X-Temperature") ?: "1.0").toDouble()
        chatRequest["messages"] = listOf(mapOf("role" to "user", "content" to body))
        return JsonUtils.objectMapper.writeValueAsBytes(chatRequest)
    }

}