package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class PublishRequestHandler : RequestHandler<PublishRequest> {

    override fun execute(request: PublishRequest, runContext: RunContext): CommonClientResponse {
        val publishRequestManager = runContext.project.getService(PublishRequestManager::class.java)
        return publishRequestManager.publish(request)
    }

    override fun prepareExecutionEnvironment(request: PublishRequest, runContext: RunContext) {

    }
}