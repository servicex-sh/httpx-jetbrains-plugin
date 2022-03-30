package org.jetbrains.plugins.httpx.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.httpClient.http.request.psi.*
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.ReflectionUtil
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Aliyun
import software.amazon.awssdk.regions.GeneratedServiceMetadataProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.ServiceEndpointKey
import software.amazon.awssdk.regions.ServiceMetadata

class AwsRequestCompletionContributor : CompletionContributor() {
    companion object {
        val globalParamNames = listOf("Action", "Version", "RegionId")
        val completionCapture: PsiElementPattern.Capture<LeafPsiElement> = PlatformPatterns.psiElement(LeafPsiElement::class.java)
            .withParent(object : ElementPattern<PsiElement?> {
                override fun accepts(o: Any?): Boolean {
                    return o is HttpPathAbsolute
                            || o is HttpHost
                            || o is HttpRequestTarget
                            || o is HttpQueryParameterValue
                            || o is HttpQueryParameterKey
                            || o is HttpHeaderFieldValue
                }

                override fun accepts(o: Any?, context: ProcessingContext): Boolean {
                    return accepts(o)
                }

                override fun getCondition(): ElementPatternCondition<PsiElement?>? {
                    return null
                }
            })
        val allServiceMetadata =
            ReflectionUtil.getStaticFieldValue(GeneratedServiceMetadataProvider::class.java, Map::class.java, "SERVICE_METADATA") as Map<String, ServiceMetadata>
    }

    init {
        extend(CompletionType.BASIC, completionCapture, AwsRequestCompletionProvider())
    }

    private class AwsRequestCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val element = parameters.position
            val parent = element.parent
            val prefix = trimDummy(parent.text)
            val httpRequest = element.parentOfType<HttpRequest>()
            if (httpRequest != null && httpRequest.httpMethod.startsWith("AWS")) {
                if (parent is HttpHost) { //host name
                    if (prefix.contains('.')) { // completion for endpoints
                        val productCode = prefix.substring(0, prefix.indexOf('.'))
                        val product = allServiceMetadata[productCode]
                        if (product != null) {
                            product.regions().forEach { region ->
                                val hostName = product.endpointFor(ServiceEndpointKey.builder().region(region).build()).toString()
                                result.addElement(LookupElementBuilder.create(hostName))
                            }
                        }
                    } else { // product code names
                        allServiceMetadata.keys.forEach {
                            result.addElement(LookupElementBuilder.create(it))
                        }
                    }
                } else if (parent is HttpQueryParameterKey) { //param names
                    val httpQuery = parent.getParentOfType<HttpQuery>(true)
                    if (httpQuery != null) {
                        val paramNames = httpQuery.queryParameterList.map { it.queryParameterKey.text }
                        globalParamNames.filter { paramNames.isEmpty() || !paramNames.contains(it) }
                            .forEach {
                                result.addElement(LookupElementBuilder.create("${it}=").withPresentableText(it))
                            }
                    }
                } else if (parent is HttpQueryParameterValue) { //param value
                    //action or version
                    val httpQueryParameter = parent.parent
                    val paramName = httpQueryParameter.firstChild.text
                    if (globalParamNames.contains(paramName)) {
                        val httpRequestTarget = httpQueryParameter.getParentOfType<HttpRequestTarget>(true)
                        val host = httpRequestTarget?.host?.text
                        if (host != null && host.contains('.')) {
                            val productCode = host.substring(0, host.indexOf('.'))
                            val product = allServiceMetadata[productCode]
                            if (product != null) {
                                // https://github.com/aws/aws-sdk-java-v2/blob/master/services/account/src/main/resources/codegen-resources/service-2.json
                                /*when (paramName) {
                                    "Action" -> {
                                        for (action in product.apis) {
                                            result.addElement(LookupElementBuilder.create(action))
                                        }
                                    }
                                    "Version" -> {
                                        result.addElement(LookupElementBuilder.create(product.version))
                                    }
                                    "RegionId" -> {
                                        for (region in Aliyun.GLOBAL_REGIONS) {
                                            result.addElement(LookupElementBuilder.create(region))
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                } else if (parent is HttpHeaderFieldValue) {
                    val httpHeaderField = parent.getParentOfType<HttpHeaderField>(true)!!
                    if (httpHeaderField.headerFieldName.text == "X-Region-Id") {
                        for (region in Aliyun.GLOBAL_REGIONS) {
                            Region.regions().forEach {
                                result.addElement(LookupElementBuilder.create(it))
                            }
                        }
                    }
                }
            }
        }

        private fun trimDummy(value: String): String {
            return StringUtil.trim(value.replace(CompletionUtil.DUMMY_IDENTIFIER, "").replace(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED, ""))
        }

    }
}