<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
	<div>
		<h2>WebHooks</h2>
		${permissionError}
		<c:if test="${fn:length(parentProjectBeans) > 0}" >
			<h3>WebHooks in parent Projects</h3>
			WebHooks from parent projects may also be executed for builds in this project. Parent projects have the following webhooks: 
			<table class="highlightable parametersTable">
				<tr><th class="name" style="width:75%">Project Name</th><th>WebHook Count</th></tr>
			<c:forEach items="${parentProjectBeans}" var="parent">
				<tr><td><a href="../webhooks/index.html?projectId=${parent.externalProjectId}">${parent.sensibleProjectName}</a></td><td>${fn:length(parent.webHookList)} webhooks configured</td></tr>
			</c:forEach>
			</table>
			<p><p>
		</c:if>
	
		<c:choose>
			<c:when test="${projectExternalId == '_Root'}">
				<h3>WebHooks configured for every TeamCity build (_Root project)</h3>
			</c:when>
			<c:otherwise>	
				<h3>WebHooks configured for ${project.fullName}</h3>
			</c:otherwise>
		</c:choose>
	
		<c:if test="${fn:length(projectBean.webHookList) == 0}" >
				<p>There are no WebHooks configured for this project.</p> 
				<a href="../webhooks/index.html?projectId=${projectExternalId}">Add project WebHooks</a>.
		</c:if>
		<c:if test="${fn:length(projectBean.webHookList) > 0}" >
				<c:if test="${not projectBean.webHookProjectSettings.enabled}" >
					<div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
				</c:if>
				<p>There are <strong>${fn:length(projectBean.webHookList)}</strong> WebHooks configured for all builds in this project. 
					<a href="../webhooks/index.html?projectId=${projectExternalId}">Edit project WebHooks</a>.</p>
				<table class="highlightable parametersTable">
					<thead>
						<tr>
							<th class=name style="width:40%">URL</th>
							<th style="width:20%">Format</th>
							<th style="width:20%">Build Events</th>
							<th style="width:20%">Enabled Builds</th>
						</tr>
					</thead>
					<tbody>
					<c:forEach items="${projectBean.webHookList}" var="hook">
						<tr>
							<td><c:out value="${hook.url}" /></td>
							<c:choose>
								<c:when test="${hook.payloadTemplate == 'none'}">
									<td><c:out value="${hook.payloadFormatForWeb}"/></td>
								</c:when>
								<c:otherwise>
									<td><a href="../webhooks/template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}"/></a></td>
								</c:otherwise>
							</c:choose>
							<td><c:out value="${hook.enabledEventsListForWeb}"/></td>
							<td><c:out value="${hook.enabledBuildsListForWeb}"/></td>
						</tr>  
					</c:forEach>
					</tbody>
				</table>
		</c:if>
	</div>	
	