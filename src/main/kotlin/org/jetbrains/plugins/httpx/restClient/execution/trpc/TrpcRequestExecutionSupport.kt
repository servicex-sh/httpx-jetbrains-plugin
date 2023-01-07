package org.jetbrains.plugins.httpx.restClient.execution.trpc

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class TrpcRequestExecutionSupport : RequestExecutionSupport<TrpcRequest> {
    companion object {
        val TRPC_METHODS = listOf("TRPC", "TRPCQ", "TRPCM", "TRPCSUB")
        val TRPC_SCHEMAS = listOf("http", "https", "ws", "wss")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return TRPC_METHODS.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<TrpcRequest> {
        return TrpcRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<TrpcRequest> {
        return TrpcRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return TRPC_METHODS
    }

    override val needsScheme: Boolean
        get() = true
    override val supportedSchemes: List<String>
        get() = TRPC_SCHEMAS
}