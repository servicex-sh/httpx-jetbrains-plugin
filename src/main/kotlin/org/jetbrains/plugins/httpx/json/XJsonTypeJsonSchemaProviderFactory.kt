package org.jetbrains.plugins.httpx.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider

@Suppress("UnstableApiUsage")
class XJsonTypeJsonSchemaProviderFactory : ContentAwareJsonSchemaFileProvider {
    companion object {
        val schemaStore = HashMap<String, VirtualFile>()
    }

    override fun getSchemaFile(psiFile: PsiFile): VirtualFile? {
        if (psiFile is JsonFile) {
            val injectedLanguageManager = InjectedLanguageManager.getInstance(psiFile.project)
            val psiLanguageInjectionHost = injectedLanguageManager.getInjectionHost(psiFile)
            if (psiLanguageInjectionHost is HttpMessageBody) {
                val httpMessageBody: HttpMessageBody = psiLanguageInjectionHost
                val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)!!
                val xJsonType = httpRequest.getHeaderField("X-JSON-Type")
                if (xJsonType != null) {
                    val jsonType = xJsonType.getValue(HttpRequestVariableSubstitutor.getDefault(psiFile.project)).trim()
                    return getJsonSchema(jsonType)
                }
            }
        }
        return null
    }

    private fun getJsonSchema(jsonType: String): VirtualFile {
        if (!schemaStore.contains(jsonType)) {
            val jsonSchemaFileName = "schema-${jsonType.hashCode()}.json"
            val jsonSchema = convertTypeToSchema(jsonType)
            val jsonSchemaFile = LightVirtualFile(jsonSchemaFileName, JsonFileType.INSTANCE, jsonSchema)
            schemaStore[jsonType] = jsonSchemaFile;
            return jsonSchemaFile
        }
        return schemaStore[jsonType]!!
    }

    fun convertTypeToSchema(jsonType: String): String {
        val jsonSchema = mutableMapOf<String, Any>()
        jsonSchema["\$schema"] = "http://json-schema.org/draft-07/schema#"
        if (jsonType.startsWith('[') && jsonType.endsWith(']')) {
            jsonSchema.putAll(convertArray(jsonType))
        } else if (jsonType.startsWith('{') && jsonType.endsWith('}')) {
            jsonSchema.putAll(convertObject(jsonType))
        } else if (jsonType.endsWith("[]")) { //array: string[]
            jsonSchema["type"] = "array"
            jsonSchema["items"] = mapOf("type" to jsonType.substring(0, jsonType.indexOf('[')))
        } else {
            jsonSchema["type"] = jsonType
        }
        return ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema)
    }

    private fun convertArray(jsonArrayType: String): Map<String, Any> {
        val schemaObject = mutableMapOf<String, Any>()
        var plainText = jsonArrayType
        if (plainText.startsWith("[")) {
            plainText = plainText.substring(1, plainText.length - 1)
        }
        val items = mutableListOf<MutableMap<String, Any>>()
        val subElements = mutableMapOf<String, Any>()
        while (plainText.contains('{') && plainText.contains('}')) {
            val subType = plainText.substring(plainText.indexOf('{'), plainText.lastIndexOf('}') + 1)
            val subElementId = "object@${subType.hashCode()}"
            subElements.put(subElementId, convertObject(subType))
            plainText = plainText.replace(subType, subElementId)
        }
        while (plainText.contains('[') && plainText.contains(']')) {
            val subType = plainText.substring(plainText.indexOf('['), plainText.lastIndexOf(']') + 1)
            val subElementId = "array@${subType.hashCode()}"
            subElements.put(subElementId, convertArray(subType))
            plainText = plainText.replace(subType, subElementId)
        }
        val types = plainText.split(",")
        for (type in types) {
            val typeName = type.trim().lowercase()
            if (typeName.isNotEmpty()) {
                if (typeName.endsWith("#array")) {
                    val arrayType = mutableMapOf<String, Any>()
                    arrayType["type"] = "array"
                    val itemType = type.substring(0, type.indexOf('#'))
                    arrayType["items"] = mapOf("type" to itemType)
                    items.add(arrayType)
                } else {
                    items.add(mutableMapOf("type" to typeName))
                }
            }
        }
        for (item in items) {
            val type = item["type"].toString()
            if (type.contains("@") && subElements.contains(type)) { // refer other type
                item.clear()
                @Suppress("UNCHECKED_CAST")
                item.putAll(subElements[type] as MutableMap<String, Any>)
            }
        }
        schemaObject["type"] = "array"
        if (items.isNotEmpty()) {
            schemaObject["items"] = items
        }
        return schemaObject;
    }

    private fun convertObject(jsonObjectType: String): Map<String, Any> {
        val schemaObject = mutableMapOf<String, Any>()
        var plainText = jsonObjectType
        if (plainText.contains("[]")) {
            plainText = plainText.replace("\\[]".toRegex(), "#array")
        }
        if (plainText.startsWith("{")) {
            plainText = plainText.substring(1, plainText.length - 1)
        }
        val properties = mutableMapOf<String, MutableMap<String, Any>>()
        val subElements = mutableMapOf<String, Any>()
        while (plainText.contains('{') && plainText.contains('}')) {
            val subType = plainText.substring(plainText.indexOf('{'), plainText.lastIndexOf('}') + 1)
            val subElementId = "object@${subType.hashCode()}"
            subElements.put(subElementId, convertObject(subType))
            plainText = plainText.replace(subType, subElementId)
        }
        while (plainText.contains('[') && plainText.contains(']')) {
            val subType = plainText.substring(plainText.indexOf('['), plainText.lastIndexOf(']') + 1)
            val subElementId = "array@${subType.hashCode()}"
            subElements.put(subElementId, mapOf("type" to "array", "items" to convertArray(subType)))
            plainText = plainText.replace(subType, subElementId)
        }
        val pairs = plainText.split(",")
        val requiredProperties = mutableListOf<String>()
        for (pair in pairs) {
            if (pair.contains(":")) {
                val parts = pair.trim().split("[:\\s]+".toRegex())
                var name = parts[0].trim()
                if (name.endsWith("?")) {
                    name = name.substring(0, name.length - 1)
                } else {
                    requiredProperties.add(name);
                }
                val type = parts[1].trim().lowercase()
                properties[name] = mutableMapOf("type" to type)
            }
        }
        if (requiredProperties.isNotEmpty()) {
            schemaObject["required"] = requiredProperties
        }
        for (entry in properties) {
            val obj = entry.value;
            val type = obj["type"] as String
            if (type.contains("@") && subElements.contains(type)) {
                @Suppress("UNCHECKED_CAST")
                properties[entry.key] = subElements[type] as MutableMap<String, Any>
            } else if (type.endsWith("#array")) {
                obj["type"] = "array"
                val itemType = type.substring(0, type.indexOf('#')).lowercase()
                obj["items"] = mapOf("type" to itemType)
            } else if (type.startsWith("set<") && type.endsWith(">")) { // Set<string>
                obj["type"] = "array"
                val itemType = type.substring(type.indexOf('<') + 1, type.indexOf('>')).lowercase()
                obj["items"] = mapOf("type" to itemType)
            }
        }
        schemaObject["type"] = "object"
        if (properties.isNotEmpty()) {
            schemaObject["properties"] = properties
        }
        return schemaObject
    }

}