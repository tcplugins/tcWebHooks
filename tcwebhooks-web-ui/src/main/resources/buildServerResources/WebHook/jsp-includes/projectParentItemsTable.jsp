<c:if test="${fn:length(projectWebHooksAndTemplates) > 0}" >
    <h2 class="webhookHeading">WebHooks and Templates in parent Projects</h2>
    <p>WebHooks from parent projects may also be executed for builds in this project. Templates and Parameters from parent projects are available for webhooks to use.</p>
    Parent projects have the following webhooks, templates, and parameters:
    <table class="highlightable parametersTable webhooktable">
        <thead>
        <tr><th class="name" style="width:40%">Project Name</th><th style="width:20%">WebHook Count</th><th style="width:20%">Template Count</th><th style="width:20%">Parameter Count</th></tr>
        </thead>
        <tbody>
    <c:forEach items="${projectWebHooksAndTemplates}" var="parent">
        <c:set var="parentTemplateCount" value="${parentTemplateCount + fn:length(parent.templates.templateList)}"/>
        <c:set var="parentParameterCount" value="${parentParameterCount + fn:length(parent.parameters.parameterList)}"/>
        <c:if test="${not showLinksToOldEditPage}">
	        <tr><td><a href="../webhooks/index.html?projectId=${parent.webhooks.externalProjectId}"><c:out value="${parent.webhooks.sensibleProjectName}"/></a></td>
	            <td><a href="../webhooks/index.html?projectId=${parent.webhooks.externalProjectId}">${fn:length(parent.webhooks.webHookList)} webhooks configured</a></td>
	            <td><a href="../webhooks/templates.html?projectId=${parent.webhooks.externalProjectId}">${fn:length(parent.templates.templateList)} templates available</a></td>
	            <td><a href="../webhooks/index.html?projectId=${parent.parameters.project.externalId}#parameters">${fn:length(parent.parameters.parameterList)} parameters configured</a></td>
	        </tr>
        </c:if>
        
        <c:if test="${showLinksToOldEditPage}">
	        <tr><td><a href="../admin/editProject.html?projectId=${parent.webhooks.externalProjectId}&tab=tcWebHooks"><c:out value="${parent.webhooks.sensibleProjectName}"/></a></td>
	            <td><a href="../admin/editProject.html?projectId=${parent.webhooks.externalProjectId}&tab=tcWebHooks">${fn:length(parent.webhooks.webHookList)} webhooks configured</a></td>
	            <td><a href="../webhooks/templates.html?projectId=${parent.webhooks.externalProjectId}">${fn:length(parent.templates.templateList)} templates available</a></td>
	            <td><a href="../admin/editProject.html?projectId=${parent.webhooks.externalProjectId}&tab=tcWebHooks#parameters">${fn:length(parent.parameters.parameterList)} parameters configured</a></td>
	        </tr>
        </c:if>
    </c:forEach>
    </tbody>
    </table>
    <p><p>
</c:if>
