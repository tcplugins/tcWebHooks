<%@ include file="/include.jsp" %>
	<div>
	
	<c:forEach items="${projectAndParents}" var="project">
		<div style="background-color: #f5f5f5; padding: 5px; margin:0.75em;  margin-top:3em;">
		<c:choose>
			<c:when test="${project.externalProjectId == '_Root'}">
				<h3 class="title actionBar" style="background-color: #f5f5f5; border-bottom: solid 2px #ABB1C4;" >WebHooks configured for every TeamCity build</h3>
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
					<c:forEach items="${project.projectWebhooks}" var="hook">
						<tr><td><c:out value="${hook.url}" /></td><td><c:out value="${hook.enabledListAsString}" /></td></tr>  
					</c:forEach>
					</tbody>
				</table>
				</div>
		</c:if>

			<c:forEach items="${project.buildWebhooks}" var="config">

				<div style='margin-top: 2.5em;'><h3 class="title">WebHooks configured for ${projectName} &gt; ${config.buildName}</h3>
				
				<c:if test="${config.hasNoBuildWebHooks}" >
						<div style='margin-left: 1em; margin-right:1em;'>
						<p>There are no WebHooks configured for this specific build.</p> 
						<a href="./webhooks/index.html?buildTypeId=${config.buildExternalId}">Add build WebHooks</a>.
						</div>
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
					</div>
				</c:if>
				
			</c:forEach>
		</div>	
		</c:forEach>
		</div>