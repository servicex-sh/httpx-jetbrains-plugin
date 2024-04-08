package org.jetbrains.plugins.httpx.restClient.execution.tarpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class TarpcRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: TarpcRequest): CommonClientResponse {
        try {
            val tarpcUri = request.uri
            var functionName = tarpcUri.path.substring(1)
            if (functionName.contains("/")) {
                functionName = functionName.substring(functionName.lastIndexOf('/') + 1)
            }
            functionName = org.apache.commons.lang3.StringUtils.capitalize(functionName)
            val jsonRequest = """
            {
              "Request": {
                "context": {
                  "deadline": {
                    "secs": 9,
                    "nanos": 999641000
                  },
                  "trace_context": {
                    "trace_id": [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ],
                    "span_id": 481056967432925815,
                    "sampling_decision": "Unsampled"
                  }
                },
                "id": 0,
                "message": {
                  "$functionName": ${request.body}
                }
              }
            }    
            """.trimIndent()
            try {
                SocketChannel.open(InetSocketAddress(tarpcUri.host, tarpcUri.port)).use { socketChannel ->
                    //JsonUtils.objectMapper
                    val jsonText = convertToSerdeJson(JsonUtils.objectMapper, jsonRequest)
                    val content = jsonText.toByteArray(StandardCharsets.UTF_8)
                    val buffer = ByteBuffer.allocate(content.size + 4)
                    buffer.put(0x00.toByte())
                    buffer.put(0x00.toByte())
                    buffer.put(0x00.toByte())
                    buffer.put(0xEA.toByte())
                    // buffer.put((byte) 0x7B);
                    // buffer.put((byte) 0x22);
                    // buffer.put((byte) 0x52);
                    //buffer.put((byte) 0x65);
                    buffer.put(content)
                    buffer.rewind()
                    socketChannel.write(buffer)
                    val data = extractData(socketChannel) ?: return TarpcResponse(
                        CommonClientResponseBody.Empty(),
                        "Error",
                        "Failed to call remote service, please check function and arguments!"
                    )
                    val text = String(data, StandardCharsets.UTF_8)
                    val result = JsonUtils.objectMapper.readValue<Map<String, Any>>(text)
                    if (result.containsKey("message")) {
                        val message = result["message"] as Map<String, Any>
                        if (message.containsKey("Ok")) {
                            val okResult = message["Ok"] as Map<String, Any>
                            if (okResult.containsKey(functionName)) {
                                val resultJson = JsonUtils.objectMapper.writeValueAsString(okResult[functionName])
                                return TarpcResponse(CommonClientResponseBody.Text(resultJson, JsonBodyFileHint.jsonBodyFileHint("tarpc-result.json")))
                            }
                        }
                    }
                    return TarpcResponse(CommonClientResponseBody.Text(text, JsonBodyFileHint.jsonBodyFileHint("tarpc-result.json")))
                }
            } catch (e: Exception) {
                return TarpcResponse(CommonClientResponseBody.Empty(), "Error", e.message)
            }
        } catch (e: Exception) {
            return TarpcResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun extractData(socketChannel: SocketChannel): ByteArray? {
        val bos = ByteArrayOutputStream()
        val buf = ByteBuffer.allocate(4096)
        var readCount: Int
        var counter = 0
        do {
            readCount = socketChannel.read(buf)
            var startOffset = 0
            var length = readCount
            if (readCount < 0) {
                return null
            }
            if (counter == 0) {
                startOffset = 4
                length = readCount - 4
            }
            bos.write(buf.array(), startOffset, length)
            counter++
        } while (readCount == 4096)
        return bos.toByteArray()
    }

    private fun convertToSerdeJson(objectMapper: ObjectMapper, jsonText: String): String {
        val map = objectMapper.readValue(jsonText, Map::class.java)
        return objectMapper.writeValueAsString(map)
    }

}