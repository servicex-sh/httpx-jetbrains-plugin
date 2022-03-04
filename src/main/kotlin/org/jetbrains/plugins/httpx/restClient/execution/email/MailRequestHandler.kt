package org.jetbrains.plugins.httpx.restClient.execution.email

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class MailRequestHandler : RequestHandler<MailRequest> {

    override fun execute(request: MailRequest, runContext: RunContext): CommonClientResponse {
        val rsocketRequestManager = runContext.project.getService(MailRequestManager::class.java)
        return rsocketRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: MailRequest, runContext: RunContext) {

    }
}