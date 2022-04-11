package org.jetbrains.plugins.httpx.restClient.execution.jsonrpc

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class JsonRpcRequestHandler : RequestHandler<JsonRpcRequest> {

    override fun execute(request: JsonRpcRequest, runContext: RunContext): CommonClientResponse {
        val jsonRpcRequestManager = runContext.project.getService(JsonRpcRequestManager::class.java)
        return jsonRpcRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: JsonRpcRequest, runContext: RunContext) {

    }
}