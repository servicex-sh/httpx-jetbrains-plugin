package org.jetbrains.plugins.httpx.restClient.execution.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI
import java.util.*


@Suppress("UnstableApiUsage")
class GraphqlRequest(override val URL: String?, override val httpMethod: String, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "application/json")
        body = textToSend ?: "{}"
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
        return if (contentType.startsWith("application/graphql")) {  // convert graphql code into json object
            val variablesHeader = getHeadValue("x-graphql-variables")
            if (variablesHeader != null && variablesHeader.startsWith("{")) {
                val objectMapper = ObjectMapper()
                val jsonRequest = mutableMapOf<String, Any>()
                jsonRequest["query"] = body
                jsonRequest["variables"] = objectMapper.readValue(variablesHeader, Map::class.java)
                objectMapper.writeValueAsBytes(jsonRequest)
            } else {
                ObjectMapper().writeValueAsBytes(Collections.singletonMap<String, Any>("query", body))
            }
        } else {
            body.encodeToByteArray()
        }
    }

}