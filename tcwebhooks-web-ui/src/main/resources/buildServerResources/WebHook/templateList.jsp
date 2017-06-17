<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHook Templates" scope="request"/>
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
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">
    
    <table id="webHookTemplateTable" class="settings">
		<thead>
		<tr style="background-color: rgb(245, 245, 245);">
		<th class="">Name</th>
		<th class="">Payload Format</th>
		<th class="">Supported Build Events</th>
		<th class="">Type</th>
		<th class="value" colspan="3" style="width:20%;">Usage</th>
		</tr>
		</thead>
		<tbody>
		    <c:forEach items="${webHookTemplates}" var="template">
			  <tr id="viewRow_${template.templateShortName}" class="webHookTemplate">
				<td class="nowrap heading">${template.templateDescription}</td>
				<td class="nowrap">${template.formatDescription}</td>
				<td>
					<ul class="commalist">
					<c:forEach items="${template.supportedBuildEnumStates}" var="state">	
						<li>${state.shortDescription}</li>
					</c:forEach>
					</ul>
				</td>
				<td class="nowrap">${template.templateState.description}</td>
				
				<td><i>not implemented yet</i></td>

		<c:choose>  
    		<c:when test="${template.templateShortName == 'none'}"> 		
				<td>No template available</td>
			</c:when>
			<c:otherwise>  		
				<td><a href="template.html?template=${template.templateShortName}">View</a></td>
			</c:otherwise>  
		</c:choose>
		
			  </tr>	
		    </c:forEach>
    	</tbody>
    	</table>
    </jsp:attribute>
</bs:page>