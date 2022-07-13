package org.jetbrains.plugins.httpx.restClient.execution.email

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class MailRequestExecutionSupport : RequestExecutionSupport<MailRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "MAIL"
    }

    override fun getRequestConverter(): RequestConverter<MailRequest> {
        return MailRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<MailRequest> {
        return MailRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("MAIL")
    }

    override val needsScheme: Boolean
        get() = false
}