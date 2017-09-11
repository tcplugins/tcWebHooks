<%@ page import="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>

<c:set var="adminUrl" value="<%=healthStatusItem.getAdditionalData().get(\"adminUrl\")%>"/>

WebHook REST API files have been cleaned. Please restart TeamCity. <a href="${adminUrl}">More info.</a>