package org.jetbrains.plugins.httpx.restClient.execution.aws

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class AwsRequestExecutionSupport : RequestExecutionSupport<AwsRequest> {
    companion object {
        val AWS_METHODS = listOf("AWS", "AWSPUT", "AWSDELETE", "AWSPOST")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return AWS_METHODS.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<AwsRequest> {
        return AwsRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<AwsRequest> {
        return AwsRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return AWS_METHODS
    }

    override val needsScheme: Boolean
        get() = true
    override val supportedSchemes: List<String>
        get() = listOf("https")
}