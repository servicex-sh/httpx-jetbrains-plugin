package org.jetbrains.plugins.httpx.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.httpClient.http.request.psi.HttpHeaderFieldName
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext

class HttpxHeadersCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, psiElement().withParent(HttpHeaderFieldName::class.java), HttpxHeaderFieldNamesProvider())
    }

    private class HttpxHeaderFieldNamesProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            result.addElement(LookupElementBuilder.create("X-JSON-Path"))
            result.addElement(LookupElementBuilder.create("Subject"))
            result.addElement(LookupElementBuilder.create("Reply-To"))
            result.addElement(LookupElementBuilder.create("X-JSON-Schema"))
            result.addElement(LookupElementBuilder.create("X-JSON-Type"))
            result.addElement(LookupElementBuilder.create("X-GraphQL-Variables"))
            result.addElement(LookupElementBuilder.create("X-SSH-Private-Key"))
            result.addElement(LookupElementBuilder.create("X-Region-Id"))
            result.addElement(LookupElementBuilder.create("X-Model"))
            result.addElement(LookupElementBuilder.create("X-Temperature"))
            result.addElement(LookupElementBuilder.create("X-OPENAI-API-KEY"))
            result.addElement(LookupElementBuilder.create("X-Args-0"))
            result.addElement(LookupElementBuilder.create("X-Args-1"))
            result.addElement(LookupElementBuilder.create("X-Args-2"))
            result.addElement(LookupElementBuilder.create("X-Args-3"))
            result.addElement(LookupElementBuilder.create("X-Args-4"))
            result.addElement(LookupElementBuilder.create("X-Args-5"))
        }
    }
}