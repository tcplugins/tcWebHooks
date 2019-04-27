<%--
~ Copyright 2017 Net Wolf UK
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>
<%@ include file="/include.jsp" %>

<c:set var="pageTitle" value="Edit WebHook Template" scope="request"/>

<bs:page>
  <jsp:attribute name="head_include">
    <bs:linkCSS>
      /css/admin/adminMain.css
      ${teamcityPluginResourcesPath}WebHook/css/styles.css
      ${teamcityPluginResourcesPath}WebHook/highlight/styles/tomorrow.css
    </bs:linkCSS>
    <bs:linkScript>
      ${teamcityPluginResourcesPath}WebHook/js/moment-2.22.2.min.js
      ${teamcityPluginResourcesPath}WebHook/js/editWebhookTemplate.js
      ${teamcityPluginResourcesPath}WebHook/highlight/highlight.pack.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Webhook Templates", url: '<c:url value="/webhooks/templates.html"/>'},
        {title: '<c:out value="${webhookTemplateBean.templateDescription}"/>', selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="quickLinks_include">
    <div class="toolbarItem">
	    <c:set var="menuItems">
		    <authz:authorize allPermissions="CHANGE_SERVER_SETTINGS">
		      <jsp:body>
		        <l:li>
			      <a href="#" title="Edit Template Details" onclick="return WebHooksPlugin.editTemplateDetails('${webhookTemplateBean.templateId}'); return false">Edit template details...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Make a copy of this Template" onclick="return WebHooksPlugin.copyTemplate('${webhookTemplateBean.templateId}'); return false">Copy template...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Export Template" onclick="WebHooksPlugin.exportTemplate('${webhookTemplateBean.templateId}'); return false">Export template...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Disable Template" onclick="WebHooksPlugin.disableTemplate('${webhookTemplateBean.templateId}'); return false">Disable template...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Delete Template" onclick="WebHooksPlugin.deleteTemplate({ templateId: '${webhookTemplateBean.templateId}' }); return false">Delete template...</a>
		        </l:li>
		      </jsp:body>
		    </authz:authorize>
		</c:set>
		<c:if test="${not empty fn:trim(menuItems)}">
		  <bs:actionsPopup controlId="prjActions${projectExternalId}"
		                   popup_options="shift: {x: -150, y: 20}, className: 'quickLinksMenuPopup'">
		    <jsp:attribute name="content">
		      <div>
		        <ul class="menuList">
		          ${menuItems}
		        </ul>
		      </div>
		    </jsp:attribute>
		    <jsp:body>Actions</jsp:body>
		  </bs:actionsPopup>
		</c:if>
    </div>
  </jsp:attribute>

  <jsp:attribute name="body_include">
  	<!--  
  		We load the ACE editor here because it loads extra resources, and if they were bundled in with the
  		TeamCity scripts in the linkscript tag, the base URL changes and ACE can't load its dependencies.  
  	 -->
    <script type=text/javascript src="..${jspHome}WebHook/js/ace-editor/src-min-noconflict/ace.js"></script>
    <script type=text/javascript src="..${jspHome}WebHook/js/ace-editor/src-min-noconflict/ext-language_tools.js"></script>
    <script type=text/javascript src="..${jspHome}WebHook/js/jquery.easytabs.min.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/js/jquery.color.js"></script>
	    <script type=text/javascript>
		var webhookDialogWidth = -1;
		var webhookDialogHeight = -1;
		var templatePaneOuterHeight = -1;
		$j(document).ready( function() {
				$j('#tab-container').easytabs({
					  animate: false,
					  updateHash: false
				});
				$j('#tab-container').bind('easytabs:after', function() {
					// one should call editor.resize() after changing size or visibility of the div 
					editor.resize();
					editorBranch.resize();
				});
		});
		var restApiDetected = ${isRestApiInstalled};
		</script>		
	<c:if test="${not isRestApiInstalled}">
		<div class="icon_before icon16 attentionRed">The <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> is not installed. Most settings on this page will non-functional.</div>
	</c:if>
		
    <bs:refreshable containerId="templateInfoContainer" pageUrl="${pageUrl}">

	<bs:messages key="templateUpdateResult"/>
		${webhookTemplateBean.templateState.description}
	  <input type="hidden" name="action" id="WebhookTemplateaction" value=""/>
      <table class="settings parameterTable" id="webhookTemplateHeader">
        
        <tr>
          <th style="width:15%;" title="Used to map WebHooks to their Template">Template Id:</th><td style="width:35%;">${webhookTemplateBean.templateId}</td>
          <th style="width:10%;" title="Determines Template ordering in the WebHook UI (smallest number first)">Rank:</th><td style="width:10%; border:none;">${webhookTemplateBean.rank}</td>
          <c:choose>
		  	<c:when test="${not empty webhookTemplateBean.dateFormat}">
          	<th style="width:15%;" title="Used as the default date format when now,currentTime,buildStartTime,buildFinishTime, is used in a template. Use a SimpleDateFormat compatible string.">Date Format:</th><td style="border:none;"><c:out value="${webhookTemplateBean.dateFormat}"/></td>
          	</c:when>
          	<c:otherwise>
          	<th style="width:15%;">Date Format:</th><td style="border:none;"><i>none</i></td>
          	</c:otherwise>
          </c:choose>
        </tr>
        <tr>
          <th style="width:15%;" title="Shown in the WebHook UI when choosing a Payload">Template Description:</th><td style="width:35%;"><c:out value="${webhookTemplateBean.templateDescription}"/></td>
          <th style="width:15%;">Payload Format:</th><td style="width:35%;" colspan=3>${webhookTemplateBean.payloadFormat}</td>
        </tr>
        <tr>
          <th style="width:15%;" title="Used in the UI to show extra information about a Template">Tooltip Text:</th>
          <c:choose>
		  	<c:when test="${not empty webhookTemplateBean.toolTipText}">
	          <td style="width:85%;" colspan="5"><c:out value="${webhookTemplateBean.toolTipText}"/></td>
          	</c:when>
          	<c:otherwise>
	          <td style="width:85%;" colspan="5"><i>none</i></td>
          	</c:otherwise>
          </c:choose>          
        </tr>
      </table>

    </bs:refreshable>      
    <bs:dialog dialogId="editTemplateItemDialog"
               dialogClass="editTemplateItemDialog"
               title="Edit Build Event Template"
               closeCommand="WebHooksPlugin.TemplateEditBuildEventDialog.close()">
        <forms:multipartForm id="editTemplateItemForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.TemplateEditBuildEventDialog.doPost();">
		<div id="templateWrapper">
		
			<div id="templateEditor">
			<h2 id="templateHeading"></h2>
            <table class="templateDialogFormTable">
            	<tr><td>Build Events:</td>
            		<td class="buildAddedToQueue" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildAddedToQueue" name="BuildAddedToQueue" type=checkbox /> Build Added to Queue</label></td>
            		<td class="buildRemovedFromQueue" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildRemovedFromQueue" name="BuildRemovedFromQueue" type=checkbox /> Build Removed from Queue by User</label></td>
            	</tr>
            	<tr><td></td>
            		<td class="buildStarted" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildStarted" name="BuildStarted" type=checkbox /> Build Started</label></td>
            		<td class="changesLoaded" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="changesLoaded" name="ChangesLoaded" type=checkbox /> Changes Loaded</label></td>
            	</tr>
            	<tr><td></td>
            		<td class="buildInterrupted" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildInterrupted" name="BuildInterrupted" type=checkbox /> Build Interrupted</label></td>
            		<td class="beforeBuildFinish" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="beforeBuildFinish" name="BeforeBuildFinish" type=checkbox /> Build Almost Completed</label></td>
            	</tr>
            	<tr><td></td>
	            	<td class="responsibilityChanged" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="responsibilityChanged" name="ResponsibilityChanged" type=checkbox /> Responsibility Changed</label></td>
            		<td></td>
            	</tr>
            	<tr><td></td>
            		<td class="buildSuccessful" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildSuccessful" name="BuildSuccessful" type=checkbox /> Build Successful</label></td>
            		<td class="buildFixed" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildFixed" name="BuildFixed" type=checkbox /> Build Fixed</label></td>
            	</tr>
            	<tr><td></td>
            		<td class="buildFailed" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildFailed" name="BuildFailed" type=checkbox /> Build Failed</label></td>
            		<td class="buildBroken" style="padding-left:3px;"><label style='white-space:nowrap;'><input class="buildState" id="buildBroken" name="BuildBroken" type=checkbox /> Build Broken</label></td>
            	</tr>
                 <tr>
 					<td colspan="3">
 					<div id="tab-container" class="tab-container">
					  <ul class='etabs'>
						   <li class='tab' id="nonBranchPaneTab"><a href="#nonBranchPane" class="active">Non-Branch Template</a></li>
						   <li class='tab' id="branchPaneTab"><a href="#branchPane">Branch Template</a></li>
						   <li class='tab' id="branchOptionTab"><a href="#branchPane"></a><label style='white-space:nowrap;' class="useTemplateTextForBranch"><input id="useTemplateTextForBranch" name="UseTemplateTextForBranch" type=checkbox /> Use Non-branch template text for Branch template text</label></li>
					  </ul>
					  <div class='panel-container' style="padding:0px;">
						<div id='nonBranchPane'>
							<p>This template is used for builds for which TeamCity is <strong>not</strong> aware of branches.</p>
							<pre id="editor" class="editor"></pre>
						</div>
						<div id='branchPane'>
							<p>This template is used for builds for which TeamCity <strong>is</strong> aware of branches.</p>
							<pre id="editorBranch" class="editor"></pre>
						</div>
						<div id='branchOption'>
						</div>
					  </div>
					</div>                        
                    </td>
                 </tr>
            </table>
            </div>
            
			<div id="templateVariables"><h2>Available Variables</h2>
				Click on a variable to insert it into your template.
				<div class="templateVariablesOverflow">
					<ul id=templateVariableList></ul>
				</div>
			</div>              
            
          </div>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="editTemplateItem"/>
            <div id="ajaxTemplateItemEditResult"></div>
            <div class="popupSaveButtonsBlock">
            	<button id="editTemplateItemDialogPreview" class="btn btn_primary" onclick="WebHooksPlugin.TemplateEditBuildEventDialog.openPreviewDialog(); return false">Preview Template</button>
                <forms:submit id="editTemplateItemDialogSubmit" label="Save Template"/>
                <forms:cancel onclick="WebHooksPlugin.TemplateEditBuildEventDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
      
	<br>
 <div class="filterTableContainer">	
    <%@ include file="templateEditListBuildEventTemplates.jsp" %>
</div>

<script>
    // trigger extension
    var langTools = ace.require("ace/ext/language_tools");
    var editor = ace.edit("editor");
    editor.session.setMode("ace/mode/jsonvelocity");
    editor.setTheme("ace/theme/github");
    editor.$blockScrolling = Infinity;
    // enable autocompletion and snippets
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
        useWorker: false,
        showPrintMargin: false
    });
    
    var editorBranch = ace.edit("editorBranch");
    editorBranch.session.setMode("ace/mode/jsonvelocity");
    editorBranch.setTheme("ace/theme/github");
    editorBranch.$blockScrolling = Infinity;
    // enable autocompletion and snippets
    editorBranch.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
        useWorker: false,
        showPrintMargin: false
    });

    var wordList = ["buildName", "buildFullName", "buildTypeId",
    				"buildInternalTypeId", "buildExternalTypeId", "buildNumber", "buildId", 
    				"projectName", "projectId", "projectInternalId", "projectExternalId", 
    				"buildStatus", "buildResult", "buildResultPrevious", "buildResultDelta", 
    				"buildStateDescription", "buildStatusUrl", "notifyType",
					"buildComment", "buildRunners", "buildTags", "changes",  "comment", "triggeredBy",
					"buildStartTime", "currentTime",  
					"branchName", "branchDisplayName", "branchIsDefault", "branch",
					"text", "message",  "rootUrl", "buildStatusHtml", 
					"agentName", "agentHostname", "agentOs",  
					"responsibilityUserOld", "responsibilityUserNew"  
					];
    
    /* your custom completer */
    var customCompleter = {
          getCompletions: function(editor, session, pos, prefix, callback) {

	   	        callback(null, wordList.map(function(word) {
	   	            return {
	   	                caption: "\$\{" + word + "\}",
	   	                value: "\$\{" + word + "\}",
	   	                meta: "webhook variable"
	   	            };
	   	        }));
          }
     }
    langTools.addCompleter(customCompleter);
    
    // Iterate over the word list and add them to the right hand panel. 
    wordList.map(function(word) {
    	$j("#templateVariableList").append("<li title='Click to insert \$\{" + word + "\} into editor'>" + word + "</li>");
    });

    // Add click handler for each item above.
    $j("#templateVariableList").on("click","li", function(){
    	// Get the text from the li, and wrap it in dollar and curl braces
	    var text = '\$\{' + $j(this).text() + '\}';
	    var insertDone = false;
	    
	    // Determine which editor is active.
	    $j("ul.etabs li.active").each(function(){
	    		if ($j(this).attr('id') == 'nonBranchPaneTab') {
	    			editor.focus();
	    			editor.session.insert(editor.getCursorPosition(), text)
	    			insertDone = true;
	    		} else if ($j(this).attr('id') == 'branchPaneTab') {
	    			editorBranch.focus();
	    			editorBranch.session.insert(editorBranch.getCursorPosition(), text)
	    			insertDone = true;
	    		}
	    });
	    if (! insertDone) {
	    	alert("Sorry, insert failed. Could not determine branch or nonbranch editor");
	    }
     });
    
    
