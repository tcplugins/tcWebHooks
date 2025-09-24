<%@ page import="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="showMode" type="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" scope="request"/>
<jsp:useBean id="healthStatusReportUrl" type="java.lang.String" scope="request"/>
<c:set var="inplaceMode" value="<%=HealthStatusItemDisplayMode.IN_PLACE%>"/>

<%--@elvariable id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem"--%>

<c:set var="Project" value="${healthStatusItem.additionalData['Project']}"/>
<c:set var="Count" value="${healthStatusItem.additionalData['WebHookCount']}"/>
<c:if test="${not empty Project && not empty Count}">
    Project <b>${Project.externalId}</b> has <b>${Count} webhooks</b> that have not been migrated.<br>
    Please see the <a href="../webhooks/migration.html?project=${Project.externalId}">${Project.name} WebHooks Migration</a> 
    page for more information.
</c:if>

<c:set var="Status" value="${healthStatusItem.additionalData['Status']}"/>
<c:if test="${not empty Status}">
    The migration of WebHooks did not complete successfully. The migration status is currently marked as <b>${Status}</b>. Manual migration will be required.<br>
    Please see the <a href="../admin/admin.html?item=tcWebHooksV3Migration">WebHooks Migration</a> admin page for full details. 
</c:if>

<br>
<br>
<b>Here is some information that will appear on every health report</b>
<br>
<br>
