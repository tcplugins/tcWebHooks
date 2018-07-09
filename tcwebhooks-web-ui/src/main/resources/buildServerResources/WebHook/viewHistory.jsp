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
    ${jspHome}WebHook/highlight/styles/tomorrow.css
        
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
		  	{title: "${projectName}", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		  </c:if>
		  <c:if test="${haveBuild}"> 
		  	{title: "${buildName}", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		  </c:if>
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <c:set var="pagerUrlPattern" value="history.html?view=${countContext}&page=[page]"/>
	        
	        <p>The most recent 10,000 webhook executions are stored in memory in TeamCity. These are cleared on restart.</p><p>Since the last TeamCity restart, there have been:</p>
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
	        <h2 class="noBorder"><span class="sentenceCase">${countContext}</span> Recent WebHook Events - Page ${items.pageNumber} of ${items.totalPages} </h2>
	        The following table shows the ${items.itemsOnThisPage} most recent <span class="lowercase">${countContext} webhook events</span>.
	        <bs:pager place="top" urlPattern="${pagerUrlPattern}" pager="${historyPager}"/>
	        <table class="settings">
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
					
					<c:if test="${afn:permissionGrantedForProjectWithId(historyItem.projectId, 'EDIT_PROJECT')}">
						<td>${historyItem.webHookExecutionStats.url}</td>
					</c:if>
					
					<c:if test="${not afn:permissionGrantedForProjectWithId(historyItem.projectId, 'EDIT_PROJECT')}">
						<td><span title="You do not have permission to see the full URL for this webhook (no project edit permission)">** ${historyItem.url}</span></td>
					</c:if>
					
					<td><c:out value="${historyItem.webHookExecutionStats.buildState.shortDescription}${historyItem.test}">undefined</c:out></td>
					<td title="x-tcwebhooks-request-id: ${historyItem.webHookExecutionStats.trackingId}">${historyItem.webHookExecutionStats.statusCode} :: ${historyItem.webHookExecutionStats.statusReason}</td>
	   				</tr>
	        	
	        </c:forEach>
	        
		    </table>
		    <bs:pager place="bottom" urlPattern="${pagerUrlPattern}" pager="${historyPager}"/>
		    
			</div>	        
</bs:refreshable>	        
    </jsp:attribute>
</bs:page>
	        
