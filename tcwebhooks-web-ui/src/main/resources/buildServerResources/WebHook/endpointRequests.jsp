<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHooks" scope="request"/>
<bs:page>

    <jsp:attribute name="head_include">
      <bs:linkCSS>
        /css/admin/adminMain.css
        /css/admin/projectConfig.css
        /css/forms.css
        /css/admin/vcsRootsTable.css
        
    /css/visibleProjects.css
    /css/addSidebar.css
    /css/settingsTable.css
    /css/profilePage.css
    /css/userRoles.css
    
    ${jspHome}WebHook/css/styles.css
        
      </bs:linkCSS>
      <bs:linkScript>
        /js/bs/blocks.js
        /js/bs/blocksWithHeader.js
        /js/bs/forms.js
        /js/bs/modalDialog.js
        /js/bs/editBuildType.js
        /js/bs/editProject.js
        /js/bs/adminActions.js
      </bs:linkScript>

      <script type="text/javascript">
        BS.Navigation.items = [
		  {title: "Projects", url: '<c:url value="/overview.html"/>'},
		  <c:if test="${haveProject}"> 
		  	{title: "${projectName}", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		  </c:if>
		  <c:if test="${haveBuild}"> 
		  	{title: "${buildName}", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		  </c:if>
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">
    		<form method="post">
    		<input type="hidden" name="delete"/>
    		<h2>View Webhook Request Payloads<button style="float:right;" class="btn btn_mini ">Clear old requests</button></h2>
		   	</form>	
    		<p><strong>Displaying ${count} recent requests</strong>. To have items displayed here, configure a webhook to post to <em>${postURL}</em></p> 

		    <c:forEach items="${storeItems}" var="item">
		    <div class="endpointWrapper">
			  <div id="viewRow_${item.hash}" class="endpointPayloadHeader">
			  <ul class="payloadHeading">
				<li class="nowrap payloadDate">${item.date}</li>
				<li class="nowrap payloadContentType">${item.contentType}</li>
				<li class="nowrap payloadUrl">${item.url}</li>
				<c:if test="${parseFailure}"> 
					<li class="attentionRed">WARNING: Parsing failed. Is your payload valid?</li>
				</c:if>
			  </ul>
			  </div>
			  <div class="endpointPayload prettyPrint">
				<pre>${item.prettyPrintedPayload}</pre>
			  </div>
			  <div class="endpointPayload ">
				<pre>${item.payload}</pre>
			  </div>
			</div>
		    </c:forEach>
    	
    </jsp:attribute>
</bs:page>
