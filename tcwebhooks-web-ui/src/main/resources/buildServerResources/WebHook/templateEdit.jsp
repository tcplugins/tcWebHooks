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

<c:set var="pageTitle" value="Edit WebHook Payload Template" scope="request"/>

<bs:page>
  <jsp:attribute name="head_include">
    <bs:linkCSS>
      /css/admin/adminMain.css
      ${teamcityPluginResourcesPath}WebHook/css/styles.css
    </bs:linkCSS>
    <bs:linkScript>
      ${teamcityPluginResourcesPath}WebHook/js/editWebhookTemplate.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Webhook Payload Templates", url: '<c:url value="/webhooks/templates.html"/>'},
        {title: '<c:out value="${webhookTemplateBean.templateName}"/>', selected: true}
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
			      <a href="#" title="Disable Template" onclick="WebHooksPlugin.disableTemplate('${webhookTemplateBean.templateId}'); return false">Disable template...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Delete Template" onclick="WebHooksPlugin.deleteTemplate('${webhookTemplateBean.templateId}'); return false">Delete template...</a>
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
		var jQueryWebhook = jQuery.noConflict();
		var webhookDialogWidth = -1;
		var webhookDialogHeight = -1;
		var templatePaneOuterHeight = -1;
		jQueryWebhook(document).ready( function() {
				jQueryWebhook('#tab-container').easytabs({
					  animate: false,
					  updateHash: false
				});
				jQueryWebhook('#tab-container').bind('easytabs:after', function() {
					// one should call editor.resize() after changing size or visibility of the div 
					editor.resize();
					editorBranch.resize();
				});
		});
		</script>		
    <bs:refreshable containerId="repoRepoInfoContainer" pageUrl="${pageUrl}">

	<bs:messages key="repoInfoUpdateResult"/>
	
      <table class="settings parameterTable" id="webhookTemplateHeader">
        
        <tr>
          <th style="width:15%;">Template Id:</th><td style="width:35%;">${webhookTemplateBean.templateId}</td>
          <th style="width:10%;">Rank:</th><td style="width:10%; border:none;">${webhookTemplateBean.rank}</td>
          <c:choose>
		  	<c:when test="${not empty webhookTemplateBean.dateFormat}">
          	<th style="width:15%;">Date Format:</th><td style="border:none;">${webhookTemplateBean.dateFormat}</td>
          	</c:when>
          	<c:otherwise>
          	<th style="width:15%;">Date Format:</th><td style="border:none;"><i>none</i></td>
          	</c:otherwise>
          </c:choose>
        </tr>
        <tr>
          <th style="width:15%;">Template Name:</th><td style="width:35%;">${webhookTemplateBean.templateName}</td>
          <th style="width:15%;">Payload Format:</th><td style="width:35%;" colspan=3>webhookTemplateBean.payloadFormat</td>
        </tr>
        <tr>
          <th style="width:15%;">Tooltip Text:</th>
          <c:choose>
		  	<c:when test="${not empty webhookTemplateBean.toolTipText}">
	          <td style="width:85%;" colspan="5">${webhookTemplateBean.toolTipText}</td>
          	</c:when>
          	<c:otherwise>
	          <td style="width:85%;" colspan="5"><i>none</i></td>
          	</c:otherwise>
          </c:choose>          
        </tr>
      </table>
      
          <bs:dialog dialogId="editTemplateDialog"
               dialogClass="editTemplateDialog"
               title="Edit Build Event Template"
               closeCommand="WebHooksPlugin.TemplateEditBuildEventDialog.close()">
        <forms:multipartForm id="editTemplateForm"
                             action="/admin/manageWebhookTemplate.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.TemplateEditBuildEventDialog.doPost();">
			<div id="ajaxRepoEditResult"></div>
			<div id="templateVariables"><h2>Available Variables</h2>
				Click on a variable to insert it into your template.
			</div>
			<div id="templateEditor">
			<h2 id="templateHeading"></h2>
            <table class="templateDialogFormTable">
            	<tr><td>Build Events:</td>
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
					  </ul>
					  <div class='panel-container'>
						<div id='nonBranchPane'>
							&nbsp;
							<pre id="editor" class="editor"></pre>
						</div>
						<div id='branchPane'>
							<label style='white-space:nowrap;' class="useTemplateTextForBranch"><input id="useTemplateTextForBranch" name="UseTemplateTextForBranch" type=checkbox /> Use Non-branch template text for Branch template text</label>
							<pre id="editorBranch" class="editor"></pre>
						</div>
					  </div>
					</div>                        
                    </td>
                 </tr>
            </table>
            </div>
            <input type="hidden" name="action" id="WebHookTemplateAction" value="editTemplate"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editTemplateDialogSubmit" label="Save Template"/>
                <forms:cancel onclick="WebHooksPlugin.TemplateEditBuildEventDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
      
    </bs:refreshable>

	<br>
 <div class="filterTableContainer">	
    <%@ include file="templateEditListBuildEventTemplates.jsp" %>
</div>

