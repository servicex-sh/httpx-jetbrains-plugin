package org.jetbrains.plugins.httpx.restClient.execution.memcache

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI

@Suppress("UnstableApiUsage")
class MemcacheRequest(
    override val URL: String?,
    override val httpMethod: String?,
    override val textToSend: String?,
    private val keyText: String?,
    private val headers: Map<String, String>
) :
    CommonClientRequest {
    var key: String? = null
    var uri: URI? = null
    var contentType: String? = null

    init {
        key = keyText?.trim()?.trim('/')
        contentType = headers.getOrDefault("Content-Type", "text/plain")
        var memcacheURI = if (headers.containsKey("URI")) {
            headers["URI"]!!
        } else if (headers.containsKey("HOST")) {
            headers["Host"]!!
        } else {
            "memcache://localhost:11211"
        }
        if (memcacheURI.startsWith("memcache://")) {
            memcacheURI = "memcache://${memcacheURI}"
        }
        this.uri = URI.create(memcacheURI)
    }

    fun isLegal(): Boolean {
        return key != null && uri != null
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: ByteArray(0)
    }

    fun getHeader(name: String): String? {
        return headers[name]
    }

}