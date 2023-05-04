package org.jetbrains.plugins.httpx.codeInsight

import com.intellij.httpClient.http.request.codeInsight.HttpRequestIncorrectHttpHeaderInspection
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager

class HttpxHeadersInspectionService(project: Project) {
    init {
        val inspectionProfileManager = ProjectInspectionProfileManager.getInstance(project)
        val inspectionToolWrapper =
            inspectionProfileManager.currentProfile.getInspectionTool("IncorrectHttpHeaderInspection", project)
        val httpHeaderInspection = inspectionToolWrapper?.tool as HttpRequestIncorrectHttpHeaderInspection
        val customHeaders = httpHeaderInspection.getCustomHeaders()
        customHeaders.add("Subject")
        customHeaders.add("Reply-To")
        customHeaders.add("X-JSON-Schema")
        customHeaders.add("X-JSON-Type")
        customHeaders.add("X-JSON-Path")
        customHeaders.add("X-Java-Type")
        customHeaders.add("X-Region-Id")
        customHeaders.add("X-GraphQL-Variables")
        customHeaders.add("X-SSH-Private-Key")
        customHeaders.add("X-Temperature")
        customHeaders.add("X-Model")
        customHeaders.add("X-OPENAI-API-KEY")
        customHeaders.add("X-Args-0")
        customHeaders.add("X-Args-1")
        customHeaders.add("X-Args-2")
        customHeaders.add("X-Args-3")
        customHeaders.add("X-Args-4")
        customHeaders.add("X-Args-5")
    }
}