package org.jetbrains.plugins.httpx.injector

import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.lang.injection.general.Injection
import com.intellij.lang.injection.general.LanguageInjectionContributor
import com.intellij.lang.injection.general.SimpleInjection
import com.intellij.lang.jsgraphql.GraphQLLanguage
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * language injection for Content-Type header with application/graphql
 */
class GraphqlInjectionContributor : LanguageInjectionContributor {
    override fun getInjection(context: PsiElement): Injection? {
        if (context is HttpMessageBody) {
            val contentTypeHeader = context.getParentOfType<HttpRequest>(false)?.getHeaderField("Content-Type")
            if (contentTypeHeader != null) {
                val contentType = contentTypeHeader.getValue(HttpRequestVariableSubstitutor.getDefault(context.project))
                if (contentType == "application/graphql") {
                    return SimpleInjection(GraphQLLanguage.INSTANCE, "", "", null);
                }
            }
        }
        return null
    }
}