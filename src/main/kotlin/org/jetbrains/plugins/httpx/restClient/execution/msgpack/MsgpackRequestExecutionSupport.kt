package org.jetbrains.plugins.httpx.restClient.execution.msgpack

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class MsgpackRequestExecutionSupport : RequestExecutionSupport<MsgpackRequest> {
    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "MSGPACK" || requestContext.method == "NVIM"
    }

    override fun getRequestConverter(): RequestConverter<MsgpackRequest> {
        return MsgpackRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<MsgpackRequest> {
        return MsgpackRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("MSGPACK", "NVIM")
    }

    override val needsScheme: Boolean
        get() = false
}