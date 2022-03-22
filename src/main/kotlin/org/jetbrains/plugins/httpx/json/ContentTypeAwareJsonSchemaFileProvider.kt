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
open class ContentTypeAwareJsonSchemaFileProvider(val contentType: String, val jsonSchemaPath: String) : ContentAwareJsonSchemaFileProvider {

    override fun getSchemaFile(psiFile: PsiFile): VirtualFile? {
        if (psiFile is JsonFile) {
            val injectedLanguageManager = InjectedLanguageManager.getInstance(psiFile.project)
            val psiLanguageInjectionHost = injectedLanguageManager.getInjectionHost(psiFile)
            if (psiLanguageInjectionHost is HttpMessageBody) {
                val httpMessageBody: HttpMessageBody = psiLanguageInjectionHost
                val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)!!
                val headerField = httpRequest.getHeaderField("Content-Type")
                if (headerField != null) {
                    var contentTypeValue = headerField.headerFieldValue?.text
                    if (contentTypeValue != null) {
                        if (contentTypeValue.contains(';')) { // Content-Type: application/graphql+json; charset=utf-8
                            contentTypeValue = contentTypeValue.substring(0, contentTypeValue.indexOf(';')).trim()
                        }
                        if (contentTypeValue == contentType) {
                            return VfsUtil.findFileByURL(this.javaClass.getResource(jsonSchemaPath)!!)
                        }
                    }
                }
            }
        }
        return null
    }
}