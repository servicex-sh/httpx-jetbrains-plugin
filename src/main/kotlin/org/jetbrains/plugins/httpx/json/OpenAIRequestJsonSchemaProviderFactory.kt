package org.jetbrains.plugins.httpx.json

import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.json.psi.JsonFile
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider

@Suppress("UnstableApiUsage")
class OpenAIRequestJsonSchemaProviderFactory : ContentAwareJsonSchemaFileProvider {
    override fun getSchemaFile(psiFile: PsiFile): VirtualFile? {
        if (psiFile is JsonFile) {
            val injectedLanguageManager = InjectedLanguageManager.getInstance(psiFile.project)
            val psiLanguageInjectionHost = injectedLanguageManager.getInjectionHost(psiFile)
            if (psiLanguageInjectionHost is HttpMessageBody) {
                val httpMessageBody: HttpMessageBody = psiLanguageInjectionHost
                val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)!!
                if (httpRequest.httpMethod == "POST") {
                    val targetText = httpRequest.requestTarget?.text ?: ""
                    // add OpenAI and Azure OpenAI support
                    if (targetText.contains(".openai.") || targetText.contains("_OPENAI_")) {
                        if (targetText.contains("/chat/completions")) {
                            return VfsUtil.findFileByURL(this.javaClass.getResource("/chatgpt-request-schema.json")!!)
                        } else if (targetText.contains("/completions")) {
                            return VfsUtil.findFileByURL(this.javaClass.getResource("/openai-completion-request-schema.json")!!)
                        }
                    }
                }
            }
        }
        return null
    }
}