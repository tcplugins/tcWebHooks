<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<c:set var="showDetailLink" value="true" scope="request"/>
<c:set var="showReRunMigration" value="false" scope="request"/>

<h4>Kotlin Projects</h4>
There are <b><c:out value="${countOfActiveKotlinProjects}"></c:out></b> projects configured using the Kotlin DSL.

<c:choose>
<c:when test="${not empty dslDiagnosticsResultError}">
				<h4>Error</h4>
				<c:out value="${dslDiagnosticsResultError}"></c:out>
</c:when>
<c:otherwise>
		<h4>tcWebHooks Kotlin DSL Maven Status</h4>
		<div>
		<table class="settings templateTable">
		<tr><td>TeamCity Kotlin DSL update</td><td>${dslDiagnosticsResult.lastTeamCityDslUpdate}</td></tr>
		<tr><td>tcWebHooks Kotlin DSL found</td><td>${dslDiagnosticsResult.tcWebHooksDslFound}</td></tr>
		<tr><td>tcWebHooks Kotlin DSL version</td><td>${dslDiagnosticsResult.tcWebHooksDslVersion}</td></tr>
		<tr><td>tcWebHooks Kotlin DSL update</td><td>${dslDiagnosticsResult.lastTcWebHooksDslUpdate}</td></tr>
		<tr><td>tcWebHooks Kotlin DSL last updated</td><td>${dslDiagnosticsResult.lastTcWebHooksUpdateFriendly}</td></tr>
		</table>
		</div>
</c:otherwise>
</c:choose>


