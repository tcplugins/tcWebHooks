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
				
				<td>0 instances</td>

		<c:choose>  
    		<c:when test="${template.templateShortName == 'none'}"> 		
				<td colspan="2">No template available</td>
			</c:when>
			<c:otherwise>  		
						<c:choose>  
				    		<c:when test="${template.templateState.isStateProvided()}"> 
								<td><a href="templateModify.html?action=override&template=${template.templateShortName}&format=${template.formatShortName}">Customise</a></td>
				         	</c:when>  
				    		<c:when test="${template.templateState.isStateUserDefined()}"> 
								<td><a href="templateModify.html?action=edit&template=${template.templateShortName}&format=${template.formatShortName}">Edit</a></td>
				         	</c:when>  
				    		<c:when test="${template.templateState.isStateUserOverridden()}"> 
								<td><a href="templateModify.html?action=edit&template=${template.templateShortName}&format=${template.formatShortName}">Edit</a></td>
				         	</c:when>  
				        	<c:otherwise>  
								<td>Edit</td>
				         	</c:otherwise>  
						</c:choose> 				
								<td><a href="templateModify.html?action=clone&template=${template.templateShortName}&format=${template.formatShortName}">Clone</a></td>
			</c:otherwise>  
		</c:choose>
		
			  </tr>	
		    </c:forEach>
    	</tbody>
    	</table>
    </jsp:attribute>
</bs:page>