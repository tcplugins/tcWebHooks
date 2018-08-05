<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" 
%><%@ include file="/include.jsp" %>
	<div>
	
	${permissionError}
	
	<c:forEach items="${projectAndParents}" var="project">
		<div style="background-color: #f5f5f5; padding: 5px; margin-bottom:3em;">
		<c:choose>
			<c:when test="${project.externalProjectId == '_Root'}">
				<h3 class="title actionBar" style="background-color: #f5f5f5; border-bottom: solid 2px #ABB1C4;">WebHooks configured for every TeamCity build (_Root)</h3>
			</c:when>
			<c:otherwise>	
				<h3 class="title actionBar" style="background-color: #f5f5f5; border-bottom: solid 2px #ABB1C4;">WebHooks configured for ${project.project.fullName}</h3>
			</c:otherwise>
		</c:choose>
	
		
		<c:if test="${project.projectWebhookCount == 0}" >
				<div style='margin-left: 1em; margin-right:1em;'>
				<p>There are no WebHooks configured for this project.</p> 
				<a href="./webhooks/index.html?projectId=${project.externalProjectId}">Add project WebHooks</a>.
				</div>
		</c:if>
		<c:if test="${project.projectWebhookCount > 0}" >
				<div style='margin-left: 1em; margin-right:1em;'>
				<c:if test="${not project.webHookProjectSettings.enabled}" >
					<div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
				</c:if>
				<p>There are <strong>${project.projectWebhookCount}</strong> WebHooks configured for all builds in this project. 
					<a href="./webhooks/index.html?projectId=${project.externalProjectId}">Edit project WebHooks</a>.</p>
				<table class="testList dark borderBottom">
					<thead><tr><th class=name style="background-color: #f5f5f5; color:#333333;">URL</th><th class=name style="background-color: #f5f5f5; color:#333333;">Enabled</th></tr></thead>
					<tbody>
					<c:if test="${not project.admin}">
						<c:forEach items="${project.projectWebhooks}" var="hook">
							<tr><td><span title="You do not have permission to see the full URL for this webhook (no project edit permission)">** <c:out value="${hook.generalisedUrl}" /></span></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
						</c:forEach>
					</c:if>
					<c:if test="${project.admin}">
						<c:forEach items="${project.projectWebhooks}" var="hook">
							<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
						</c:forEach>
					</c:if>
					</tbody>
				</table>
				</div>
		</c:if>

			<c:forEach items="${project.buildWebhooks}" var="config">

				<div style='margin-top: 2.5em;'><h3 class="title" style="background-color: #f5f5f5; border-bottom: solid 2px #ABB1C4;">WebHooks configured for ${projectName} &gt; ${config.buildName}</h3>
				
				<c:if test="${config.hasNoBuildWebHooks}" >
						<div style='margin-left: 1em; margin-right:1em;'>
						<p>There are no WebHooks configured for this specific build.</p> 
						<a href="./webhooks/index.html?buildTypeId=${config.buildExternalId}">Add build WebHooks</a>.
						</div>
				</c:if>
				<c:if test="${config.hasBuildWebHooks}" >
						<div style='margin-left: 1em; margin-right:1em;'>
						<p>There are <strong>${config.buildCount}</strong> WebHooks for this specific build. 
							<a href="./webhooks/index.html?buildTypeId=${config.buildExternalId}">Edit build WebHooks</a>.</p>
						<table class="testList dark borderBottom">
							<thead><tr><th class=name>URL</th><th class=name>Enabled</th></tr></thead>
							<tbody>
							<c:forEach items="${config.buildWebHookList}" var="hook">
								<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
							</c:forEach>
							</tbody>
						</table>
						</div>
				</c:if>
				</div>
			</c:forEach>
		</div>	
		</c:forEach>
		
		<h2 class="noBorder">All Recent WebHook Events</h2>
	        The following table shows the ${items.itemsOnThisPage} most recent <span class="lowercase">${countContext} webhook events</span>.
	        <table class="settings" style="width:100%;">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>Build</th><th>URL (some URLs may be partially hidden)</th><th>Build Event</th><th>Event Response</th></tr>

	        <c:forEach items="${items.items}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<c:if test="${not empty historyItem.buildId}">
					    <td><a href="../viewLog.html?buildId=${historyItem.buildId}">${historyItem.buildTypeName} #${historyItem.buildId}</a></td>
					</c:if>
					<c:if test="${empty historyItem.buildId}">
					    <td><a href="../viewType.html?buildTypeId=${historyItem.buildTypeExternalId}">${historyItem.buildTypeName}</a></td>
					</c:if>
					
					<c:if test="${afn:permissionGrantedForProjectWithId(historyItem.webHookConfig.projectInternalId, 'EDIT_PROJECT')}">
						<td><span title="Webhook from project '${historyItem.webHookConfig.projectExternalId}'">${historyItem.webHookExecutionStats.url}</span></td>
					</c:if>
					
					<c:if test="${not afn:permissionGrantedForProjectWithId(historyItem.webHookConfig.projectInternalId, 'EDIT_PROJECT')}">
						<td><span title="You do not have permission to see the full URL for this webhook (no 'EDIT_PROJECT' permission on '${historyItem.webHookConfig.projectExternalId}')">** ${historyItem.url}</span></td>
					</c:if>
					
					<td><c:out value="${historyItem.webHookExecutionStats.buildState.shortDescription}${historyItem.test}">undefined</c:out></td>
					<td title="x-tcwebhooks-request-id: ${historyItem.webHookExecutionStats.trackingId}">${historyItem.webHookExecutionStats.statusCode} :: ${historyItem.webHookExecutionStats.statusReason}</td>
	   				</tr>
	        	
	        </c:forEach>
	        
		    </table>
		    
		</div>
		