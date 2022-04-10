package org.jetbrains.plugins.httpx.restClient.execution.msgpack

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.msgpack.jackson.dataformat.MessagePackMapper
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

@Suppress("UnstableApiUsage")
class MsgpackRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: MsgpackRequest): CommonClientResponse {
        try {
            val msgpackUri = request.uri
            var functionName = msgpackUri.path.substring(1)
            if (functionName.contains("/")) {
                functionName = functionName.substring(functionName.lastIndexOf('/') + 1)
            }
            var args = arrayOf<Any>()
            var body = request.jsonArrayBodyWithArgsHeaders()
            if (body.isNotEmpty()) {
                if (!body.startsWith("[")) {
                    body = "[$body]"
                }
                try {
                    args = JsonUtils.objectMapper.readValue<List<Any>>(body).toTypedArray()
                } catch (e: java.lang.Exception) {
                    return MsgpackResponse(CommonClientResponseBody.Empty(), "Error", "Failed to parse args: $body")
                }
            }
            val msgpackRequest: MutableList<Any> = ArrayList()
            msgpackRequest.add(0) //RPC request
            msgpackRequest.add(0) //msg id
            msgpackRequest.add(functionName)
            msgpackRequest.add(args)
            val objectMapper = MessagePackMapper()
            try {
                SocketChannel.open(InetSocketAddress(msgpackUri.host, msgpackUri.port)).use { socketChannel ->
                    val content = objectMapper.writeValueAsBytes(msgpackRequest)
                    socketChannel.write(ByteBuffer.wrap(content))
                    val data = extractData(socketChannel) ?: return MsgpackResponse(
                        CommonClientResponseBody.Empty(),
                        "Error",
                        "Failed to call remote service, please check function and arguments!"
                    )
                    val response = objectMapper.readValue<List<Any>>(data)
                    @Suppress("SENSELESS_COMPARISON")
                    if (response.size > 3 && response[3] != null) {
                        val resultJson = Gson().toJson(response[3])
                        return MsgpackResponse(CommonClientResponseBody.Text(resultJson, JsonBodyFileHint.jsonBodyFileHint("msgpack-result.json")))
                    } else {
                        val error = response[2]
                        if (error != null) {
                            return MsgpackResponse(CommonClientResponseBody.Empty(), "Error", Gson().toJson(error))
                        } else {
                            return MsgpackResponse(CommonClientResponseBody.Empty())
                        }
                    }
                }
            } catch (e: Exception) {
                return MsgpackResponse(CommonClientResponseBody.Empty(), "Error", e.message)
            }
        } catch (e: Exception) {
            return MsgpackResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun extractData(socketChannel: SocketChannel): ByteArray? {
        val bos = ByteArrayOutputStream()
        val buf = ByteBuffer.allocate(30720)
        var readCount: Int
        do {
            readCount = socketChannel.read(buf)
            if (readCount < 0) {
                return byteArrayOf()
            }
            bos.write(buf.array(), 0, readCount)
        } while (readCount == 30720)
        return bos.toByteArray()
    }

}