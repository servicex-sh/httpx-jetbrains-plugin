package org.jetbrains.plugins.httpx.restClient.execution.jsonrpc

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class JsonRpcRequestExecutionSupport : RequestExecutionSupport<JsonRpcRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "JSONRPC"
    }

    override fun getRequestConverter(): RequestConverter<JsonRpcRequest> {
        return JsonRpcRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<JsonRpcRequest> {
        return JsonRpcRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("JSONRPC")
    }

    override val needsScheme: Boolean
        get() = true
    override val supportedSchemes: List<String>
        get() = listOf("http", "https", "tcp")
}