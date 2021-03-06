package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.plugins.httpx.json.JsonUtils
import java.net.URL

class ProductName {
    var en: String? = null
    var zh: String? = null
}

class Products {
    lateinit var products: List<Product>

    companion object {
        private var instance: Products? = null

        fun instance(): Products {
            if (instance == null) {
                val jsonURL = Products::class.java.getResource("/aliyun/products.json")!!
                instance = JsonUtils.objectMapper.readValue<Products>(jsonURL)
            }
            return instance!!
        }
    }

    fun findAllProductCodes(): List<String> {
        return products.map { it.code }
    }

    fun findProduct(name: String): Product? {
        return products.find { name.equals(it.code, true) }
    }

    fun findProductByHost(host: String): Product? {
        return products.find { it.hasEndpoint(host) }
    }
}

class Product {
    lateinit var code: String
    lateinit var version: String
    var name: ProductName? = null
    var location_service_code: String? = null
    var global_endpoint: String? = null
    var regional_endpoints: Map<String, String>? = null
    lateinit var apis: List<String>
    var requestActions: MutableMap<String, Action> = mutableMapOf()

    fun getRegionalEndpoints(): Map<String, String> {
        return regional_endpoints ?: emptyMap()
    }

    fun hasGlobalEndpoint(): Boolean {
        return global_endpoint != null && global_endpoint!!.isNotEmpty()
    }

    fun hasEndpoint(host: String): Boolean {
        if (global_endpoint != null && host == global_endpoint) {
            return true
        }
        if (regional_endpoints != null && regional_endpoints!!.isNotEmpty()) {
            for (endpoint in regional_endpoints!!.values) {
                if (endpoint == host) {
                    return true
                }
            }
        }
        return false
    }

    fun findEndpointByRegion(regionId: String?): String {
        if (regionId != null && regional_endpoints != null) {
            if (regional_endpoints!!.contains(regionId)) {
                return regional_endpoints!![regionId]!!
            }
        }
        if (global_endpoint != null && global_endpoint!!.isNotEmpty()) {
            return global_endpoint!!
        }
        return "${code.toLowerCase()}.aliyuncs.com"
    }

    fun findAction(name: String): Action? {
        if (requestActions.contains(name)) {
            return requestActions[name]
        }
        if (this.apis.contains(name)) {
            val actionJsonUrl = "https://raw.githubusercontent.com/aliyun/aliyun-openapi-meta/master/metadatas/${this.code.toLowerCase()}/${name}.json"
            return try {
                JsonUtils.objectMapper.readValue<Action>(URL(actionJsonUrl)).also {
                    requestActions[name] = it
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}


class Action {
    lateinit var name: String
    var parameters: List<Parameter> = emptyList()

    fun convertToJsonSchema(product: Product): String {
        val jsonSchema = mutableMapOf<String, Any>()
        jsonSchema["\$schema"] = "http://json-schema.org/draft-07/schema#"
        jsonSchema["description"] = "JSON Schema for $name action of ${product.code}"
        jsonSchema["type"] = "object"
        val properties = mutableMapOf<String, Any>()
        val requiredProperties = mutableListOf<String>()
        for (parameter in parameters) {
            val jsonType = when (parameter.type) {
                "Boolean" -> "boolean"
                "Integer" -> "integer"
                "Long" -> "number"
                "Float" -> "number"
                "Double" -> "number"
                "Json" -> "object"
                "RepeatList" -> "array"
                else -> "string"
            }
            val paramName = parameter.name
            val paramMeta = mutableMapOf<String, Any>()
            paramMeta["type"] = jsonType
            paramMeta["x-intellij-html-description"] = """
                <a href='https://next.api.aliyun.com/api/${product.code}/${product.version}/${name}?tab=DOC'>${paramName}</a>
            """.trimIndent()
            if (paramName.endsWith("RegionId")) {
                paramMeta["enum"] = Aliyun.GLOBAL_REGIONS
            }
            if (jsonType == "array") {
                val subParameters = parameter.sub_parameters
                if (subParameters != null && subParameters.isNotEmpty()) {
                    paramMeta["items"] = convertToObjectArray(subParameters, product)
                }
            }
            properties[paramName] = paramMeta
            if (parameter.required) {
                requiredProperties.add(parameter.name)
            }
        }
        jsonSchema["properties"] = properties
        if (requiredProperties.isNotEmpty()) {
            jsonSchema["required"] = requiredProperties
        }
        return JsonUtils.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema)
    }

    private fun convertToObjectArray(subParams: List<Parameter>, product: Product): Map<String, Any> {
        val subParamsSchema = mutableMapOf<String, Any>()
        subParamsSchema["type"] = "object"
        val properties = mutableMapOf<String, Any>()
        val requiredProperties = mutableListOf<String>()
        for (parameter in subParams) {
            val jsonType = when (parameter.type) {
                "Boolean" -> "boolean"
                "Integer" -> "integer"
                "Long" -> "number"
                "Float" -> "number"
                "Double" -> "number"
                else -> "string"
            }
            val paramName = parameter.name
            val paramMeta = mutableMapOf<String, Any>()
            paramMeta["type"] = jsonType
            paramMeta["x-intellij-html-description"] = """
                       <a href='https://next.api.aliyun.com/api/${product.code}/${product.version}/${name}?tab=DOC'>${paramName}</a>
                   """.trimIndent()
            if (paramName.endsWith("RegionId")) {
                paramMeta["enum"] = Aliyun.GLOBAL_REGIONS
            }
            properties[paramName] = paramMeta
            if (parameter.required) {
                requiredProperties.add(parameter.name)
            }
        }
        subParamsSchema["properties"] = properties
        if (requiredProperties.isNotEmpty()) {
            subParamsSchema["required"] = requiredProperties
        }
        return subParamsSchema
    }

    class Parameter {
        lateinit var name: String
        lateinit var type: String
        var required: Boolean = false
        var sub_parameters: List<Parameter>? = null
    }
}