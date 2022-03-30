package org.jetbrains.plugins.httpx.json

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
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Products

@Suppress("UnstableApiUsage")
class AliyunRequestActionJsonSchemaProviderFactory : ContentAwareJsonSchemaFileProvider {
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
                if (httpRequest.httpMethod == "ALIYUN") {
                    val requestTarget = httpRequest.requestTarget
                    if (requestTarget != null) {
                        val host = requestTarget.host!!.text
                        val actionParam = requestTarget.query!!.queryParameterList.find { it.queryParameterKey.text == "Action" }
                        if (actionParam != null) {
                            val actionName = actionParam.queryParameterValue!!.text
                            if (actionName != null) {
                                return getJsonSchema(host, actionName)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun getJsonSchema(host: String, actionName: String): VirtualFile? {
        val key = "${host}-${actionName}"
        if (schemaStore.contains(key)) {
            return schemaStore[key]
        }
        val jsonSchemaFileName = "schema-${key}.json"
        val product = Products.instance().findProductByHost(host)
        if (product != null) {
            val action = product.findAction(actionName)
            if (action != null) {
                val jsonSchema = action.convertToJsonSchema(product)
                val jsonSchemaFile = LightVirtualFile(jsonSchemaFileName, JsonFileType.INSTANCE, jsonSchema)
                schemaStore[key] = jsonSchemaFile;
                return jsonSchemaFile
            }
        }
        return null
    }

}