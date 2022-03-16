package org.jetbrains.plugins.httpx.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider
import kotlin.collections.set


@Suppress("UnstableApiUsage")
class XJavaTypeJsonSchemaProviderFactory : ContentAwareJsonSchemaFileProvider {
    companion object {
        val schemaStore = HashMap<String, VirtualFile>()
    }

    override fun getSchemaFile(psiFile: PsiFile): VirtualFile? {
        if (psiFile is JsonFile) {
            val project = psiFile.project
            val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
            val psiLanguageInjectionHost = injectedLanguageManager.getInjectionHost(psiFile)
            if (psiLanguageInjectionHost is HttpMessageBody) {
                val httpMessageBody: HttpMessageBody = psiLanguageInjectionHost
                val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)!!
                val xJavaType = httpRequest.getHeaderField("X-Java-Type")
                if (xJavaType != null) {
                    val javaType = xJavaType.getValue(HttpRequestVariableSubstitutor.getDefault(project)).trim()
                    return getJsonSchema(javaType, project)
                }
            }
        }
        return null
    }

    private fun getJsonSchema(javaType: String, project: Project): VirtualFile? {
        if (schemaStore.containsKey(javaType)) {
            return schemaStore[javaType]
        }
        val targetPsiClasses = JavaFullClassNameIndex.getInstance().get(javaType.hashCode(), project, GlobalSearchScope.allScope(project))
        if (targetPsiClasses != null && targetPsiClasses.isNotEmpty()) {
            val psiClass = targetPsiClasses.first()
            val modificationStamp = psiClass.containingFile.modificationStamp
            val cacheKey = "${javaType}-${modificationStamp}"
            if (schemaStore.containsKey(cacheKey)) {
                // return schemaStore[cacheKey]
            }
            val jsonSchemaFileName = "schema-${javaType.hashCode()}.json"
            val jsonSchema = convertTypeToSchema(psiClass)
            val jsonSchemaFile = LightVirtualFile(jsonSchemaFileName, JsonFileType.INSTANCE, jsonSchema)
            if (psiClass.isWritable) {
                schemaStore[cacheKey] = jsonSchemaFile
            } else {
                schemaStore[javaType] = jsonSchemaFile
            }
            return jsonSchemaFile
        }
        return null
    }

    private fun convertTypeToSchema(psiClass: PsiClass): String {
        val jsonSchema = mutableMapOf<String, Any>()
        jsonSchema["\$schema"] = "http://json-schema.org/draft-07/schema#"
        jsonSchema["type"] = "object"
        val properties = HashMap<String, Any>()
        psiClass.allFields.forEach {
            val name = it.name
            val typeName = it.type.canonicalText
            val jsonSchemaType = if (typeName == "java.lang.Integer") {
                "integer"
            } else if (typeName == "java.lang.Boolean") {
                "boolean"
            } else if (typeName == "java.lang.Double" || typeName == "java.lang.Float" || typeName == "java.lang.Long") {
                "number"
            } else if (typeName == "java.util.UUID") {
                "uuid"
            } else if (typeName == "java.net.URI" || typeName == "java.net.URL") {
                "uri"
            } else if (typeName == "java.util.Date" || typeName.contains("DateTime")) {
                "data-time"
            } else if (typeName.contains("Date")) {
                "date"
            } else if (typeName.contains("Time")) {
                "time"
            } else if (typeName == "java.lang.String") {
                "string"
            } else if (name.lowercase().contains("email")) {
                "email"
            } else if (typeName.contains("List<")) {
                "array"
            } else {
                "object"
            }

            properties[name] = mapOf("type" to jsonSchemaType)
        }
        jsonSchema["properties"] = properties
        return ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema)
    }


}