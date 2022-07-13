package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class SSHRequestExecutionSupport : RequestExecutionSupport<SSHRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "SSH"
    }

    override fun getRequestConverter(): RequestConverter<SSHRequest> {
        return SSHRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<SSHRequest> {
        return SSHRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("SSH")
    }

    override val needsScheme: Boolean
        get() = false
}