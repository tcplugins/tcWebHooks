<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <h2 class="noBorder">WebHooks</h2>
	        
	        <p>The most recent 10,000 webhook executions are stored in memory in TeamCity. These are cleared on restart.</p><p>Since the last TeamCity restart, there have been:</p>
	        <div class="stats-table-wrapper">
	        <div class="webhookCounts">
		        <div class="webhookOkCount webhookCount">
		        	<span class="count">${okCount}</span>
		        	<span class="description">Successful WebHook Events</span>
		        </div>
		        <div class="spacer"></div>
		        <div class="webhookErrorCount webhookCount">
		        	<span class="count">${errorCount}</span>
		        	<span class="description">Errored WebHook Events</span>	        
		        </div>
		        <div class="spacer"></div>
		        <div class="webhookSkippedCount webhookCount">
		        	<span class="count">${skippedCount}</span>
		        	<span class="description">Skipped WebHook Events</span>	        
		        </div>
		        <div class="spacer"></div>
		        <div class="webhookTotalCount webhookCount">
		        	<span class="count">${totalCount}</span>
		        	<span class="description">Total WebHook Events</span>
		        </div>
	        </div>
	        </div>
	        <hr>
	        <p>
	        <h2 class="noBorder">Recent WebHook Errors</h2>
	        The following table shows the 20 most recent webhook errors.
	        <table class="settings">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>URL</th><th>Error</th></tr>
	        <c:forEach items="${history}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<td>${historyItem.webHookExecutionStats.url}</td>
					<td title="x-tcwebhooks-request-id: ${historyItem.webHookExecutionStats.trackingId}">${historyItem.webHookExecutionStats.statusCode} :: ${historyItem.webHookExecutionStats.statusReason}</td>
	   				</tr>
	        	
	        </c:forEach>
	        
		    </table>

			</div>	        
</bs:refreshable>	        

	        