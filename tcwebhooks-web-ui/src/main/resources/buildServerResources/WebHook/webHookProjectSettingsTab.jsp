<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
<c:set var="webhookConfigDialogScope" value="WebHooksPlugin.Configurations" scope="request"/>
<c:set var="parameterConfigDialogScope" value="WebHooksPlugin.Parameters" scope="request"/>
<c:set var="showLinksToOldEditPage" value="true" scope="request"/>
<c:set var="showLinksForInlineEditingIfRestApiMissing" value="false" scope="request"/>
<c:set var="parentTemplateCount" value="0" scope="request"/>
<c:set var="parentParameterCount" value="0" scope="request"/>


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
		
		<%@ include file="jsp-includes/projectParentItemsTable.jsp" %>

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
	<%@ include file="jsp-includes/deleteWebHookHeaderDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookFilterDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookFilterDialog.jsp" %>
	<%@ include file="jsp-includes/noRestApiDialog.jsp" %>
	
	<script type="text/javascript">
		var restApiDetected = ${isRestApiInstalled};
		var ProjectBuilds = ${projectWebHooksAsJson};
	</script>
