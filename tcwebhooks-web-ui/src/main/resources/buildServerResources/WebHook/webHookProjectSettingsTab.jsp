<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
<c:set var="webhookConfigDialogScope" value="WebHooksPlugin.Configurations" scope="request"/>
<c:set var="parameterConfigDialogScope" value="WebHooksPlugin.Parameters" scope="request"/>

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
	<div>
		<h2>WebHooks and Templates</h2>
		<c:if test="${not isRestApiInstalled}">
			<div class="icon_before icon16 attentionRed">The <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> is not installed. Experimental support for modifying webhooks and parameters will be disabled.</div>
		</c:if>
		<c:if test="${isRestApiInstalled}">
			<div class="icon_before icon16 attentionRed">The WebHooks REST API plugin is installed. Experimental support for modifying webhooks and parameters on this page is enabled.</div>
		</c:if>
		
		${permissionError}
		
		<c:if test="${fn:length(projectWebHooksAndTemplates) > 0}" >
			<h3>WebHooks and Templates in parent Projects</h3>
			WebHooks from parent projects may also be executed for builds in this project. Templates from parent projects are available for webhooks to use.
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
				<h3>WebHooks configured for every TeamCity build (_Root project)</h3>
			</c:when>
			<c:otherwise>
				<h3>WebHooks configured for <c:out value="${project.fullName}"/></h3>
			</c:otherwise>
		</c:choose>
		
		<%@ include file="jsp-includes/projectWebHooksTable.jsp" %>
		<p>
		<%@ include file="jsp-includes/projectParametersTable.jsp" %>
		<p>
		
		<c:choose>
			<c:when test="${projectExternalId == '_Root'}">
				<h3>WebHook Templates available for every TeamCity build (_Root project)</h3>
			</c:when>
			<c:otherwise>
				<h3>WebHook Templates available for <c:out value="${project.fullName}"/> and sub-projects</h3>
			</c:otherwise>
		</c:choose>

		<c:if test="${fn:length(webHookTemplates) == 0}" >
				<p>There are no Templates associated with this project.</p>
				To associate a template with this project, 
				<a href="../webhooks/templates.html">View WebHook Templates</a> and either select this project
				when creating a new	template, or edit an existing template and change the associated project.
		</c:if>
		<c:if test="${fn:length(webHookTemplates) > 0}" >
				<p>There are <strong>${fn:length(webHookTemplates)}</strong> Templates associated with this project.
					<a href="../webhooks/templates.html?projectId=${projectExternalId}">View project Templates</a>.</p>
			    <table id="webHookTemplateTable" class="settings">
					<thead>
					<tr style="background-color: rgb(245, 245, 245);">
					<th class="">Description</th>
					<th class="">Payload Format</th>
					<th class="">Supported Build Events</th>
					<th class="">Type</th>
					<th class="value" colspan="3" style="width:20%;">Usage</th>
					</tr>
					</thead>
					<tbody>
					    <c:forEach items="${webHookTemplates}" var="template">
						  <tr id="viewRow_${template.templateId}" class="webHookTemplate">
							<td class="nowrap heading" title="<c:out value="${template.templateToolTip}"/> (id: <c:out value="${template.templateId}"/>)"><c:out value="${template.templateDescription}" /></td>
							<td class="nowrap">${template.formatDescription}</td>
							<td>
								<ul class="commalist">
								<c:forEach items="${template.supportedBuildEnumStates}" var="state">	
									<li>${state.shortDescription}</li>
								</c:forEach>
								</ul>
							</td>
							<td class="nowrap">${template.templateState.description}</td>
							
							<td><a href="../webhooks/search.html?templateId=${template.templateId}">${template.webhookUsageCount}&nbsp;webhook(s)</a></td>
			
					<c:choose>  
			    		<c:when test="${template.templateDescription == 'Legacy Webhook'}"> 		
							<td>No template available</td>
						</c:when>
						<c:otherwise>  		
							<td><a href="../webhooks/template.html?template=${template.templateId}">View</a></td>
						</c:otherwise>  
					</c:choose>
					
						  </tr>	
					    </c:forEach>
					</tbody>
				</table>
		</c:if>		
		
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
