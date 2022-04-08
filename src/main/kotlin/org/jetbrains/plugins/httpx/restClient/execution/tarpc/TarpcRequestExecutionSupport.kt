package org.jetbrains.plugins.httpx.restClient.execution.tarpc

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler
import org.jetbrains.plugins.httpx.restClient.execution.thrift.ThriftRequest
import org.jetbrains.plugins.httpx.restClient.execution.thrift.ThriftRequestConverter
import org.jetbrains.plugins.httpx.restClient.execution.thrift.ThriftRequestHandler

@Suppress("UnstableApiUsage")
class TarpcRequestExecutionSupport : RequestExecutionSupport<TarpcRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "TARPC"
    }

    override fun getRequestConverter(): RequestConverter<TarpcRequest> {
        return TarpcRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<TarpcRequest> {
        return TarpcRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("TARPC")
    }
}