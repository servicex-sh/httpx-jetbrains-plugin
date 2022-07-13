package org.jetbrains.plugins.httpx.restClient.execution.rest

import com.intellij.httpClient.execution.RestClientRequest
import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.impl.HttpRequestHandler

@Suppress("UnstableApiUsage")
class JsonRestRequestExecutionSupport : RequestExecutionSupport<RestClientRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "REST"
    }

    override fun getRequestConverter(): RequestConverter<RestClientRequest> {
        return JsonRestRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<RestClientRequest> {
        return HttpRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("REST")
    }

    override val needsScheme: Boolean
        get() = true
    override val supportedSchemes: List<String>
        get() = listOf("http","https")
}