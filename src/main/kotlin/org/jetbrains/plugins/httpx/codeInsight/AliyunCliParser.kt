package org.jetbrains.plugins.httpx.codeInsight

import org.apache.tools.ant.types.Commandline
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Product

class AliyunCliParser(val text: String) {
    val serviceCode: String
    private val actionName: String
    private val params: Array<String>?

    init {
        // aliyun alidns AddGtmAddressPool --region cn-qingdao --InstanceId xxx
        val parts = text.split(' ', ignoreCase = false, limit = 4)
        serviceCode = parts[1]
        actionName = parts[2]
        params = try {
            if (parts.size > 3) {
                Commandline.translateCommandline(parts[3])
            } else {
                null
            }
        } catch (ignore: Exception) {
            null
        }
    }

    fun httpRequestCode(product: Product): String {
        val builder = StringBuilder()
        builder.append('\n')
        builder.append("### aliyun request converted from cli: ").append(text).append('\n')
        val paramsMap = if (params != null) {
            val len = params.size
            val tempMap = mutableMapOf<String, Any>()
            for (i in 0 until len step 2) {
                var name = params[i]
                if (name.startsWith("--")) {
                    name = name.substring(2)
                }
                val value = if (len > i + 1) {
                    params[i + 1]
                } else {
                    ""
                }
                if (value.matches("[1-9]\\d{0,8}".toRegex())) {
                    tempMap[name] = value.toInt()
                } else {
                    tempMap[name] = value
                }
            }
            tempMap
        } else {
            mutableMapOf()
        }
        val regionId = if (paramsMap.containsKey("region")) {
            paramsMap.remove("region") as String
        } else {
            null
        }
        val host = product.findEndpointByRegion(regionId)
        builder.append("ALIYUN ").append(host).append("?Action=").append(actionName).append('\n')
        if (regionId != null) {
            builder.append("X-Region-Id: ").append(regionId).append('\n')
        }
        if (paramsMap.isNotEmpty()) {
            // clean sub params
            val subParams: MutableMap<String, MutableMap<Int, MutableMap<String, Any>>> = mutableMapOf()
            val removedKeys = mutableListOf<String>()
            paramsMap.filter { it.key.contains(".") }
                .forEach { (name, value) ->
                    val parts = name.split('.', ignoreCase = false, limit = 3)
                    val mainName = parts[0]
                    val position = parts[1].toInt()
                    val subName = parts[2]
                    if (!subParams.containsKey(mainName)) {
                        subParams[mainName] = mutableMapOf()
                    }
                    if (!subParams[mainName]!!.containsKey(position)) {
                        subParams[mainName]!![position] = mutableMapOf()
                    }
                    subParams[mainName]!![position]!![subName] = value
                    removedKeys.add(name)
                }
            removedKeys.forEach { paramsMap.remove(it) }
            subParams.forEach { (name, subParamsPairs) ->
                val paramsList = mutableListOf<Map<String, Any>>()
                subParamsPairs.keys.toSortedSet().forEach {
                    paramsList.add(subParamsPairs[it]!!)
                }
                paramsMap[name] = paramsList
            }
            builder.append("Content-Type: application/json").append('\n')
            builder.append('\n')
            builder.append(JsonUtils.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(paramsMap))
            builder.append('\n')
        }
        return builder.toString()
    }

}