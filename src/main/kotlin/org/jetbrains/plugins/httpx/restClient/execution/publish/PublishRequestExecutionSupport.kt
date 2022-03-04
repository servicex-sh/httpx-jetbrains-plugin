package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class PublishRequestExecutionSupport : RequestExecutionSupport<PublishRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "PUB"
    }

    override fun getRequestConverter(): RequestConverter<PublishRequest> {
        return PublishRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<PublishRequest> {
        return PublishRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("PUB")
    }
}