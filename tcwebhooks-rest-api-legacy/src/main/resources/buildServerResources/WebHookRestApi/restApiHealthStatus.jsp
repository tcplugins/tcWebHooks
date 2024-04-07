<%@ page import="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>

<c:set var="adminUrl" value="<%=healthStatusItem.getAdditionalData().get(\"adminUrl\")%>"/>
<c:set var="possibleProblemFilesCount" value="<%=healthStatusItem.getAdditionalData().get(\"possibleProblemFilesCount\")%>"/>

Editing of WebHook Templates via the WebUI or 
REST API may fail due to a jar conflict in TeamCity's 
bundled REST API jars. <a href="${adminUrl}">More info.</a>