<script>
    // trigger extension
    var langTools = ace.require("ace/ext/language_tools");
    var editor = ace.edit("editor");
    editor.session.setMode("ace/mode/json");
    editor.setTheme("ace/theme/xcode");
    // enable autocompletion and snippets
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true
    });
    
    var editorBranch = ace.edit("editorBranch");
    editorBranch.session.setMode("ace/mode/json");
    editorBranch.setTheme("ace/theme/xcode");
    // enable autocompletion and snippets
    editorBranch.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true
    });
    
    /* your custom completer */
    var customCompleter = {
          getCompletions: function(editor, session, pos, prefix, callback) {
	   	        var wordList = ["buildFullName", "buildComment", "changes", "agentOs", 
	   	        				"buildNumber", "branchIsDefault", "buildResultPrevious", "buildStatus", 
	   	        				"projectInternalId", "buildStatusUrl", "buildTypeId", "responsibilityUserNew", 
	   	        				"buildRunners", "buildStartTime", "buildTags", "responsibilityUserOld", 
	   	        				"buildFinishTime", "buildStateDescription", "text", "buildName", 
	   	        				"buildResult", "agentName", "branchName", "buildResultDelta", "buildId", 
	   	        				"message", "buildExternalTypeId", "rootUrl", "currentTime", "notifyType", 
	   	        				"buildInternalTypeId", "comment", "projectExternalId", "branchDisplayName", 
	   	        				"projectName", "projectId", "agentHostname", "buildStatusHtml", "triggeredBy", ];
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
</script>

    <bs:dialog dialogId="repoEditFilterDialog"
               dialogClass="repoEditFilterDialog"
               title="Edit Artifact Filter"
               closeCommand="DebRepoFilterPlugin.RepoEditFilterDialog.close()">
        <forms:multipartForm id="repoEditFilterForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoFilterPlugin.RepoEditFilterDialog.doPost();">

            <table class="runnerFormTable">
                <tr>
                    <th>Build Type<l:star/></th>
                    <td>
                        <div>
                        	<select id="debrepofilter.buildtypeid" name="debrepofilter.buildtypeid">
                        	<c:forEach items="${sortedProjectBuildTypes}" var="buildType">
                        		<option value="${buildType.buildTypeId}">${buildType.fullName}</option>
                        	</c:forEach>
                        	</select>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>regex<l:star/></th>
                    <td>
                        <div><input type="text" id="debrepofilter.regex" name="debrepofilter.regex"/></div>
                    </td>
                </tr>
                <tr>
                    <th>dist<l:star/></th>
                    <td>
                        <div><input type="text" id="debrepofilter.dist" name="debrepofilter.dist"/></div>
                    </td>
                </tr>
                <tr>
                    <th>component<l:star/></th>
                    <td>
                        <div><input type="text" id="debrepofilter.component" name="debrepofilter.component"/></div>
                        <div id="ajaxResult"></div>
                    </td>
                </tr>
            </table>
            <input type="hidden" id="debrepofilter.id" name="debrepofilter.id"/>
            <input type="hidden" id="debrepo.uuid" name="debrepo.uuid" value="${repoConfig.uuid}"/>
            <input type="hidden" name="action" id="DebRepoaction" value="editFilter"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="repoEditFilterDialogSubmit" label="Save"/>
                <forms:cancel onclick="DebRepoFilterPlugin.RepoEditFilterDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>

    <bs:dialog dialogId="repoDeleteFilterDialog"
               dialogClass="repoDeleteFilterDialog"
               title="Confirm Artifact Filter deletion"
               closeCommand="DebRepoFilterPlugin.RepoDeleteFilterDialog.close()">
        <forms:multipartForm id="repoDeleteFilterForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoFilterPlugin.RepoDeleteFilterDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>Deleting an Artifact Filter does not remove previously added artifacts from the repository.
                        <div id="ajaxDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="debrepofilter.id" name="debrepofilter.id"/>
            <input type="hidden" id="debrepo.uuid" name="debrepo.uuid" value="${repoConfig.uuid}"/>
            <input type="hidden" id="debrepofilter.buildtypeid" name="debrepofilter.buildtypeid"/>
            <input type="hidden" name="action" id="DebRepoaction" value="deleteFilter"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="repoDeleteFilterDialogSubmit" label="Delete Filter"/>
                <forms:cancel onclick="DebRepoFilterPlugin.RepoDeleteFilterDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    
    <bs:dialog dialogId="deleteRepoDialog"
               dialogClass="deleteRepoDialog"
               title="Confirm Debian Repository deletion"
               closeCommand="DebRepoPlugin.DeleteRepoDialog.close()">
        <forms:multipartForm id="deleteRepoForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoPlugin.DeleteRepoDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>Deleting a Debian Repository removes all repository configuration and  artifacts from the repository listing.<br>
                		It does not delete the build artifacts from disk.
                        <div id="ajaxRepoDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="debrepo.uuid" name="debrepo.uuid" value="${repoConfig.uuid}"/>
            <input type="hidden" name="action" id="DebRepoaction" value="deleteRepo"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteRepoDialogSubmit" label="Delete Repository"/>
                <forms:cancel onclick="DebRepoPlugin.DeleteRepoDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    
  </jsp:attribute>
</bs:page>