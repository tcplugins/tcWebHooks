<%@ include file="/include.jsp" %>
	<div><h3 class="title">WebHooks configured for ${projectName}</h3>
	
<c:if test="${noProjectWebHooks}" >
		<div style='margin-left: 1em; margin-right:1em;'>
		<p>There are no WebHooks configured for this project.</p> 
		<a href="./webhooks/index.html?projectId=${projectExternalId}">Add project WebHooks</a>.
		</div>
	</div>
</c:if>
<c:if test="${projectWebHooks}" >
		<div style='margin-left: 1em; margin-right:1em;'>
		<c:if test="${projectWebHooksDisabled}" >
			<div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
		</c:if>
		<p>There are <strong>${projectWebHookCount}</strong> WebHooks configured for all builds in this project. 
			<a href="./webhooks/index.html?projectId=${projectExternalId}">Edit project WebHooks</a>.</p>
		<table class="testList dark borderBottom">
			<thead><tr><th class=name>URL</th><th class=name>Enabled</th></tr></thead>
			<tbody>
			<c:forEach items="${projectWebHookList}" var="hook">
				<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
			</c:forEach>
			</tbody>
		</table>
		</div>
	</div>
</c:if>

<div style='margin-top: 2.5em;'><h3 class="title">WebHooks configured for ${projectName} &gt; ${buildName}</h3>

<c:if test="${noBuildWebHooks}" >
		<div style='margin-left: 1em; margin-right:1em;'>
		<p>There are no WebHooks configured for this specific build.</p> 
		<a href="./webhooks/index.html?buildTypeId=${buildExternalId}">Add build WebHooks</a>.
		</div>
	</div>
</c:if>
<c:if test="${buildWebHooks}" >
		<div style='margin-left: 1em; margin-right:1em;'>
		<p>There are <strong>${buildWebHookCount}</strong> WebHooks for this specific build. 
			<a href="./webhooks/index.html?buildTypeId=${buildExternalId}">Edit build WebHooks</a>.</p>
		<table class="testList dark borderBottom">
			<thead><tr><th class=name>URL</th><th class=name>Enabled</th></tr></thead>
			<tbody>
			<c:forEach items="${buildWebHookList}" var="hook">
				<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
			</c:forEach>
			</tbody>
		</table>
		</div>
	</div>
</c:if>