</script>

    <bs:dialog dialogId="previewTemplateItemDialog"
               dialogClass="previewTemplateItemDialog"
               title="Preview Build Event Template"
               closeCommand="WebHooksPlugin.PreviewTemplateItemDialog.close()">
        <forms:multipartForm id="previewTemplateItemForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.PreviewTemplateItemDialog.doPost();">

            <input type="hidden" id="templateId" name="templateId"/>
            <input type="hidden" id="templateNumber" name="templateNumber"/>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="previewTemplateItem"/>
            <table style="width:100%">
            	<tr><td colspan=2><strong>Preview Template Payload</strong></td></tr>
                <tr><td colspan=2>Select a build to use as example data for a webhook test execution:</td></tr> 
	            <tr><td>Project:</td><td> <forms:select id="previewTemplateItemDialogProjectSelect" name="previewTemplateItemDialogProjectSelect"/></td></tr>
            	<tr><td>Build:</td><td> <forms:select id="previewTemplateItemDialogBuildSelect" name="previewTemplateItemDialogBuildSelect"/><br></td></tr>
            	<tr><td>Build Event:</td><td> <forms:select id="previewTemplateItemDialogBuildStateSelect" name="previewTemplateItemDialogBuildStateSelect"/><br></td></tr>
	           	<tr><td colspan="2"><div>
           						<div id="currentTemplatePreview"></div>
           						</div>
           						
           						</td></tr>
				<tr><td colspan=2>
				<span class="testingLimitations"><a rel="noopener noreferrer" target="_blank" href="https://github.com/tcplugins/tcWebHooks/wiki/Testing-a-WebHook#limitations-with-webhook-testing">Testing limitations (opens in new window)</a></span>
				<strong>Execute Test Webhook</strong></td></tr>           						
            	<tr><td>WebHook:</td><td> <forms:select id="previewTemplateItemDialogWebHookSelect" name="previewTemplateItemDialogWebHookSelect"/><br></td></tr>
            	<tr><td>URL:</td><td> <input type=text length=1024 size=50 id="previewTempleteItemDialogUrl" name="previewTempleteItemDialogUrl" placeholder="Enter a URL, or choose a webhook above, or both (to override the URL)"><br></td></tr>
				<tr><td colspan=2>
				<div id="webhookTestProgress">
					<forms:progressRing progressTitle="Sending test webhook..."/>						            		
					<span class="stage-status__description">Sending test webhook...</span>
				</div>	
				<div id="previewTempleteItemDialogAjaxResult" /></td></tr>            	
           	</table>
           						
            <div class="popupSaveButtonsBlock">
                <forms:submit id="previewTemplateItemDialogSubmit" label="Send Test WebHook for Build Event"/>
                <forms:cancel onclick="WebHooksPlugin.PreviewTemplateItemDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    
    <bs:dialog dialogId="deleteTemplateItemDialog"
               dialogClass="deleteTemplateItemDialog"
               title="Confirm Build Event Template deletion"
               closeCommand="WebHooksPlugin.DeleteTemplateItemDialog.close()">
        <forms:multipartForm id="deleteTemplateItemForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.DeleteTemplateItemDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>Are you sure you want to delete this Build Event Template? There is no undo.
                        <div id="ajaxDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="templateId" name="templateId"/>
            <input type="hidden" id="templateNumber" name="templateNumber"/>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="deleteTemplateItem"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteTemplateItemDialogSubmit" label="Delete Build Event Template"/>
                <forms:cancel onclick="WebHooksPlugin.DeleteTemplateItemDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    
    <bs:dialog dialogId="deleteTemplateDialog"
               dialogClass="deleteTemplateDialog"
               title="Confirm Webhook Template deletion"
               closeCommand="WebHooksPlugin.DeleteTemplateDialog.close()">
        <forms:multipartForm id="deleteTemplateForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.DeleteTemplateDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteTemplateWarningMessage">This is a default warning. You should not be seeing this. 
                        <div id="ajaxTemplateDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="templateId" name="templateId" value="${webhookTemplateBean.templateId}"/>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="deleteTemplateItem"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteTemplateDialogSubmit" label="Delete Template"/>
                <forms:cancel onclick="WebHooksPlugin.DeleteTemplateDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    
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
    
    <bs:dialog dialogId="exportTemplateDialog"
               dialogClass="exportTemplateDialog"
               title="Export Template"
               closeCommand="WebHooksPlugin.ExportTemplateDialog.close()">
        <forms:multipartForm id="exportTemplateForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.ExportTemplateDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>Click the link below to export and download this template as JSON.<p>
                		
                		<a href="../../../app/rest/webhooks/templates/id:${webhookTemplateBean.templateId}/export?fields=$long,content">Download template...</a>
		       
                </td></tr>
            </table>
            <div class="popupSaveButtonsBlock">
                <forms:cancel onclick="WebHooksPlugin.ExportTemplateDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>

    <bs:dialog dialogId="editTemplateDialog"
               dialogClass="editTemplateDialog"
               title="Edit Webhook Template details"
               closeCommand="WebHooksPlugin.EditTemplateDialog.close()">
        <forms:multipartForm id="editTemplateForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.EditTemplateDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td colspan=2>
                	<div class="templateEdit">
                			<p><strong>Edit the fields below to modify template details</strong></p>
                			<p>It is not possible to modify a template ID because it is referenced by webhook configurations. 
                			Instead copy this template to a new ID by choosing &quot;Copy template...&quot; from the Actions menu on this page.</p>
                	</div>

                	<div class="templateCopy">
                			<p><strong>Fill in the fields below to copy this template.</strong></p>
                			<p>The most important thing is that the ID must be unique.</p>
                			<p>Once you have copied the template you will be redirected to the template view page
                			 where you can edit any build event template items. 
                	</div>
                	<div id="ajaxTemplateEditResult"></div>
                	
                </td></tr>
                <tr class="templateDetails">
                    <th>ID<l:star/></th>
                    <td>
                        <div><input type="text" id="template.id" name="template.id"/></div>
                    </td>
                </tr>
                <tr class="templateDetails">
                    <th>Description<l:star/></th>
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
                <tr class="templateDetails">
                    <th>Payload Format</th>
                    <td>
                        <div>
                        <select id="payloadFormat" name="template.payloadFormat">
                        	<option value="">Choose a Payload Format...</option> 
                        	<c:forEach items="${payloadFormats}" var="format">
                        		<option value="<c:out value="${format.formatShortName}" />"><c:out value="${format.formatDescription}" /></option>
                        	</c:forEach>
                        </select>
                        </div>
                    </td>
                </tr>
                
            </table>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="editTemplate"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editTemplateDialogSubmit" label="Save Template"/>
                <forms:cancel onclick="WebHooksPlugin.EditTemplateDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog> 
    
  </jsp:attribute>
</bs:page>