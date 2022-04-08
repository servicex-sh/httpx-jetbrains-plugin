package org.jetbrains.plugins.httpx.restClient.execution.tarpc

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class TarpcRequestHandler : RequestHandler<TarpcRequest> {

    override fun execute(request: TarpcRequest, runContext: RunContext): CommonClientResponse {
        val tarpcRequestManager = runContext.project.getService(TarpcRequestManager::class.java)
        return tarpcRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: TarpcRequest, runContext: RunContext) {

    }
}