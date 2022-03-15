package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class AliyunRequestHandler : RequestHandler<AliyunRequest> {

    override fun execute(request: AliyunRequest, runContext: RunContext): CommonClientResponse {
        val aliyunRequestManager = runContext.project.getService(AliyunRequestManager::class.java)
        return aliyunRequestManager.execute(request)
    }

    override fun prepareExecutionEnvironment(request: AliyunRequest, runContext: RunContext) {

    }
}