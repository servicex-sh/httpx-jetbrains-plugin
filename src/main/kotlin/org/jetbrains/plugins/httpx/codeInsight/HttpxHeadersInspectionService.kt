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
        customHeaders.plus("Subject")
        customHeaders.plus("Reply-To")
        customHeaders.plus("X-JSON-Schema")
        customHeaders.plus("X-JSON-Type")
        customHeaders.plus("X-JSON-Path")
        customHeaders.plus("X-Java-Type")
        customHeaders.plus("X-Region-Id")
        customHeaders.plus("X-GraphQL-Variables")
        customHeaders.plus("X-SSH-Private-Key")
        customHeaders.plus("X-Temperature")
        customHeaders.plus("X-Model")
        customHeaders.plus("X-OPENAI-API-KEY")
        customHeaders.plus("X-Args-0")
        customHeaders.plus("X-Args-1")
        customHeaders.plus("X-Args-2")
        customHeaders.plus("X-Args-3")
        customHeaders.plus("X-Args-4")
        customHeaders.plus("X-Args-5")
    }
}