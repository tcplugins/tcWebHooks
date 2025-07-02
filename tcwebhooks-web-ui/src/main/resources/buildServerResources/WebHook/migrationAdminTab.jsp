<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>


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
			<c:forEach items="${migrationData}" var="webhooks">
					<c:if test="${fn:length(webhooks.value) > 0}" >
					<tr id="viewRow_${webhooks.key.externalId}" class="webHookRow"><td colspan="5" class="tagsLabel">${webhooks.key.name} (${webhooks.key.externalId})</td></tr>
					</c:if>
				    <c:forEach items="${webhooks.value}" var="hook">
							<tr id="viewRow_${hook.key}" class="webHookRow">
							<td class="name">${hook.key}</td>
							<td class="actionCell"><c:if test="${not empty hook.value.candidate}" >&#9873;</c:if></td>
							<td class="actionCell"><c:if test="${not empty hook.value.migrated}" >&#9873;</c:if></td>
							<td class="actionCell"><c:if test="${not empty hook.value.cached}" >&#9873;</c:if></td>
				    		</tr>
				    </c:forEach>
			</c:forEach>
	    	</tbody>
		</table>
	    
	    </div>
