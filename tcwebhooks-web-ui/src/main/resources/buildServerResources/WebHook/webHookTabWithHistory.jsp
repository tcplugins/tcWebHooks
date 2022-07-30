<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib prefix="afn" uri="/WEB-INF/functions/authz"
%><%@ include file="/include.jsp" %>
	<div>

	${permissionError}

    <table id="webHookSearchTable" class="buildTab settings">
			<c:forEach items="${projectAndParents}" var="projectList">
	   		<thead>
			<tr style="background-color: rgb(245, 245, 245);"><th colspan="4" style="border-bottom:1px solid #ccc; border-top:1px solid #ccc;">
			<h3 style="text-align: left; padding-left: 10px;">
				<c:choose>
				<c:when test="${projectList.externalProjectId == '_Root'}">
					WebHooks configured for every TeamCity build (_Root)
				</c:when>
				<c:otherwise>
					WebHooks configured for <c:out value="${projectList.sensibleProjectFullName}" />
				</c:otherwise>
				</c:choose>
					<c:if test="${afn:permissionGrantedForProjectWithId(projectList.projectId, 'EDIT_PROJECT')}">
						&nbsp;-&nbsp;<a href="./webhooks/index.html?projectId=${projectList.externalProjectId}">Edit Project WebHooks</a>
					</c:if>
			</h3>
			</th></tr>
		   		<tr style="background-color: rgb(245, 245, 245);">
					<th class="name">URL</th>
					<th class="name">Format</th>
					<th class="name">Build Events</th>
					<th class="value" style="width:20%;">Enabled Builds</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${projectList.webHookList}" var="hook">

					<tr id="viewRow_${hook.uniqueKey}" class="webHookRow" style="line-height:1;">
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
					<td class="value <%--highlight--%> webHookRowItemFormat" style="width:15%;"><a title='<c:out value="${hook.templateToolTip}"/>' href="./webhooks/template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}" /></a></td>
								</c:otherwise>
							</c:choose>


						<td class="value <%--highlight--%>" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"--%>><c:out value="${hook.enabledEventsListForWeb}" /></td>
						<td class="value <%--highlight--%>" title="${hook.buildTypeCountAsToolTip}" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#buildPane');"--%>><c:out value="${hook.buildTypeCountAsFriendlyString}" /></td>
						<%--
						<td class="edit highlight"><a onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');" href="javascript://">edit</a></td>
						<td class="edit highlight"><a onclick="WebHooksPlugin.showDeleteDialog('${hook.uniqueKey}');" href="javascript://">delete</a></td>
						 --%>
					</tr>
				</c:forEach>
				<tr class="blankRow"><td colspan="5">&nbsp;</td></tr>
			</c:forEach>
			
			
			
			<c:forEach items="${buildWebHooks}" var="projectList">
		   		<thead>
				<tr style="background-color: rgb(245, 245, 245);"><th colspan="4" style="border-bottom:1px solid #ccc; border-top:1px solid #ccc;">
				<h3 style="text-align: left; padding-left: 10px;">
						WebHooks configured for <c:out value="${projectList.sensibleProjectFullName}" /> &gt; <c:out value="${buildName}"/>
						<c:if test="${afn:permissionGrantedForProjectWithId(projectList.projectId, 'EDIT_PROJECT')}">
							&nbsp;-&nbsp;<a href="./webhooks/index.html?buildTypeId=<c:out value="${buildExternalId}" />">Edit build WebHooks</a>
						</c:if>
				</h3>
				</th></tr>
			   		<tr style="background-color: rgb(245, 245, 245);">
						<th class="name">URL</th>
						<th class="name">Format</th>
						<th class="name">Build Events</th>
						<th class="value" style="width:20%;">Enabled Builds</th>
					</tr>
				</thead>			
				<c:forEach items="${projectList.webHookList}" var="hook">

					<tr id="viewRow_${hook.uniqueKey}" class="webHookRow" style="line-height:1;">
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
					<td class="value <%--highlight--%> webHookRowItemFormat" style="width:15%;"><a title='<c:out value="${hook.templateToolTip}"/>' href="./webhooks/template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}" /></a></td>
								</c:otherwise>
							</c:choose>


						<td class="value <%--highlight--%>" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');"--%>><c:out value="${hook.enabledEventsListForWeb}" /></td>
						<td class="value <%--highlight--%>" title="${hook.buildTypeCountAsToolTip}" style="width:15%;" <%-- onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#buildPane');"--%>><c:out value="${hook.buildTypeCountAsFriendlyString}" /></td>
						<%--
						<td class="edit highlight"><a onclick="WebHooksPlugin.showEditDialog('${hook.uniqueKey}','#hookPane');" href="javascript://">edit</a></td>
						<td class="edit highlight"><a onclick="WebHooksPlugin.showDeleteDialog('${hook.uniqueKey}');" href="javascript://">delete</a></td>
						 --%>
					</tr>
					
				</c:forEach>
				<tr class="blankRow"><td colspan="5">&nbsp;</td></tr>
			</c:forEach>	
			</tbody>
		</table>
		<h2 class="noBorder">All Recent WebHook Events</h2>
	        The following table shows the ${items.itemsOnThisPage} most recent <span class="lowercase">${countContext} webhook events</span>.
	        <table class="settings" style="width:100%;">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Date</th><th>Build</th><th>URL (some URLs may be partially hidden)</th><th>Build Event</th><th>Event Response</th></tr>

	        <c:forEach items="${items.items}" var="historyItem">
	        		<tr>
					<td>${historyItem.webHookExecutionStats.initTimeStamp}</td>
					<c:if test="${not empty historyItem.buildId}">
					    <td><a href="../viewLog.html?buildId=<c:out value="${historyItem.buildId}"/>"><c:out value="${historyItem.buildTypeName}"/> #<c:out value="${historyItem.buildId}"/></a></td>
					</c:if>
					<c:if test="${empty historyItem.buildId}">
					    <td><a href="../viewType.html?buildTypeId=<c:out value="${historyItem.buildTypeExternalId}"/>"><c:out value="${historyItem.buildTypeName}"/></a></td>
					</c:if>

					<c:if test="${afn:permissionGrantedForProjectWithId(historyItem.webHookConfig.projectInternalId, 'EDIT_PROJECT')}">
						<c:choose>
							<c:when test="${not historyItem.webHookExecutionStats.secureValueAccessed}">
								<td><span title="Webhook from project '<c:out value="${historyItem.webHookConfig.projectExternalId}"/>'"><c:out value="${historyItem.webHookExecutionStats.url}"/></span></td>
							</c:when>
							<c:when test="${historyItem.webHookExecutionStats.secureValueAccessed && not webhookSecureEnabledMap.get(historyItem.webHookConfig.uniqueKey)}">
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
	   				</tr>

	        </c:forEach>

		    </table>

		</div>
