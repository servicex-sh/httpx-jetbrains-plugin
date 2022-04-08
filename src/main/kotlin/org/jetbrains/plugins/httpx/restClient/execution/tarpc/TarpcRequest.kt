package org.jetbrains.plugins.httpx.restClient.execution.tarpc

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI

@Suppress("UnstableApiUsage")
class TarpcRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, private val headers: Map<String, String>) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "application/json")
        body = textToSend ?: "{}"
    }

}