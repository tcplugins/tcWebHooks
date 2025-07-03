<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>

					<c:if test="${fn:length(webhooks.value) > 0}" >
						<tr id="viewProjectNameRow_${webhooks.key.externalId}" class="webHookRow" style="background-color: rgb(245, 245, 245);">
							<td colspan="5" class="tagsLabel" style="padding-top: 1em; padding-bottom:1em;">${webhooks.key.name} (${webhooks.key.externalId})
							</td>
						</tr>
						<tr id="viewProjectVcsStatusesRow_${webhooks.key.externalId}" class="webHookRow">
							<td colspan="5">
								<c:if test="${showDetailLink}">
									<span class="migrationDetailLink"><a href="../webhooks/migration.html?project=${webhooks.key.externalId}">Migration details for <c:out value="${webhooks.key.name}"/></a></span>
								</c:if>
								
								<c:choose>
									<c:when test="${vcsStatuses.get(webhooks.key).isKotlin and vcsStatuses.get(webhooks.key).vcsAndSyncEnabled}">
										This project has KotlinDSL VCS Settings enabled. Migrations will not be attempted, and must be manually applied.<br>
										This project has sync with the UI enabled. If users have created webhooks, there may be a patch file committed to the
										repository. This patch file will need to be manually applied to <code>settings.kts.
									</c:when>
									<c:when test="${vcsStatuses.get(webhooks.key).isKotlin and not vcsStatuses.get(webhooks.key).vcsAndSyncEnabled}">
										This project has <em>KotlinDSL VCS Settings</em> <strong>enabled</strong>. Migrations will not be attempted, and must be manually applied.<br>
										This project has <em>Allow editing project settings via UI</em> <strong>disabled</strong>. Webhooks must be created by adding them to <code>settings.kts</code>.
									</c:when>
									<c:when test="${not vcsStatuses.get(webhooks.key).isKotlin and vcsStatuses.get(webhooks.key).vcsAndSyncEnabled}">
										tcWebHooks configurations will have been attempted but may still have to be remedied by hand.
										Configuration of each webhook can be generated. 
										You may need to copy and paste these configuration(s) into the <code>plugin-settings.xml</code> file committed in the repository. 
									</c:when>
									<c:when test="${not vcsStatuses.get(webhooks.key).isKotlin and vcsStatuses.get(webhooks.key).vcsEnabled and not vcsStatuses.get(webhooks.key).vcsAndSyncEnabled}">
										This project has <em>XML VCS Settings</em> <strong>enabled</strong> and <em>Allow editing project settings via UI</em> is <strong>disabled</strong>.<br>
										 
										Migrations will not be attempted, and must be manually applied.<br>
										Configuration of each webhook can be generated. 
										These configuration(s) will need to copy and pasted into the <code>plugin-settings.xml</code> file and committed in the repository. 
									</c:when>
									<c:when test="${not vcsStatuses.get(webhooks.key).vcsEnabled}">
										This project is not configured to store settings in VCS.<br>
										 
										Migrations will have already been attempted, and all webhooks migrated over to the new format.<br>
									</c:when>
									<c:otherwise>
										Please raise a bug on github if you see this message. <a href="https://github.com/tcplugins/tcWebHooks/issues">https://github.com/tcplugins/tcWebHooks/issues</a>.
										<c:if test="${not empty reasons.get(webhooks.key)}">
												<p><c:out value="${reasons.get(webhooks.key)}"/>
										</c:if>
									</c:otherwise>
								</c:choose>
								
								<ul>
									<li><em>KotlinDSL:</em> <strong></b><c:out value="${vcsStatuses.get(webhooks.key).isKotlin}"/></strong></li>
									<li><em>VCS Enabled:</em> <strong><c:out value="${vcsStatuses.get(webhooks.key).vcsEnabled}"/></strong></li>
									<li><em>VCS Sync Enabled:</em> <strong><c:out value="${vcsStatuses.get(webhooks.key).vcsAndSyncEnabled}"/></strong></li>
								</ul>
							</td>
						</tr>
						<tr style="background-color: rgb(245, 245, 245);">
							<th class="name">WebHook ID</th>
							<th class="">Plugin Settings</th>
							<th class="">Project Config (Project Feature)</th>
							<th class="">In Cache</th>
						</tr>
				    <c:forEach items="${webhooks.value}" var="hook">
							<tr id="viewRow_${hook.key}" class="webHookRow">
							<td class="name">${hook.key}</td>
							<td class="buildTemplateAction"><c:if test="${not empty hook.value.candidate}"><span style='font-size:2em;'>&#9873;</span></c:if></td>
							<td class="buildTemplateAction"><c:if test="${not empty hook.value.migrated}"><span style='font-size:2em;'>&#9733;</span></c:if></td>
							<td class="buildTemplateAction"><c:if test="${not empty hook.value.cached}"><span style='font-size:2em;'>&#9733;</span></c:if></td>
				    		</tr>
				    </c:forEach>
					<tr class="blankline">
						<td colspan="5" style="border-left:0px; border-right:0px;">&nbsp;</td>
					</tr>
					</c:if>
