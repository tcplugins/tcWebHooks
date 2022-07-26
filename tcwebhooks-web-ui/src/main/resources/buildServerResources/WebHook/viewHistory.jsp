<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" 
%><%@ include file="/include.jsp" 
%><jsp:useBean id="historyPager" type="jetbrains.buildServer.util.Pager" scope="request"
/><c:set var="title" value="WebHooks" scope="request"/><bs:page>

    <jsp:attribute name="head_include">
      <bs:linkCSS>
        /css/admin/adminMain.css
        /css/admin/projectConfig.css
        /css/forms.css
        /css/pager.css
        /css/admin/vcsRootsTable.css
        
    /css/visibleProjects.css
    /css/addSidebar.css
    /css/settingsTable.css
    /css/profilePage.css
    /css/userRoles.css
    
    ${jspHome}WebHook/css/styles.css
    ${jspHome}WebHook/3rd-party/highlight/styles/tomorrow.css
        
      </bs:linkCSS>
      <bs:linkScript>
        /js/bs/blocks.js
        /js/bs/blocksWithHeader.js
        /js/bs/forms.js
        /js/bs/modalDialog.js
        /js/bs/editBuildType.js
        /js/bs/editProject.js
        /js/bs/adminActions.js
      </bs:linkScript>

      <script type="text/javascript">
        BS.Navigation.items = [
		  {title: "Projects", url: '<c:url value="/overview.html"/>'},
		  <c:if test="${haveProject}"> 
		  	{title: "<c:out value="${projectName}"/>", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		  </c:if>
		  <c:if test="${haveBuild}"> 
		  	{title: "<c:out value="${buildName}"/>", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		  </c:if>
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <c:set var="pagerUrlPattern" value="history.html?view=${view}&page=[page]"/>
	        
	        <p>Since the last TeamCity restart, there have been:</p>
	        <div class="stats-table-wrapper">
	        <div class="webhookCounts">
		        <div class="webhookOkCount webhookCount"><a href="history.html?view=ok">
		        	<span class="count">${okCount}</span>
		        	<span class="description">Successful Events</span>
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookErrorCount webhookCount"><a href="history.html?view=errors">
		        	<span class="count">${errorCount}</span>
		        	<span class="description">Errored Events</span>	        
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookSkippedCount webhookCount"><a href="history.html?view=skipped">
		        	<span class="count">${skippedCount}</span>
		        	<span class="description">Skipped Events</span>	        
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookTotalCount webhookCount"><a href="history.html?view=all">
		        	<span class="count">${totalCount}</span>
		        	<span class="description">Total Events</span>
		        </a></div>
	        </div>
	        </div>
	        <hr>
	        <p>
	        <h2 class="noBorder"><span class="sentenceCase">${countContext}</span> - Page ${items.pageNumber} of ${items.totalPages} </h2>
	        <p>A log of the most recent 10,000 webhook executions is stored in memory in TeamCity. These are cleared on restart.</p>
	        The following table shows the ${items.itemsOnThisPage} most <span class="lowercase">${countContext}</span>, with older items on subsequent pages.
	        <bs:pager place="top" urlPattern="${pagerUrlPattern}" pager="${historyPager}"/>
	        <table class="settings">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>Build</th><th>URL (some URLs may be partially hidden)</th><th>Build Event</th><th colspan="2">Event Response</th></tr>

	        <c:forEach items="${items.items}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<c:if test="${not empty historyItem.buildId}">
					    <td><a href="../viewLog.html?buildId=${historyItem.buildId}"><c:out value="${historyItem.buildTypeName}"/> #${historyItem.buildId}</a></td>
					</c:if>
					<c:if test="${empty historyItem.buildId}">
					    <td><a href="../viewType.html?buildTypeId=<c:out value="${historyItem.buildTypeExternalId}"/>"><c:out value="${historyItem.buildTypeName}"/></a></td>
					</c:if>
					
					<c:if test="${afn:permissionGrantedForProjectWithId(historyItem.webHookConfig.projectInternalId, 'EDIT_PROJECT')}">
						<c:choose>
							<c:when test="${not historyItem.webHookExecutionStats.secureValueAccessed}">
								<td><span title="Webhook from project '<c:out value="${historyItem.webHookConfig.projectExternalId}"/>'"><c:out value="${historyItem.webHookExecutionStats.url}"/></span></td>
							</c:when>
							<c:when test="${historyItem.webHookExecutionStats.secureValueAccessed && not historyItem.webHookConfig.hideSecureValues}">
								<td><span title="Webhook from project '<c:out value="${historyItem.webHookConfig.projectExternalId}"/>'"><c:out value="${historyItem.webHookExecutionStats.url}"/></span></td>
							</c:when>
							<c:otherwise>
								<td><span title="Full URL hidden. A secure value was used to build the WebHook URL or payload">** <c:out value="${historyItem.url}"/></span></td>
							</c:otherwise>
						</c:choose>
					</c:if>
					
					<c:if test="${not afn:permissionGrantedForProjectWithId(historyItem.webHookConfig.projectInternalId, 'EDIT_PROJECT')}">
						<td><span title="You do not have permission to see the full URL for this webhook (no 'EDIT_PROJECT' permission on '<c:out value="${historyItem.webHookConfig.projectExternalId}"/>')">** <c:out value="${historyItem.url}"/></span></td>
					</c:if>
					
					<td><c:out value="${historyItem.webHookExecutionStats.buildState.shortDescription}${historyItem.test}">undefined</c:out></td>
					<td title="x-tcwebhooks-request-id: ${historyItem.webHookExecutionStats.trackingId}">${historyItem.webHookExecutionStats.statusCode} :: <c:out value="${historyItem.webHookExecutionStats.statusReason}"/></td>
	        	    <td><a href="./search.html?webhookId=${historyItem.webHookConfig.uniqueKey}"><i title="Search for WebHook Configuration" class="icon-gears"></i></a></td>
	   				</tr>
	        </c:forEach>
	        
		    </table>
		    <bs:pager place="bottom" urlPattern="${pagerUrlPattern}" pager="${historyPager}"/>
		    
			</div>	        
</bs:refreshable>	        
    </jsp:attribute>
</bs:page>
	        
