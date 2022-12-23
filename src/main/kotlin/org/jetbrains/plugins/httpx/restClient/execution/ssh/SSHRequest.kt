package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
class SSHRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, private val headers: Map<String, String>) :
    CommonClientRequest {
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        body = textToSend ?: ""
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

    fun getHeader(name: String): String? {
        val lowerCaseName = name.toLowerCase()
        if (headers.isNotEmpty()) {
            for (header in headers) {
                if (header.key.toLowerCase() == lowerCaseName) {
                    return header.value
                }
            }
        }
        return null
    }

}