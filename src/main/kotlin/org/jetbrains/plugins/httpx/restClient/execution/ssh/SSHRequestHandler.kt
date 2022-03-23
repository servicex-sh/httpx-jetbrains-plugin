package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class SSHRequestHandler : RequestHandler<SSHRequest> {

    override fun execute(request: SSHRequest, runContext: RunContext): CommonClientResponse {
        val sshRequestManager = runContext.project.getService(SSHRequestManager::class.java)
        return sshRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: SSHRequest, runContext: RunContext) {

    }
}