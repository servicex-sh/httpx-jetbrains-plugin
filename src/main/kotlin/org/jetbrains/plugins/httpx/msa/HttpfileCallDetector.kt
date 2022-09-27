package org.jetbrains.plugins.httpx.msa

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.project.Project
import org.jetbrains.uast.UCallExpression
import org.strangeway.msa.db.InteractionType
import org.strangeway.msa.frameworks.CallDetector
import org.strangeway.msa.frameworks.FrameworkInteraction
import org.strangeway.msa.frameworks.Interaction
import org.strangeway.msa.frameworks.hasLibraryClass

class HttpfileCallDetector : CallDetector {
    private val interaction: Interaction = FrameworkInteraction(InteractionType.REQUEST, "HTTP")
    private val rsocketExchangeAnnotations = listOf("sh.servicex.httpfile.HttpRequestName")
    override fun getCallInteraction(project: Project, uCall: UCallExpression): Interaction? {
        val psiMethod = uCall.resolve()
        if (psiMethod != null) {
            if (AnnotationUtil.isAnnotated(psiMethod, rsocketExchangeAnnotations, 0)) {
                return interaction
            }
        }
        return null
    }

    override fun isAvailable(project: Project): Boolean {
        return hasLibraryClass(project, "sh.servicex.httpfile.HttpRequestName")
    }
}