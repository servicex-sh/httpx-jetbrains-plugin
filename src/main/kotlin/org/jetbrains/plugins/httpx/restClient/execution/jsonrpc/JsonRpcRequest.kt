package org.jetbrains.plugins.httpx.restClient.execution.jsonrpc

import com.intellij.httpClient.execution.common.CommonClientRequest
import org.jetbrains.plugins.httpx.json.JsonUtils.convertToDoubleQuoteString
import java.net.URI

@Suppress("UnstableApiUsage")
class JsonRpcRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "application/json")
        body = textToSend ?: ""
    }

    /**
     * merge X-Args-0 headers into json array body
     */
    fun jsonArrayBodyWithArgsHeaders(): String {
        val argsHeaders: Map<String, String> = headers.filter { it.key.toLowerCase().startsWith("x-args-") }
            .mapKeys { it.key.toLowerCase() }
        if (argsHeaders.isEmpty()) {
            return body
        }
        var newBody = body
        if (!contentType.contains("json")) {
            if (!newBody.startsWith('"')) {
                newBody = convertToDoubleQuoteString(newBody)
            }
        }
        val argLines = mutableListOf<String>()
        for (i in 0..argsHeaders.size) {
            val key = "x-args-$i"
            argLines.add(argsHeaders.getOrDefault(key, newBody))
        }
        return "[" + java.lang.String.join(",", argLines) + "]"
    }

}