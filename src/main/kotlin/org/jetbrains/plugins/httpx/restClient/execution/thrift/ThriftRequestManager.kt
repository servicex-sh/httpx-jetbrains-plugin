package org.jetbrains.plugins.httpx.restClient.execution.thrift

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.httpx.json.JsonUtils.objectMapper
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class ThriftRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: ThriftRequest): CommonClientResponse {
        try {
            val thriftUri = request.uri
            var serviceName: String = thriftUri.path.substring(1)
            if (serviceName.contains("/")) { //convert '/' to ':'
                serviceName = serviceName.replace('/', ':')
            }
            val builder = StringBuilder()
            builder.append('[').append('1').append(',') // tjson start and version
            builder.append('"').append(serviceName).append('"').append(',') //append service name
            builder.append('1').append(',') // call type - TMessageType.CALL
            builder.append('1').append(',') // message id
            //cleaned TJSON
            builder.append(convertToTJSON(objectMapper, request.textToSend!!)) // json args
            builder.append(']') //tjson lose
            val content = builder.toString().toByteArray(StandardCharsets.UTF_8)
            try {
                SocketChannel.open(InetSocketAddress(thriftUri.host, thriftUri.port)).use { socketChannel ->
                    val buffer = ByteBuffer.allocate(content.size + 4)
                    buffer.putInt(content.size)
                    buffer.put(content)
                    buffer.rewind()
                    socketChannel.write(buffer)
                    val data = extractData(socketChannel)
                    val text = String(data, StandardCharsets.UTF_8)
                    return ThriftResponse(CommonClientResponseBody.Text(text, JsonBodyFileHint.jsonBodyFileHint("thrift-result.json")))
                }
            } catch (e: Exception) {
                return ThriftResponse(CommonClientResponseBody.Empty(), "Error", e.message)
            }
        } catch (e: Exception) {
            return ThriftResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun extractData(socketChannel: SocketChannel): ByteArray {
        val bos = ByteArrayOutputStream()
        val buf = ByteBuffer.allocate(1024)
        var readCount: Int
        var counter = 0
        do {
            readCount = socketChannel.read(buf)
            var startOffset = 0
            var length = readCount
            if (counter == 0) {
                startOffset = 4
                length = readCount - 4
            }
            bos.write(buf.array(), startOffset, length)
            counter++
        } while (readCount == 1024)
        return bos.toByteArray()
    }

    private fun convertToTJSON(objectMapper: ObjectMapper, jsonText: String): String? {
        val map = objectMapper.readValue(jsonText, Map::class.java)
        return objectMapper.writeValueAsString(map)
    }

}