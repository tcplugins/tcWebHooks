<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include.jsp" %>
<c:set var="pageTitle" value="Edit WebHooks" scope="request"/>
<c:set var="webhookConfigDialogScope" value="WebHooksPlugin.Configurations.WithoutRestApi" scope="request"/>
<c:set var="parameterConfigDialogScope" value="WebHooksPlugin.Parameters" scope="request"/>
<c:set var="showLinksToOldEditPage" value="false" scope="request"/>
<c:set var="showLinksForInlineEditingIfRestApiMissing" value="true" scope="request"/>
<c:set var="showEditButton" value="${showLinksForInlineEditingIfRestApiMissing}" scope="request"/>
<c:set var="parentTemplateCount" value="0" scope="request"/>
<c:set var="parentParameterCount" value="0" scope="request"/>
<bs:page>

    <jsp:attribute name="head_include">
      <bs:linkCSS>
        /css/admin/adminMain.css
        /css/admin/projectConfig.css
        /css/forms.css
        /css/admin/vcsRootsTable.css
        
    /css/visibleProjects.css
    /css/settingsTable.css
    /css/profilePage.css
    /css/userRoles.css
    
    ${jspHome}WebHook/css/styles.css
    ${jspHome}WebHook/3rd-party/highlight/styles/tomorrow.css
    ${jspHome}WebHook/3rd-party/jquery.sweet-dropdown-1.0.0/jquery.sweet-dropdown.min.css
        
      </bs:linkCSS>
      <bs:linkScript>
        /js/bs/blocks.js
        /js/bs/blocksWithHeader.js
        /js/bs/forms.js
        /js/bs/modalDialog.js
        /js/bs/editBuildType.js
        /js/bs/editProject.js
        /js/bs/adminActions.js
      ${jspHome}WebHook/js/editWebhookCommon.js
      ${jspHome}WebHook/js/editWebhookParameter.js
      ${jspHome}WebHook/js/editWebhookFilter.js
      ${jspHome}WebHook/js/editWebhookHeader.js
      ${jspHome}WebHook/js/editWebhookConfiguration.js
      ${jspHome}WebHook/js/editWebhookConfigurationWithoutRestApi.js
      ${jspHome}WebHook/js/noRestApi.js
	</bs:linkScript>
	
	<script type="text/javascript">
		BS.Navigation.items = [
		{title: "Projects", url: '<c:url value="/overview.html"/>'},
		<c:if test="${haveProject}"> 
			{title: "<c:out value="${projectName}"/>", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		</c:if>
		<c:if test="${haveBuild}"> 
			{title: "<c:out value="${buildName}"/>", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		</c:if>
		{title: "${pageTitle}", selected:true}
        ];
		
	</script>
</jsp:attribute> 

<jsp:attribute name="body_include">
	
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/jquery.easytabs.min.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/jquery.color.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/moment-2.22.2.min.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/highlight/highlight.pack.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/jquery.sweet-dropdown-1.0.0/jquery.sweet-dropdown.min.js"></script>

    <div class="editBuildPageGeneral webhookContainer" style="background-color:white; float:left; margin:0; padding:0; width:70%;">
    
   		${permissionError}
		
		<%@ include file="jsp-includes/projectParentItemsTable.jsp" %>

		<c:choose>
			<c:when test="${haveBuild}"> 
				<h2 class="webhookHeading">WebHooks applicable to build <c:out value="${buildName}"/></h2>
				To edit all webhooks for builds in the project <a href="index.html?projectId=${projectExternalId}">edit Project webhooks</a>.
			</c:when>
			<c:when test="${projectExternalId == '_Root'}">
				<h2 class="webhookHeading">WebHooks configured for every TeamCity build (_Root project)</h2>
			</c:when>
			<c:otherwise>
				<h2 class="webhookHeading">WebHooks configured for the <i><c:out value="${project.fullName}"/></i> project</h2>
			</c:otherwise>
		</c:choose>
		
		<%@ include file="jsp-includes/projectWebHooksTable.jsp" %>

		<p>
    
    	<%@ include file="jsp-includes/projectParametersTable.jsp" %>
		<p>
		<%@ include file="jsp-includes/projectTemplatesTable.jsp" %>

  		<div id="messageArea"></div>
	    <div id="systemParams"><!--  begin systemParams div -->

		<c:choose>
			<c:when test="${not haveProject}">
				<strong>${errorReason}</strong><br/>Please access this page via the WebHooks tab on a project or build overview page. 
			</c:when>
			<c:otherwise>
					<c:if test="${not hasPermission}">
						<c:choose>
							<c:when test="${projectName == '_Root'}">
								<strong>You must have System Administrator permission to edit _Root WebHooks</strong>
							</c:when>
							<c:otherwise>	
								<strong>You must have Project Administrator permission to edit WebHooks</strong>
							</c:otherwise>
						</c:choose>
					</c:if>
			</c:otherwise>
		</c:choose>

        </div><!--  end systemParams div -->
      </div>
    <div id=sidebarAdmin>
      <div class=configurationSection>
      	<h2 class="webhookHeading">WebHook Information</h2>
          <p>WebHooks are simply HTTP POST requests or "callbacks" triggered by events. They allow one web application (in this case TeamCity) to notify another web app of events.</p>
          <p>When an event occurs, the tcWebHooks plugin will submit an HTTP POST to the URL configured. The receiving webapp is then able to use the information for any purpose. It could be used to light a lava lamp, or post a message on an IRC channel.</p>

			<c:choose>
				<c:when test="${ShowFurtherReading == 'ALL'}">
				          <p>Further Reading:
				          <ul>${moreInfoText}
				          	<li><a href="http://netwolfuk.wordpress.com/teamcity-plugins/">tcWebHooks plugin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'DEFAULT'}">
				          <p>Further Reading:
				          <ul>
				          	<li><a href="http://netwolfuk.wordpress.com/teamcity-plugins/">tcWebHooks plugin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'SINGLE'}">
				          <p>Further Reading:</p>
				          <ul>${moreInfoText}</ul>
				</c:when>
			</c:choose>
			
		  <h2 class="webhookHeading">WebHook Templates</h2>
		  <p><a href="templates.html">WebHook Templates</a> are a way of packaging up a set of payloads together. The template can then be re-used by any number of webhooks.</p>
		  
		  
		  <h2 class="webhookHeading">Testing Endpoint</h2>	
		  <p>It is possible to test webhooks by posting them back to the tcWebHooks plugin inside TeamCity. See <a href="endpoint-viewer.html"/>here for details</a>.</p>
		  
		  <h2 class="webhookHeading">Plugin Information</h2>	
		  <p>tcWebHooks version: <strong>${pluginVersion}</strong></p>
      </div>
    </div>

	<%@ include file="jsp-includes/editWebHookDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookParameterDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookParameterDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookHeaderDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookHeaderDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookFilterDialog.jsp" %>
	<%@ include file="jsp-includes/deleteWebHookFilterDialog.jsp" %>
	<%@ include file="jsp-includes/noRestApiDialog.jsp" %>
	
	<script type="text/javascript">
		var restApiDetected = ${isRestApiInstalled};
		var ProjectBuilds = ${projectWebHooksAsJson};
	</script>
    </jsp:attribute>
</bs:page>