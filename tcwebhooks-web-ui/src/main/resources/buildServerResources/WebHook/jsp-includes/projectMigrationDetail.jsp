<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>

						<tr id="viewProjectNameRow_${migrationStatus.project.externalId}" class="webHookRow migration${migrationStatus.nextMigrationStep}" style="background-color: rgb(245, 245, 245);">
							<td colspan="5" class="tagsLabel migration${migrationStatus.nextMigrationStep}" style="padding-top: 1em; padding-bottom:1em;"><c:out value="${migrationStatus.project.name}"/> (${migrationStatus.project.externalId})
								<c:if test="${showDetailLink}">
									<span class="migrationDetailLink"><a href="../webhooks/migration.html?project=${migrationStatus.project.externalId}">Migration details for <c:out value="${migrationStatus.project.name}"/></a></span>
								</c:if>
								<c:if test="${showReRunMigration}">
									<authz:authorize projectId="${migrationStatus.project.externalId}" allPermissions="EDIT_PROJECT">
										<c:choose>
										<c:when test="${migrationStatus.nextMigrationStep eq 'COMPLETED'}"> </c:when>
										<c:when test="${migrationStatus.nextMigrationStep eq 'MANUAL_MIGRATION_REQUIRED'}"> </c:when>
										<c:otherwise>
											<form 
												action="../webhooks/migration.html?project=${migrationStatus.project.externalId}"
												method="post"
											>
											<input type="hidden" name="action" value="runMigration"/>
											<input type="hidden" name="tc-csrf-token" value="${sessionScope['tc-csrf-token']}"/>
											<button style="float:right;" class="btn" id="reRunMigration">Re-run Migration for this project</button>
											</form>
										</c:otherwise>
										</c:choose>
									</authz:authorize>
								</c:if>
							</td>
						</tr>
						<tr id="viewProjectprojectVcsStatusRow_${migrationStatus.project.externalId}" class="webHookRow">
							<td colspan="5" class="migration${migrationStatus.nextMigrationStep}">
								<div style="float:right; padding=2em; margin-left:5em; border-left: 1px #ccc solid;">
									<ul>
										<li><em>KotlinDSL:</em> <strong></b><c:out value="${migrationStatus.projectVcsStatus.kotlin}"/></strong></li>
										<li><em>VCS Enabled:</em> <strong><c:out value="${migrationStatus.projectVcsStatus.vcsEnabled}"/></strong></li>
										<li><em>Settings Editable in UI:</em> <strong><c:out value="${migrationStatus.projectVcsStatus.vcsSyncEnabled}"/></strong></li>
										<li><em>Migration Status:</em> <strong><c:out value="${migrationStatus.projectVcsStatus.migrationStatus}"/></strong>
										<li><em>Next Migration Step:</em> <strong><c:out value="${migrationStatus.nextMigrationStep}"/></strong>
									</ul>
								</div>
									<p>${migrationStatus.reason}</p>
									<p>
								<c:choose>
									<c:when test="${migrationStatus.projectVcsStatus.kotlin and migrationStatus.projectVcsStatus.vcsSyncEnabled}">
										This project has <em>KotlinDSL VCS Settings</em> <strong>enabled</strong>. Migrations will not be attempted, and must be manually applied.<br>
										This project has <em>Allow editing project settings via UI</em> <strong>enabled</strong>. If users have created webhooks, there may be a patch 
										file committed to the repository. 
										This patch file will need to be <a href="https://github.com/tcplugins/tcWebHooks/wiki/Migrating-webhooks-from-Kotlin-Patch-Files" target="_new">manually applied to <code>settings.kts</code></a>.
									</c:when>
									<c:when test="${migrationStatus.projectVcsStatus.kotlin and not migrationStatus.projectVcsStatus.vcsSyncEnabled}">
										This project has <em>KotlinDSL VCS Settings</em> <strong>enabled</strong>. Migrations will not be attempted, and must be manually applied.<br>
										This project has <em>Allow editing project settings via UI</em> <strong>disabled</strong>. Webhooks must be created by adding them to <code>settings.kts</code>.
									</c:when>
									<c:when test="${not migrationStatus.projectVcsStatus.kotlin and migrationStatus.projectVcsStatus.vcsEnabled and migrationStatus.projectVcsStatus.vcsSyncEnabled}">
										tcWebHooks configurations will have been attempted but may still have to be remedied by hand.
										Configuration of each webhook can be generated. 
										You may need to copy and paste these configuration(s) into the <code>project-config.xml</code> file committed in the repository. 
									</c:when>
									<c:when test="${not migrationStatus.projectVcsStatus.kotlin and migrationStatus.projectVcsStatus.vcsEnabled and not migrationStatus.projectVcsStatus.vcsSyncEnabled}">
										This project has <em>XML VCS Settings</em> <strong>enabled</strong> and <em>Allow editing project settings via UI</em> is <strong>disabled</strong>.<br>
										 
										Migrations will not be attempted, and must be manually applied.<br>
										Configuration of each webhook can be generated. 
										These configuration(s) will need to copy and pasted into the <code>plugin-settings.xml</code> file and committed in the repository. 
									</c:when>
									<c:when test="${not migrationStatus.projectVcsStatus.vcsEnabled}">
										This project is not configured to store settings in VCS. Migrations will have already been attempted.<br>
										<br>
									</c:when>
									<c:otherwise>
										Please raise a bug on github if you see this message. <a href="https://github.com/tcplugins/tcWebHooks/issues">https://github.com/tcplugins/tcWebHooks/issues</a>.
										<c:if test="${not empty reasons.get(webhooks.key)}">
												<p><c:out value="${reasons.get(webhooks.key)}"/>
										</c:if>
									</c:otherwise>
								</c:choose>
									</p>
							</td>
						</tr>
						<tr class="webHookRow" style="background-color: rgb(245, 245, 245);">
							<th class="name migration${migrationStatus.nextMigrationStep}">WebHook ID</th>
							<th class="migration${migrationStatus.nextMigrationStep}">Plugin Settings</th>
							<th class="migration${migrationStatus.nextMigrationStep}">Project Config (Project Feature)</th>
							<th class="migration${migrationStatus.nextMigrationStep}">In Cache</th>
						</tr>
					<c:forEach items="${migrationStatus.webhooks}" var="hook">
							<tr id="viewRow_${hook.key}" class="webHookRow">
							<td class="name migration${migrationStatus.nextMigrationStep}">${hook.key}</td>
							<td class="buildTemplateAction migration${migrationStatus.nextMigrationStep}"><c:if test="${not empty hook.value.candidate}"><span style='font-size:2em;'>&#9873;</span></c:if></td>
							<td class="buildTemplateAction migration${migrationStatus.nextMigrationStep}"><c:if test="${not empty hook.value.migrated}"><span style='font-size:2em;'>&#9733;</span></c:if></td>
							<td class="buildTemplateAction migration${migrationStatus.nextMigrationStep}"><c:if test="${not empty hook.value.cached}"><span style='font-size:2em;'>&#9733;</span></c:if></td>
							</tr>
					</c:forEach>
					<c:if test="${fn:length(migrationStatus.webhooks) == 0}">
						<tr class="webHookRow">
						<td colspan="5" class="migration${migrationStatus.nextMigrationStep}">There are 0 webhooks configured for this project</td>
						</tr>
					</c:if>
					<tr class="blankline">
						<td colspan="5" class="migration${migrationStatus.nextMigrationStep}" style="border-left:0px; border-right:0px;">&nbsp;</td>
					</tr>
