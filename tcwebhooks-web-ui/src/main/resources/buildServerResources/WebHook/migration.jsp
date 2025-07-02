<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHook Migrations" scope="request"/>
<bs:page>
	<jsp:attribute name="page_title">
	  ${title}
	</jsp:attribute>

    <jsp:attribute name="head_include">
      <bs:linkCSS>
        /css/admin/adminMain.css
        /css/admin/projectConfig.css
        /css/forms.css
        /css/admin/vcsRootsTable.css

        /css/visibleProjects.css
        /css/settingsTable.css
        /css/profilePage.css
        /css/userRoles.css

        ${jspHome}WebHook/css/styles.css
      </bs:linkCSS>

      <bs:linkScript>
        /js/bs/blocks.js
        /js/bs/blocksWithHeader.js
        /js/bs/forms.js
        /js/bs/modalDialog.js
        /js/bs/editBuildType.js
        /js/bs/editProject.js
        /js/bs/adminActions.js
        ${jspHome}WebHook/js/editWebhook.js
      </bs:linkScript>

    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "WebHooks", url: '<c:url value="/webhooks/index.html"/>'},
        {title: '${title}', selected: true}
      ];
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">

		<c:if test="${fn:length(migrationCandidates) == 0}" >
				<p>There are no WebHooks waiting to be migrated for this project.</p>
		</c:if>
		<c:if test="${fn:length(migrationCandidates) > 0}" >
				<p><strong>${fn:length(migrationCandidates)}</strong> WebHooks found to be migrated.</p>
		</c:if>
		
		<c:if test="${fn:length(migratedWebHooks) == 0}" >
				<p>There are no WebHooks already migrated for this project.</p>
		</c:if>
		<c:if test="${fn:length(migratedWebHooks) > 0}" >
				<p><strong>${fn:length(migratedWebHooks)}</strong> WebHooks have already been migrated.</p>
		</c:if>
		
		<c:if test="${fn:length(cachedWebHooks) == 0}" >
				<p>There are no WebHooks found in the cache for this project.</p>
		</c:if>
		<c:if test="${fn:length(cachedWebHooks) > 0}" >
				<p><strong>${fn:length(cachedWebHooks)}</strong> WebHooks have been found in the cache.</p>
		</c:if>

	    <br/>
	    <div style="width: 50%">
	    <table class="settings webhooktable">
	   		<thead>
		   		<tr style="background-color: rgb(245, 245, 245);">
					<th class="name">WebHook ID</th>
					<th class="">Plugin Settings</th>
					<th class="">Project Config (Project Feature)</th>
					<th class="">In Cache</th>
				</tr>
			</thead>
			<tbody>
	    <c:forEach items="${webhooks}" var="hook">
				<tr id="viewRow_${hook.key}" class="webHookRow">
				<td class="name">${hook.key}</td>
				<td class="actionCell"><c:if test="${not empty hook.value.candidate}" >&#9873;</c:if></td>
				<td class="actionCell"><c:if test="${not empty hook.value.migrated}" >&#9873;</c:if></td>
				<td class="actionCell"><c:if test="${not empty hook.value.cached}" >&#9873;</c:if></td>
	    		</tr>
	    </c:forEach>
	    	</tbody>
		</table>
	    
	    </div>
			<c:forEach items="${searchResults}" var="projectList">
			
				<c:forEach items="${projectList.webHookList}" var="hook">

					<tr id="viewRow_${hook.uniqueKey}" class="webHookRow">
					<td class="name <%--highlight--%>" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"--%>>
							<c:if test="${not afn:permissionGrantedForProjectWithId(projectList.projectId, 'EDIT_PROJECT')}">
								<span title="You do not have permission to see the full URL for this webhook (no project edit permission)">** <c:out value="${hook.generalisedUrl}" /></span>
							</c:if>
							<c:if test="${afn:permissionGrantedForProjectWithId(projectList.projectId, 'EDIT_PROJECT')}">
								<c:out value="${hook.url}" />
							</c:if>
					</td>
							<c:choose>
								<c:when test="${hook.payloadTemplate == 'none'}">
					<td class="value <%--highlight--%> webHookRowItemFormat" style="width:15%;"><c:out value="${hook.payloadFormatForWeb}" /></td>
								</c:when>
								<c:otherwise>
					<td class="value <%--highlight--%> webHookRowItemFormat" style="width:15%;"><a title='<c:out value="${hook.templateToolTip}"/>' href="template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}" /></a></td>
								</c:otherwise>
							</c:choose>


						<td class="value <%--highlight--%>" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"--%>><c:out value="${hook.enabledEventsListForWeb}" /></td>
						<td class="value <%--highlight--%>" title="${hook.buildTypeCountAsToolTip}" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#buildPane');"--%>><c:out value="${hook.buildTypeCountAsFriendlyString}" /></td>
						<%--
						<td class="edit highlight"><a onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');" href="javascript://">edit</a></td>
						<td class="edit highlight"><a onclick="WebHooksPlugin.showDeleteDialog('${hook.uniqueKey}');" href="javascript://">delete</a></td>
						 --%>
					<td>
							<ul class="commalist webhookTags">
								<c:forEach items="${hook.tags}" var="tag">
									<li title="<c:out value="${tag.type.description}" />"><a href="../webhooks/search.html?tag=<c:out value="${tag.name}" />"><c:out value="${tag.name}" /></a></li>	
								</c:forEach>
							</ul>	
					</td>
					<td><a href="./history.html?webhookId=${hook.uniqueKey}"><i title="Search History for Recent Events for this WebHook" class="icon-tasks"></i></a></td>
					</tr>
				</c:forEach>
				<tr class="blankRow"><td colspan="5">&nbsp;</td></tr>
			</tbody>
			</c:forEach>
		</table>

		<script type="application/javascript">
			function populateBuildHistory() {

				<c:if test="${not haveBuild && haveProject}">
				populateBuildHistoryAjax("project:${projectExternalId},");
				</c:if>
				<c:if test="${haveBuild}">
				populateBuildHistoryAjax("buildType:${buildExternalId},");
				</c:if>
			}
		</script>

    	<%--@ include file="webHookInclude.jsp" --%>
    </jsp:attribute>
</bs:page>