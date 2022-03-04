package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class SubscribeRequestHandler : RequestHandler<SubscribeRequest> {

    override fun execute(request: SubscribeRequest, runContext: RunContext): CommonClientResponse {
        val subscribeRequestManager = runContext.project.getService(SubscribeRequestManager::class.java)
        return subscribeRequestManager.subscribe(request)
    }

    override fun prepareExecutionEnvironment(request: SubscribeRequest, runContext: RunContext) {

    }
}