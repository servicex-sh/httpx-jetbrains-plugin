package org.jetbrains.plugins.httpx.restClient.execution.thrift

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class ThriftRequestExecutionSupport : RequestExecutionSupport<ThriftRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "THRIFT"
    }

    override fun getRequestConverter(): RequestConverter<ThriftRequest> {
        return ThriftRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<ThriftRequest> {
        return ThriftRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("THRIFT")
    }
}