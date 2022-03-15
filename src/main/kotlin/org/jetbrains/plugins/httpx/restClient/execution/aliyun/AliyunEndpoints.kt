package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class AliyunEndpoints {
    val endpoints: Map<String, Any>;

    init {
        endpoints = ObjectMapper().readValue(AliyunEndpoints::class.java.getResource("/endpoints.json")!!)
    }

}