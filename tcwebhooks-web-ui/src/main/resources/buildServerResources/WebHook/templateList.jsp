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
      ${jspHome}WebHook/js/addWebhookTemplate.js
      </bs:linkScript>

    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: '${title}', selected: true}
      ];
    </script>
    </jsp:attribute>
    
  <jsp:attribute name="quickLinks_include">
    <div class="toolbarItem" style="padding-top:2em;">
    <button style="float:right;" class="btn" onclick="WebHooksPlugin.addTemplate(); return false">Add New Template</button>
    </div>
  </jsp:attribute>    
      
    <jsp:attribute name="body_include">
    
    <script type=text/javascript>
    		var restApiDetected = ${isRestApiInstalled};
		</script>		
		<div>
		<h2>The following table shows the WebHooks Templates installed in this TeamCity instance.</h2> 
		A guide to <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHook-Templates-:-An-Introduction">WebHook Templates is on the tcWebHooks Wiki</a>. 
		<p>Additional templates can be downloaded from the 
		<a href="https://github.com/tcplugins/tcWebHooksTemplates">tcWebHooksTemplates project</a> on GitHub. 
		User contributions are welcome. Please see the readme for instructions on importing and exporting WebHook Templates.<p>
		<c:if test="${not isRestApiInstalled}">
			<p>Viewing or making changes to template content will not be possible using the WebHook Templates UI because the <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API</a> is not installed.
		</c:if>
    	</div>
    <table id="webHookTemplateTable" class="settings">
		<thead>
		<tr style="background-color: rgb(245, 245, 245);">
		<th class="">Description</th>
		<th class="">Payload Format</th>
		<th class="">Supported Build Events</th>
		<th class="">Type</th>
		<th class="value" colspan="3" style="width:20%;">Usage</th>
		</tr>
		</thead>
		<tbody>
		    <c:forEach items="${webHookTemplates}" var="template">
			  <tr id="viewRow_${template.templateId}" class="webHookTemplate">
				<td class="nowrap heading" title="<c:out value="${template.templateToolTip}"/> (id: <c:out value="${template.templateId}"/>)">${template.templateDescription}</td>
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
    		<c:when test="${template.templateId == 'none'}"> 		
				<td>No template available</td>
			</c:when>
			<c:otherwise>  		
				<td><a href="template.html?template=${template.templateId}">View</a></td>
			</c:otherwise>  
		</c:choose>
		
			  </tr>	
		    </c:forEach>
    	</tbody>
    	</table>

    <bs:dialog dialogId="noRestApiDialog"
               dialogClass="noRestApiDialog"
               title="No WebHoooks REST API Plugin detected"
               closeCommand="WebHooksPlugin.NoRestApiDialog.close()">
        <forms:multipartForm id="noRestApiForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.NoRestApiDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>The WebHoooks REST API Plugin was not detected. This page makes heavy use of
                		the WebHooks REST API to provide editing of WebHook Templates.<p>
                		Please install the <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> to use this page.
                </td></tr>
            </table>
            <div class="popupSaveButtonsBlock">
                <forms:cancel onclick="WebHooksPlugin.NoRestApiDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    	
    <bs:dialog dialogId="addTemplateDialog"
               dialogClass="addTemplateDialog"
               title="Add New Webhook Template"
               closeCommand="WebHooksPlugin.AddTemplateDialog.close()">
        <forms:multipartForm id="addTemplateForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.AddTemplateDialog.doPost();">

            <table class="runnerFormTable">
                <tr><tdcolspan=2>
                	<div class="templateShort">
                			<p><strong>Are you sure you want to create a new template from scratch?</strong></p>
                			<p>It might be easier to cancel this dialog and copy one of the existing templates.<p>
                			<p>To copy an existing template, click &quot;view&quot; 
                			on the template and choose &quot;Copy template...&quot; from the Actions menu on the next page.</p>
                	</div>

                	<div class="templateDetails">
                			<p><strong>Fill in the fields below to create a new template.</strong></p>
                			<p>The most important thing is that the ID must be unique.</p>
                			<p>Once you have created a new template you will be redirected to the template view page
                			 where you will need to add build event template items to it. 
                	</div>
                	                
                <div id="ajaxTemplateAddResult"></div>
                </td></tr>
                <tr class="templateDetails">
                    <th>ID<l:star/></th>
                    <td>
                        <div><input type="text" id="template.id" name="template.id"/></div>
                    </td>
                </tr>
                <tr class="templateDetails">
                    <th>Name<l:star/></th>
                    <td>
                        <div><input type="text" id="template.description" name="template.description"/></div>
                    </td>
                </tr>
                <tr class="templateDetails">
                    <th>Tooltip</th>
                    <td>
                        <div><input type="text" id="template.tooltip" name="template.tooltip"/></div>
                    </td>
                </tr>
                <tr class="templateDetails">
                    <th>Rank<l:star/></th>
                    <td>
                        <div><input type="text" id="template.rank" name="template.rank"/></div>
                    </td>
                </tr>
                <tr class="templateDetails">
                    <th>Date Format</th>
                    <td>
                        <div><input type="text" id="template.dateFormat" name="template.dateFormat"/></div>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="addTemplate"/>
            <div class="popupSaveButtonsBlock">
                <forms:cancel id="addTemplateDialogExpand" onclick="WebHooksPlugin.AddTemplateDialog.expandDialog()" label="Yes, I want to create a New Template"/>
                <forms:submit id="addTemplateDialogSubmit" label="New Template"/>
                <forms:cancel onclick="WebHooksPlugin.AddTemplateDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>    	
    	
    </jsp:attribute>
</bs:page>