<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

		<div>
				<p>WebHooks are simply HTTP POST requests or "callbacks" triggered by events.
					They allow one web application (in this case TeamCity) to notify another web app of events.</p>
					<p>When an event occurs, the tcWebHooks plugin will submit an HTTP POST to the URL configured.
						The receiving webapp is then able to use the information for any purpose. It could be used to
						light a lava lamp, or post a message on an IRC channel.</p>
			<p>There are <a href="../webhooks/search.html?show=all">${webHooksCount} WebHooks</a> configured in this TeamCity Server.<p>

					<h2>WebHook Templates</h2>
					<p>There are ${webHookTemplatesCount} WebHook Templates installed in this TeamCity server.<p>
					<p><a href="../webhooks/templates.html">WebHook Templates</a> are a way of packaging up a set of payloads together. The template can then be re-used by any number of webhooks.</p>
					<p>Please see the <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHook-Templates-:-An-Introduction">WebHook Templates wiki pages</a> for more information.</p>

					<bs:refreshable containerId="webhooksStatisticsContainer" pageUrl="${pageUrl}">
					<h2>WebHook Execution History Statistics</h2>
					The following chart shows statistics of the status returned by webhook executions over the past 60 days.
					Statistics are assembled hourly, and persisted across TeamCity restarts.
					<c:choose>
						<c:when test="${webhookStatisticsEnabled}" >
							<span style="margin-left:2em;float:right;"><a href="javascript://" 
								onclick="executeAction({'action': 'refreshStatistics'});" 
								title="Statistics are assembled hourly and persisted to disk. Click to refresh now.">Refresh graph</a></span>
							<canvas id="historyChart"></canvas>
						</c:when>
						<c:otherwise>
							<div class="historyChart" style="border: 1px solid grey; padding: 1.5em; margin: 1em;">
								This chart is not shown because Statistics collection is currently Disabled 
							</div>
						</c:otherwise>
					</c:choose>
					
					<bs:messages key="updateStatisticsResult"/>
					<div style="border: 1px solid grey; padding: 1.5em; margin: 1em;" id="statisticsOptions">
						<div>
							<c:choose>
								<c:when test="${webhookStatisticsEnabled}">							
									<h3 style="margin-top: 0px; margin-bottom: 0px">Statistics collection is currently Enabled  
										<button onclick="executeAction({'action': 'updateStatistics', 'updateStatistics':'disabled'});" style="float:right; margin-top: 1em;" class="btn btn_primary submitButton">Disable Statistics Collection</button></h3>
								</c:when>
								<c:otherwise>
									<h3 style="margin-top: 0px; margin-bottom: 0px">Statistics collection is currently Disabled  
										<button onclick="executeAction({'action': 'updateStatistics', 'updateStatistics':'enabled'});" style="float:right; margin-top: 1em;" class="btn btn_primary submitButton">Enable Statistics Collection</button></h3>
								</c:otherwise>
							</c:choose>
							tcWebHooks collects statistics about WebHook executions. The statistics collected populated the above graph.
						</div>
						<c:if test="${webhookStatisticsEnabled}" >
							<div>
								<c:choose>
									<c:when test="${webhookAnalyticsEnabled}">
										<h3 style="margin-bottom: 0px">Analytics sharing is currently Enabled  
											<button onclick="executeAction({'action': 'updateAnalytics', 'updateAnalytics':'disabled'});" style="float:right; margin-top: 1em;" class="btn btn_primary submitButton">Disable Analytics Sharing</button></h3>
									</c:when>
									<c:otherwise>
										<h3 style="margin-bottom: 0px">Analytics sharing is currently Disabled  
											<button onclick="executeAction({'action': 'updateAnalytics', 'updateAnalytics':'enabled'});" style="float:right; margin-top: 1em;" class="btn btn_primary submitButton">Enable Analytics Sharing</button></h3>
									</c:otherwise>
								</c:choose>
								Help make tcWebHooks better by <a href="https://github.com/tcplugins/tcWebHooks/wiki/Analytics" title="View more information on tcWebHooks wiki">sharing webhook data</a> with the tcWebHooks developers. <a title="Requires REST API to be installed" href="../../app/rest/webhooks/statistics">See the data that would be shared</a>.
							</div>
						</c:if>
					</div>
<c:if test="${webhookStatisticsEnabled}" >
	<script>
		var graphData = ${statistics};
		var ctx = document.getElementById('historyChart').getContext('2d');
		var chart = new Chart(ctx, {
			type: 'bar',
			data: graphData,
			options : {
				aspectRatio: 4,
				scales : {
					xAxes : [ {
						offset: true,
						stacked : true,
						type : 'time',
						time : {
							unit : 'week'
						}
					} ],
					yAxes : [ {
						stacked : true,
						ticks: {
							beginAtZero: true
						}
					} ]
				},
				legend: {
					display: false
				}
			}
		});

	</script>
</c:if>
					</bs:refreshable>
					<h2>WebHook Execution History Details</h2>
					The following data is assembled from the in-memory execution results. Results are cleared when TeamCity is restarted.
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
	<script>
		function executeAction(parameters) {
			BS.ajaxRequest(window['base_uri'] + '/admin/manageWebHookStatistics.html', {
				parameters: parameters,
    			onComplete: function(transport) {
    				var shouldClose = true;
    				var shouldRedirect = false;
    				if (transport != null && transport.responseXML != null) {
    					var response = transport.responseXML.getElementsByTagName("response");
    					if (response != null && response.length > 0) {
    						var responseTag = response[0];
    						var error = responseTag.getAttribute("error");
    						if (error != null) {
								shouldClose = false;
    						} else if (responseTag.getAttribute("status") == "OK") {
    							shouldClose = true;
    							if (responseTag.getAttribute("redirect") == "true") {
									shouldRedirect = true;
    							}
    						} else if (responseTag.firstChild == null) {
    							shouldClose = false;
    							alert("Error: empty response");
    						}
    					}
    				}
    				if (shouldRedirect) {
    					dialog.close();
    					window.location = window['base_uri'] + '/admin/editDebianRepository.html?repo=' + $j("#addRepoForm input[id='debrepo.name']").val()
    				} else if (shouldClose) {
    					$("webhooksStatisticsContainer").refresh();
    				}
					
    			}
    		});
            return false;
		}
	</script>
