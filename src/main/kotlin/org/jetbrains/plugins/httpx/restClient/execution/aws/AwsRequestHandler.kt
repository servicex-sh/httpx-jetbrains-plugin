package org.jetbrains.plugins.httpx.restClient.execution.aws

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class AwsRequestHandler : RequestHandler<AwsRequest> {

    override fun execute(request: AwsRequest, runContext: RunContext): CommonClientResponse {
        val awsRequestManager = runContext.project.getService(AwsRequestManager::class.java)
        return awsRequestManager.execute(request)
    }

    override fun prepareExecutionEnvironment(request: AwsRequest, runContext: RunContext) {

    }
}