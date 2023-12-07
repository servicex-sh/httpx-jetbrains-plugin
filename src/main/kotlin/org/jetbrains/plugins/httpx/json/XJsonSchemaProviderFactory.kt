package org.jetbrains.plugins.httpx.json

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
import okhttp3.OkHttpClient
import okhttp3.Request

@Suppress("UnstableApiUsage")
class XJsonSchemaProviderFactory : ContentAwareJsonSchemaFileProvider {
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
                val xJsonSchema = httpRequest.getHeaderField("X-JSON-Schema")
                if (xJsonSchema != null) {
                    val jsonSchemaUrl = xJsonSchema.getValue(HttpRequestVariableSubstitutor.getDefault(psiFile.project, psiFile)).trim()
                    return getJsonSchema(jsonSchemaUrl)
                }
            }
        }
        return null
    }

    private fun getJsonSchema(jsonSchemaUrl: String): VirtualFile {
        if (!schemaStore.contains(jsonSchemaUrl)) {
            val jsonSchemaFileName = "schema-${jsonSchemaUrl.hashCode()}.json"
            val jsonSchema = getJsonSchemaContent(jsonSchemaUrl)
            val jsonSchemaFile = LightVirtualFile(jsonSchemaFileName, JsonFileType.INSTANCE, jsonSchema)
            schemaStore[jsonSchemaUrl] = jsonSchemaFile;
            return jsonSchemaFile
        }
        return schemaStore[jsonSchemaUrl]!!
    }

    private fun getJsonSchemaContent(jsonSchemaUrl: String): String {
        val client = OkHttpClient()
        client.newCall(Request.Builder().url(jsonSchemaUrl).build())
            .execute()
            .use { response -> return response.body()!!.string() }
    }

}