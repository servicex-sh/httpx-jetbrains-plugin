package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class SubscribeRequestExecutionSupport : RequestExecutionSupport<SubscribeRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "SUB"
    }

    override fun getRequestConverter(): RequestConverter<SubscribeRequest> {
        return SubscribeRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<SubscribeRequest> {
        return SubscribeRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("SUB")
    }

    override val needsScheme: Boolean
        get() = false
}