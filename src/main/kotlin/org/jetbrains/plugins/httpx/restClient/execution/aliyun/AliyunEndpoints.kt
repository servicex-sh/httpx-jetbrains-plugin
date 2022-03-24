package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.plugins.httpx.json.JsonUtils

class AliyunEndpoints {
    val endpoints: Map<String, Any>;

    init {
        endpoints = JsonUtils.objectMapper.readValue(AliyunEndpoints::class.java.getResource("/endpoints.json")!!)
    }

}