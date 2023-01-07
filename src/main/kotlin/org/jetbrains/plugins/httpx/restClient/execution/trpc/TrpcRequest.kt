package org.jetbrains.plugins.httpx.restClient.execution.trpc

import com.intellij.httpClient.execution.common.CommonClientRequest
import org.intellij.markdown.html.urlEncode
import java.net.URI

@Suppress("UnstableApiUsage")
class TrpcRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "application/json")
        body = textToSend ?: ""
    }

    fun getRealHttpMethod(): String {
        return when (httpMethod) {
            "TRPC" -> "GET"
            "TRPCQ" -> "GET"
            "TRPCM" -> "POST"
            else -> "GET"
        }
    }

    fun getRealUri(): URI {
        val httpMethod = getRealHttpMethod()
        if (httpMethod == "GET" && body.isNotEmpty()) {
            return URI.create(uri.toString() + "?input=" + urlEncode(body))
        }
        return uri
    }

}