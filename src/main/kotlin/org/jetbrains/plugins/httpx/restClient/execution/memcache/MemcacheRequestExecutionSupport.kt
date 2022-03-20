package org.jetbrains.plugins.httpx.restClient.execution.memcache

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class MemcacheRequestExecutionSupport : RequestExecutionSupport<MemcacheRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "MEMCACHE"
    }

    override fun getRequestConverter(): RequestConverter<MemcacheRequest> {
        return MemcacheRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<MemcacheRequest> {
        return MemcacheRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("MEMCACHE")
    }
}