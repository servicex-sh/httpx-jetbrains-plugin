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

    fun bodyBytes(): ByteArray {
        return if (contentType.startsWith("application/graphql")) {  // convert graphql code into json object
            if (body.startsWith("#variables") && body.contains('\n')) {
                val objectMapper = ObjectMapper()
                val jsonRequest = mutableMapOf<String, Any>()
                val lineBreakOffset = body.indexOf('\n')
                //query
                val query = body.substring(lineBreakOffset + 1)
                jsonRequest["query"] = query
                //variables
                val variablesJson = body.substring(body.indexOf(' ') + 1, lineBreakOffset).trim()
                if (variablesJson.startsWith("{")) {
                    jsonRequest["variables"] = objectMapper.readValue(variablesJson, Map::class.java)
                }
                objectMapper.writeValueAsBytes(jsonRequest)
            } else {
                ObjectMapper().writeValueAsBytes(Collections.singletonMap<String, Any>("query", body))
            }
        } else {
            body.encodeToByteArray()
        }
    }

}