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
      ${teamcityPluginResourcesPath}WebHook/editWebhookTemplate.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Webhook Payload Templates", url: '<c:url value="/webhooks/templates.html"/>'},
        {title: '<c:out value="${webhookTemplateBean.templateName}"/>', selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="body_include">
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
      
          <bs:dialog dialogId="editRepoDialog"
               dialogClass="editRepoDialog"
               title="Edit Debian Repository"
               closeCommand="DebRepoPlugin.EditRepoDialog.close()">
        <forms:multipartForm id="editRepoForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoPlugin.EditRepoDialog.doPost();">
			<div id="ajaxRepoEditResult"></div>
            <table class="runnerFormTable">
                 <tr>
                    <th>Repository Name<l:star/></th>
                    <td>
                    	<div><input type="text" id="webhookTemplate.templateId" name="webhookTemplate.templateId" value="${webhookTemplateBean.templateId}"/></div>
                    	The Repository name forms part of the URL used to access it. Renaming a repository will change its URL and all Debian servers which use this repository will need their /etc/apt/sources.list file updated.<br>
                        Names MUST be unique across a TeamCity instance and must only contain A-Za-z0-9_- characters.
                        
                    </td>
                 </tr>
            </table>
            <input type="hidden" name="action" id="DebRepoaction" value="editRepo"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editRepoDialogSubmit" label="Edit Repository"/>
                <forms:cancel onclick="DebRepoPlugin.EditRepoDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
      
    </bs:refreshable>

	<br>
 <div class="filterTableContainer">	
    <%@ include file="templateEditListBuildEventTemplates.jsp" %>
</div>

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