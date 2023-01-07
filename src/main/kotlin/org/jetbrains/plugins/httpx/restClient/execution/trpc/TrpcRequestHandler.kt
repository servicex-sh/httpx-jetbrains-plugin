package org.jetbrains.plugins.httpx.restClient.execution.trpc

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class TrpcRequestHandler : RequestHandler<TrpcRequest> {

    override fun execute(request: TrpcRequest, runContext: RunContext): CommonClientResponse {
        val trpcRequestManager = runContext.project.getService(TrpcRequestManager::class.java)
        return trpcRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: TrpcRequest, runContext: RunContext) {

    }
}