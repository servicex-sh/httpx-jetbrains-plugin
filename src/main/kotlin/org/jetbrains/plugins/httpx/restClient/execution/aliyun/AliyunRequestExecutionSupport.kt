package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class AliyunRequestExecutionSupport : RequestExecutionSupport<AliyunRequest> {
    companion object {
        val ALIYUN_METHODS = listOf("ALIYUN")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return ALIYUN_METHODS.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<AliyunRequest> {
        return AliyunRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<AliyunRequest> {
        return AliyunRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return ALIYUN_METHODS
    }

    override val needsScheme: Boolean
        get() = true
    override val supportedSchemes: List<String>
        get() = listOf("https")
}