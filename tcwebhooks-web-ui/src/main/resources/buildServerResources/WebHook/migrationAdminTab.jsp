<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<c:set var="showDetailLink" value="true" scope="request"/>

	    <div style="width: 50%">
	    <table class="settings templateTable">
			<tbody>
			<c:forEach items="${migrationData}" var="webhooks">
				<%@ include file="jsp-includes/projectMigrationDetail.jsp" %>
			</c:forEach>
	    	</tbody>
		</table>
	    
	    </div>
