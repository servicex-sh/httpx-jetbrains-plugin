package org.jetbrains.plugins.httpx.injector

import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.lang.Language
import com.intellij.lang.injection.general.Injection
import com.intellij.lang.injection.general.LanguageInjectionContributor
import com.intellij.lang.injection.general.SimpleInjection
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType

/**
 * language injection for SSH method with shell script
 */
class ChatgptInjectionContributor : LanguageInjectionContributor {
    private val mdLanguage: Language = Language.findLanguageByID("Markdown")!!

    override fun getInjection(context: PsiElement): Injection? {
        if (context is HttpMessageBody) {
            val httpRequest = context.parentOfType<HttpRequest>(false)
            if (httpRequest != null && httpRequest.httpMethod == "CHATGPT") {
                return SimpleInjection(mdLanguage, "", "", null);
            }
        }
        return null
    }
}