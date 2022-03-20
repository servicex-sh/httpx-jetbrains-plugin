package org.jetbrains.plugins.httpx.restClient.execution.memcache

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.spotify.folsom.BinaryMemcacheClient
import com.spotify.folsom.ConnectFuture
import com.spotify.folsom.MemcacheClientBuilder

@Suppress("UnstableApiUsage")
class MemcacheRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: MemcacheRequest): CommonClientResponse {
        var client: BinaryMemcacheClient<ByteArray>? = null
        try {
            val memcacheURI = request.uri!!
            val key = request.key!!
            var port = memcacheURI.port
            if (port <= 0) {
                port = 11211
            }
            client = MemcacheClientBuilder.newByteArrayClient()
                .withAddress(memcacheURI.host, port)
                .connectBinary()
            ConnectFuture.connectFuture(client).toCompletableFuture().get()
            val bodyBytes = request.bodyBytes()
            if (bodyBytes.isNotEmpty()) {  //set
                client.set(key, bodyBytes, 0).toCompletableFuture().get()
            } else if (key.startsWith("-")) { //delete
                val realKey = key.substring(1)
                client.delete(realKey).toCompletableFuture().get()
            } else {  //get
                val content = client.get(key).toCompletableFuture().get()
                return if (content.isNotEmpty()) {
                    MemcacheResponse(CommonClientResponseBody.Text(String(content)))
                } else {
                    MemcacheResponse(CommonClientResponseBody.Empty(), "OK", "Cache not found: " + key)
                }
            }
            return MemcacheResponse()
        } catch (e: Exception) {
            return MemcacheResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        } finally {
            client?.shutdown()
        }
    }

}