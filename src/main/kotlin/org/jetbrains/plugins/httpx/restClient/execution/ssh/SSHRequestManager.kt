package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.jcraft.jsch.JSch
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import java.io.File
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
                val privateKey = request.getHeader("X-SSH-Private-Key")
                if (privateKey != null) {
                    if (!File(privateKey).exists()) {
                        return SSHResponse(CommonClientResponseBody.Empty(), "Error", "Failed to load SSH private key: $privateKey")
                    } else {
                        jsch.addIdentity(privateKey)
                    }
                } else {
                    // load private keys from $HOME/.ssh/
                    val sshDir = File(System.getProperty("user.home"), ".ssh")
                    if (sshDir.exists()) {
                        val privateFiles = sshDir.listFiles { _: File?, name: String -> name.startsWith("id_") && !name.contains(".") }
                        if (privateFiles != null && privateFiles.isNotEmpty()) {
                            for (privateFile in privateFiles) {
                                jsch.addIdentity(privateFile.absolutePath)
                            }
                        }
                    }
                }
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
            val outputContentType = request.getHeader("Accept");
            return if (outputContentType != null && outputContentType.contains("json")) {
                SSHResponse(CommonClientResponseBody.Text(content, JsonBodyFileHint.jsonBodyFileHint("ssh-result.json")))
            } else {
                SSHResponse(CommonClientResponseBody.Text(content, TextBodyFileHint.textBodyFileHint("ssh-result.txt")))
            }
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

