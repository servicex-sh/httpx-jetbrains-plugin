package org.jetbrains.plugins.httpx.restClient.execution.msgpack

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext
import org.jetbrains.plugins.httpx.restClient.execution.tarpc.TarpcRequest
import org.jetbrains.plugins.httpx.restClient.execution.tarpc.TarpcRequestManager

@Suppress("UnstableApiUsage")
class MsgpackRequestHandler : RequestHandler<MsgpackRequest> {

    override fun execute(request: MsgpackRequest, runContext: RunContext): CommonClientResponse {
        val msgpackRequestManager = runContext.project.getService(MsgpackRequestManager::class.java)
        return msgpackRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: MsgpackRequest, runContext: RunContext) {

    }
}