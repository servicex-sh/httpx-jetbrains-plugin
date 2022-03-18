package org.jetbrains.plugins.httpx.restClient.execution.thrift

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class ThriftRequestHandler : RequestHandler<ThriftRequest> {

    override fun execute(request: ThriftRequest, runContext: RunContext): CommonClientResponse {
        val thriftRequestManager = runContext.project.getService(ThriftRequestManager::class.java)
        return thriftRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: ThriftRequest, runContext: RunContext) {

    }
}