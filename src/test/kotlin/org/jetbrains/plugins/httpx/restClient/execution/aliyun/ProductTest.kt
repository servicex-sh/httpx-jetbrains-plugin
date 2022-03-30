package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.plugins.httpx.json.JsonUtils.objectMapper
import org.junit.Test
import java.net.URL

class ProductTest {
    @Test
    fun testParse() {
        val products = Products.instance()
        println(products.products.size)
        val arms = products.findProduct("arms")!!
        val action = arms.findAction("ConfigApp")!!
        println(action.convertToJsonSchema(arms))
    }

    @Test
    fun testActionParse() {
        val url = "https://raw.githubusercontent.com/aliyun/aliyun-openapi-meta/master/metadatas/arms/ConfigApp.json";
        val action = objectMapper.readValue<Action>(URL(url))
        println(action.parameters.size)
    }

    @Test
    fun testSubParameter() {
        val products = Products.instance()
        val alidns = products.findProduct("alidns")!!
        val action = alidns.findAction("AddGtmAddressPool")!!
        println(action.convertToJsonSchema(alidns))
    }
}