package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.lettuce.core.RedisClient
import io.lettuce.core.ScriptOutputType
import org.jetbrains.plugins.httpx.json.JsonUtils.objectMapper
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.TextBodyFileHint
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.json.Path
import redis.clients.jedis.json.Path2

@Suppress("UnstableApiUsage")
class RedisRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: RedisRequest): CommonClientResponse {
        val method = request.httpMethod!!
        if (method.startsWith("JSON")) {
            return redisJsonFunctions(request)
        }
        try {
            if (method == "LOAD") {
                return loadRedisFunctions(request);
            }
            val redisClient = RedisClient.create(request.uri.toString())
            val result = redisClient.connect().use {
                val commands = it.sync()
                when (method) {
                    "RSET" -> {
                        commands.set(request.key, request.bodyText())
                    }

                    "HMSET" -> {
                        val params = objectMapper.readValue<Map<String, Any>>(request.bodyText())
                        val redisParams = mutableMapOf<String, String>()
                        params.forEach { entry ->
                            val value = entry.value
                            if (value is String) {
                                redisParams[entry.key] = value
                            } else {
                                redisParams[entry.key] = value.toString()
                            }
                        }
                        commands.hmset(request.key, redisParams)
                    }

                    "EVAL" -> {
                        val key = request.key
                        if (key == null || key.isEmpty() || key == "0") {
                            commands.eval(request.bodyText(), ScriptOutputType.VALUE)
                        } else {
                            val parts = key.split("\\s+".toRegex())
                            val paramCount = parts[0].toInt()
                            val keys: List<String> = parts.subList(1, paramCount + 1)
                            val args: List<String> = parts.subList(paramCount + 1, parts.size)
                            commands.eval(request.bodyText(), ScriptOutputType.VALUE, keys.toTypedArray(), *args.toTypedArray())
                        }
                    }

                    else -> {
                        ""
                    }
                }
            }
            return if (result != null) {
                val acceptContentType = request.getHeader("Accept", "text/plain")
                if (acceptContentType.contains("json") || (result.startsWith("{") && result.endsWith("}"))) {
                    RedisResponse(CommonClientResponseBody.Text(result, JsonBodyFileHint.jsonBodyFileHint("redis-result.json")))
                } else {
                    RedisResponse(CommonClientResponseBody.Text(result, TextBodyFileHint.textBodyFileHint("redis-result.txt")))
                }
            } else {
                RedisResponse()
            }
        } catch (e: Exception) {
            return RedisResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun loadRedisFunctions(request: RedisRequest): CommonClientResponse {
        try {
            val redisUri = request.uri
            var luaScript = request.bodyText()
            val libName = request.key
            if (!luaScript.startsWith("#!lua")) {
                luaScript = "#!lua name=$libName\n$luaScript"
            }
            Jedis(redisUri.toString()).use { jedis ->
                val result = jedis.functionLoadReplace(luaScript)
                return if (result == null || result != libName) {
                    RedisResponse(CommonClientResponseBody.Empty(), "Error", result)
                } else {
                    RedisResponse(CommonClientResponseBody.Text(result, TextBodyFileHint.textBodyFileHint("redis-result.txt")))
                }
            }
        } catch (e: Exception) {
            return RedisResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun redisJsonFunctions(request: RedisRequest): CommonClientResponse {
        val method = request.httpMethod!!
        try {
            UnifiedJedis(HostAndPort(request.uri!!.host, request.uri!!.port)).use { jedis ->
                var jsonKey = request.key!!
                var jsonPath = "$"
                if (jsonKey.contains("/$")) {
                    jsonPath = jsonKey.substring(jsonKey.indexOf("/$") + 1)
                    jsonKey = jsonKey.substring(0, jsonKey.indexOf("/$"))
                }
                return when (method) {
                    "JSONGET" -> {
                        val json = jedis.jsonGet(jsonKey, Path.of(jsonPath))
                        if (json == null) {
                            return RedisResponse(CommonClientResponseBody.Empty(), "Error", "No value found for ${request.key}")
                        } else {
                            val jsonText = objectMapper.writeValueAsString(json)
                            RedisResponse(CommonClientResponseBody.Text(jsonText, JsonBodyFileHint.jsonBodyFileHint("redis-result.json")))
                        }
                    }

                    "JSONSET" -> {
                        jedis.jsonSet(jsonKey, Path2.of(jsonPath), request.bodyText())
                        RedisResponse()
                    }

                    else -> {
                        RedisResponse(CommonClientResponseBody.Empty(), "Error", "Unknown method $method")
                    }
                }
            }
        } catch (e: Exception) {
            return RedisResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

}