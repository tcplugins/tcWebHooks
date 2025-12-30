<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<c:set var="showDetailLink" value="true" scope="request"/>
<c:set var="showReRunMigration" value="false" scope="request"/>

	    <div>
	    <table class="settings templateTable">
			<tbody>
			<c:forEach items="${migrationStatuses}" var="migrationStatus">
				<%@ include file="jsp-includes/projectMigrationDetail.jsp" %>
			</c:forEach>
	    	</tbody>
		</table>
	    
	    </div>
