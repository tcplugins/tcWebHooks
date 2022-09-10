<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
<c:set var="webhookConfigDialogScope" value="WebHooksPlugin.Configurations" scope="request"/>
<c:set var="parameterConfigDialogScope" value="WebHooksPlugin.Parameters" scope="request"/>
<c:set var="showLinksToOldEditPage" value="true" scope="request"/>
<c:set var="showLinksForInlineEditingIfRestApiMissing" value="false" scope="request"/>


<style>
	ul.commalist {
		display: inline;
		list-style: none;
		padding:0;

	}
	
	ul.commalist li {
		display: inline;
  white-space:nowrap;
}

ul.commalist li:after {
  content: ", ";
}

ul.commalist li:last-child:after {
	content: "";
}
</style>
<div class="webhookContainer">
	<h2 class="webhookHeading" style="padding-top: 0">WebHooks and Templates</h2>
	
	<c:if test="${not isRestApiInstalled}">
		<c:set var="showEditButton" value="${showLinksForInlineEditingIfRestApiMissing}" scope="request"/>
		<div class="icon_before icon16 attentionRed">The <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> is not installed. Experimental support for modifying webhooks and parameters will be disabled.</div>
	</c:if>
	<c:if test="${isRestApiInstalled}">
		<c:set var="showEditButton" value="${isRestApiInstalled}" scope="request"/>
		<div class="icon_before icon16 attentionRed">The WebHooks REST API plugin is installed. Experimental support for modifying webhooks and parameters on this page is enabled.</div>
	</c:if>
	
	${permissionError}
		
		<c:if test="${fn:length(projectWebHooksAndTemplates) > 0}" >
			<h2 class="webhookHeading">WebHooks and Templates in parent Projects</h2>
			<p>WebHooks from parent projects may also be executed for builds in this project. Templates from parent projects are available for webhooks to use.</p>
			Parent projects have the following webhooks and templates:
			<table class="highlightable parametersTable webhooktable">
				<thead>
				<tr><th class="name" style="width:40%">Project Name</th><th style="width:20%">WebHook Count</th><th style="width:20%">Template Count</th><th style="width:20%">Parameter Count</th></tr>
				</thead>
				<tbody>
			<c:forEach items="${projectWebHooksAndTemplates}" var="parent">
				<tr><td><a href="editProject.html?projectId=${parent.webhooks.externalProjectId}&tab=tcWebHooks"><c:out value="${parent.webhooks.sensibleProjectName}"/></a></td>
					<td><a href="../webhooks/index.html?projectId=${parent.webhooks.externalProjectId}">${fn:length(parent.webhooks.webHookList)} webhooks configured</a></td>
					<td>${fn:length(parent.templates.templateList)} templates available</td>
					<td><a href="../webhooks/index.html?projectId=${parent.parameters.project.externalId}#parameters">${fn:length(parent.parameters.parameterList)} parameters configured</a></td>
				</tr>
			</c:forEach>
			</tbody>
			</table>
			<p><p>
		</c:if>

		<c:choose>
			<c:when test="${projectExternalId == '_Root'}">
				<h2 class="webhookHeading">WebHooks configured for every TeamCity build (_Root project)</h2>
			</c:when>
			<c:otherwise>
				<h2 class="webhookHeading">WebHooks configured for the <i><c:out value="${project.fullName}"/></i> project</h2>
			</c:otherwise>
		</c:choose>
		
		<%@ include file="jsp-includes/projectWebHooksTable.jsp" %>
		<p>
		<%@ include file="jsp-includes/projectParametersTable.jsp" %>
		<p>
		<%@ include file="jsp-includes/projectTemplatesTable.jsp" %>
		<p>
		
		
	</div>
	<%@ include file="jsp-includes/editWebHookDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookParameterDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookParameterDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookHeaderDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookFilterDialog.jsp" %>
	<%@ include file="jsp-includes/noRestApiDialog.jsp" %>
	
	<script type="text/javascript">
		var restApiDetected = ${isRestApiInstalled};
		var ProjectBuilds = ${projectWebHooksAsJson};
	</script>
