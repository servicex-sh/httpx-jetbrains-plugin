package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class ChatgptRequestHandler : RequestHandler<ChatgptRequest> {

    override fun execute(request: ChatgptRequest, runContext: RunContext): CommonClientResponse {
        val chatgptRequestManager = runContext.project.getService(ChatgptRequestManager::class.java)
        return chatgptRequestManager.execute(request)
    }

    override fun prepareExecutionEnvironment(request: ChatgptRequest, runContext: RunContext) {

    }
}