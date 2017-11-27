<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <h2 class="noBorder">WebHook REST API</h2>
	        <h3>Health Report</h3>
	        
	        <bs:messages key="apiFixResult"/>
	        
	        <table class="settings">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>URL</th><th>Error</th></tr>
	        <c:forEach items="${history}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<td>${historyItem.webHook.url}</td>
					<td>${historyItem.httpStatus} :: ${historyItem.httpStatusDescription}</td>
	   				</tr>
	        	
	        </c:forEach>
	        
		    </table>

			</div>	        
</bs:refreshable>	        

	        