<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHook Search Results" scope="request"/>
<bs:page>

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
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: '${title}', selected: true}
      ];
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
    
		<div id="webHookSearchForm">
			<form action="search.html" method="get"><input type=text name="search" placeholder="Search Web Hooks"></form>
    	</div>
		<c:if test="${fn:length(searchResults) == 0}" >
				<p>There are no matching WebHooks found.</p>
		</c:if>
		<c:if test="${fn:length(searchResults) > 0}" >
				<p>There are <strong>${resultCount}</strong> matching WebHooks found in <strong>${fn:length(searchResults)}</strong> projects.</p> 
	    <br/>
	    <table id="webHookSearchTable" class="settings">
			<c:forEach items="${searchResults}" var="projectList">
	   		<thead>
			<tr class="projectTitle"><th colspan="6">${projectList.sensibleProjectFullName} <span class="webhookCreate">Create new WebHook for this project</span></th></tr>
		   		<tr style="background-color: rgb(245, 245, 245);">
					<th class="name">URL</th>
					<th class="name">Format</th>
					<th class="name">Build Events</th>
					<th class="value" style="width:20%;" colspan="3">Enabled Builds</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${projectList.webHookList}" var="hook">
					
					<tr id="viewRow_${hook.uniqueKey}" class="webHookRow">
						<td class="name highlight" onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"><c:out value="${hook.url}" /></td>
						
								<c:choose>
									<c:when test="${hook.payloadTemplate == 'none'}">
						<td class="value highlight webHookRowItemFormat" style="width:15%;"><c:out value="${hook.payloadFormatForWeb}" /></td>
									</c:when>
									<c:otherwise>
						<td class="value highlight webHookRowItemFormat" style="width:15%;"><a href="template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}" /></a></td>
									</c:otherwise>
								</c:choose>					
						
						
						<td class="value highlight" style="width:15%;" onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"><c:out value="${hook.enabledEventsListForWeb}" /></td>
						<td class="value highlight" style="width:15%;" onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#buildPane');"><c:out value="${hook.enabledBuildsListForWeb}" /></td>
						<td class="edit highlight"><a onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');" href="javascript://">edit</a></td>
						<td class="edit highlight"><a onclick="WebHooksPlugin.showDeleteDialog('${hook.uniqueKey}');" href="javascript://">delete</a></td>
					</tr>
				</c:forEach>
				<tr class="blankRow"><td colspan="4">&nbsp;</td></tr>
			</tbody>
			</c:forEach>
		</table>
		</c:if>

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

    	<%@ include file="webHookInclude.jsp" %>
    </jsp:attribute>
</bs:page>