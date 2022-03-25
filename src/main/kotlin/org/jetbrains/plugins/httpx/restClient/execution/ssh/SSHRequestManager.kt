package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.jcraft.jsch.JSch
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import java.net.URI
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class SSHRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: SSHRequest): CommonClientResponse {
        val sshURI: URI = request.uri
        var session: com.jcraft.jsch.Session? = null
        var channel: com.jcraft.jsch.ChannelExec? = null
        try {
            val jsch = JSch()
            var sshPort = sshURI.port
            if (sshPort <= 0) {
                sshPort = 22
            }
            var userName: String? = null
            var password: String? = null
            val userInfo: String? = sshURI.userInfo
            if (userInfo != null && userInfo.isNotEmpty()) {
                if (userInfo.contains(":")) {
                    val parts = userInfo.split(":".toRegex(), limit = 2).toTypedArray()
                    userName = parts[0]
                    password = parts[1]
                } else {
                    userName = userInfo
                }
            }
            val basicAuthorization = request.getBasicAuthorization()
            if (basicAuthorization != null) { //username and password login
                userName = basicAuthorization[0]
                password = basicAuthorization[1]
            }
            if (userName != null && password != null) { //login by username and password
                session = jsch.getSession(userName, sshURI.host, sshPort)
                session.setPassword(password)
            } else { //login by private key
                var privateKey = request.getHeader("X-SSH-Private-Key")
                if (privateKey == null) {
                    privateKey = System.getProperty("user.home") + "/.ssh/id_rsa"
                }
                if (!java.io.File(privateKey).exists()) {
                    return SSHResponse(CommonClientResponseBody.Empty(), "Error", "Failed to load SSH private key: $privateKey")
                }
                jsch.addIdentity(privateKey)
                session = jsch.getSession(userName, sshURI.host, sshPort)
            }
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()
            channel = session.openChannel("exec") as com.jcraft.jsch.ChannelExec
            channel.setCommand(cleanUpCommands(request.body))
            val responseStream: java.io.ByteArrayOutputStream = java.io.ByteArrayOutputStream()
            channel.outputStream = responseStream
            channel.connect()
            while (channel.isConnected) {
                Thread.sleep(100)
            }
            val content = responseStream.toString(StandardCharsets.UTF_8)
            return SSHResponse(CommonClientResponseBody.Text(content, TextBodyFileHint.textBodyFileHint("ssh-result.txt")))
        } catch (e: Exception) {
            return SSHResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        } finally {
            session?.disconnect()
            channel?.disconnect()
        }
    }

    private fun cleanUpCommands(body: String): String {
        val builder = StringBuilder()
        body.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotEmpty() && !line.startsWith("#")) {
                if (line.endsWith("\\")) { //concat next line
                    builder.append(rawLine, 0, rawLine.lastIndexOf('\\'))
                } else {
                    builder.append(rawLine).append("; ")
                }
            }
        }
        val script = builder.toString()
        return script.substring(0, script.length - 2)
    }

}
