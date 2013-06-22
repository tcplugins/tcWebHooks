<%@ include file="/include.jsp" %>
	<div><h3 class="title">WebHooks configured for ${projectName}</h3>
	
<c:if test="${noWebHooks}" >
		<p>There are no WebHooks configured for this project.</p> 
		<a href="./webhooks/index.html?projectId=${projectExternalId}">Add project WebHooks</a>.
	</div>
</c:if>
<c:if test="${webHooks}" >
	<div style='margin-left: 0.5em;'>
	<c:if test="${webHooksDisabled}" >
		<div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
	</c:if>
	<p>There are <strong>${webHookCount}</strong> WebHooks configured for this project. <br/>
		<a href="./webhooks/index.html?projectId=${projectExternalId}">Edit project WebHooks</a>.</p>
	</div>
	<table class="testList dark borderBottom">
		<thead><tr><th class=name>URL</th><th class=name>Enabled</th></tr></thead>
		<tbody>
		<c:forEach items="${webHookList}" var="hook">
			<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
		</c:forEach>
		</tbody>
	</table>
	</div>
</c:if>