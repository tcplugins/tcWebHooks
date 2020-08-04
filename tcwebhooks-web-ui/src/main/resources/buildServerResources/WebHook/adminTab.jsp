<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

		<div>
	        <h2>WebHook Templates</h2>
	        <p>There are ${webHookTemplatesCount} WebHook Templates installed in this TeamCity server.<p>
	        <p><a href="../webhooks/templates.html">WebHook Templates</a> are a way of packaging up a set of payloads together. The template can then be re-used by any number of webhooks.</p>
	        <p>Please see the <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHook-Templates-:-An-Introduction">WebHook Templates wiki pages</a> for more information.</p>
	        
	        <h2>WebHook Execution History</h2>
	        <p>Since the last TeamCity restart, there have been:</p>
	        <div class="stats-table-wrapper">
	        <div class="webhookCounts">
		        <div class="webhookOkCount webhookCount"><a href="../webhooks/history.html?view=ok">
		        	<span class="count">${okCount}</span>
		        	<span class="description">Successful Events</span>
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookErrorCount webhookCount"><a href="../webhooks/history.html?view=errors">
		        	<span class="count">${errorCount}</span>
		        	<span class="description">Errored Events</span>	        
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookSkippedCount webhookCount"><a href="../webhooks/history.html?view=skipped">
		        	<span class="count">${skippedCount}</span>
		        	<span class="description">Skipped Events</span>	        
		        </a></div>
		        <div class="spacer"></div>
		        <div class="webhookTotalCount webhookCount"><a href="../webhooks/history.html?view=all">
		        	<span class="count">${totalCount}</span>
		        	<span class="description">Total Events</span>
		        </a></div>
	        </div>
	        </div>
	        <p>
	        <h2 class="noBorder">Recent WebHook Errors</h2>
	        <p>A log of the most recent 10,000 webhook executions is stored in memory in TeamCity. These are cleared on restart. Click the numbers above to see relevant events.</p> The following table shows the 20 most recent webhook errors. 
	        <table class="settings">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>URL</th><th>Build Event</th><th>Error</th></tr>
	        <c:forEach items="${history}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<td><c:out value="${historyItem.webHookExecutionStats.url}"/></td>
					<td><c:out value="${historyItem.webHookExecutionStats.buildState.shortDescription}">undefined</c:out></td>
					<td title="x-tcwebhooks-request-id: ${historyItem.webHookExecutionStats.trackingId}">${historyItem.webHookExecutionStats.statusCode} :: <c:out value="${historyItem.webHookExecutionStats.statusReason}"/></td>
	   				</tr>
	        	
	        </c:forEach>
	        
		    </table>

		</div>	        

	        