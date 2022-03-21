package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class RedisRequestExecutionSupport : RequestExecutionSupport<RedisRequest> {
    companion object {
        val redisMethods = listOf("RSET", "HMSET", "EVAL")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return redisMethods.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<RedisRequest> {
        return RedisRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<RedisRequest> {
        return RedisRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return redisMethods
    }
}