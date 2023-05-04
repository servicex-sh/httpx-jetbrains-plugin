package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class ChatgptRequestExecutionSupport : RequestExecutionSupport<ChatgptRequest> {

    companion object {
        val CHATGPT_METHODS = listOf("CHATGPT");
        val CHATGPT_SCHEMAS = listOf("https")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return CHATGPT_METHODS.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<ChatgptRequest> {
        return ChatgptRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<ChatgptRequest> {
        return ChatgptRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return CHATGPT_METHODS
    }

    override val needsScheme: Boolean
        get() = true

    override val supportedSchemes: List<String>
        get() = CHATGPT_SCHEMAS
}