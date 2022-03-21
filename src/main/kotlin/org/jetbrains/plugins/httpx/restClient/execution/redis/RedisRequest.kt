package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI

@Suppress("UnstableApiUsage")
class RedisRequest(
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
        var redisURI = if (headers.containsKey("URI")) {
            headers["URI"]!!
        } else if (headers.containsKey("HOST")) {
            headers["Host"]!!
        } else {
            "redis://localhost:6379"
        }
        if (!redisURI.startsWith("redis://")) {
            redisURI = "redis://${redisURI}"
        }
        this.uri = URI.create(redisURI)
    }

    fun isLegal(): Boolean {
        return key != null && uri != null
    }

    fun bodyText(): String {
        return textToSend ?: ""
    }

    fun getHeader(name: String, defaultValue: String): String {
        return headers.getOrDefault(name, defaultValue)
    }

}