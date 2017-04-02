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
<c:set var="projectExternalId" value="${webhookTemplateBean.project.externalId}" scope="request"/>

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
        {title: '<c:out value="${webhookTemplateBean.name}"/>', selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="quickLinks_include">
    <div class="toolbarItem">
	    <c:set var="menuItems">
		    <authz:authorize allPermissions="EDIT_PROJECT" projectId="${webhookTemplateBean.project.projectId}">
		      <jsp:body>
		        <l:li>
			      <a href="#" title="Edit Respository Name and Project affilliation" onclick="return DebRepoPlugin.editDebRepo('${repoConfig.uuid}'); return false">Edit repository...</a>
		        </l:li>
		        <l:li>
			      <a href="#" title="Delete Repository Configuration and Index" onclick="DebRepoPlugin.removeDebRepo('${repoConfig.uuid}'); return false">Delete repository...</a>
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
    <bs:refreshable containerId="repoRepoInfoContainer" pageUrl="${pageUrl}">
  	  <h2>Debian Repository : ${repoConfig.repoName}</h2>

	<bs:messages key="repoInfoUpdateResult"/>
	
      <table class="settings parameterTable" id="webhookTemplateHeader">
        
        <tr>
          <th style="width:15%;">Template Id:</th><td style="width:35%;">${webhookTemplateBean.name}</td>
          <th style="width:15%;">Builds Types:</th><td style="width:35%; border:none;">${fn:length(repoConfig.buildTypes)}</td>
        </tr>
        <tr>
          <th style="width:15%;">Project:</th> <td style="width:35%;"><c:out value="${webhookTemplateBean.project.fullName}"/></td>
          <th style="width:15%;">Artifact Filters:</th><td style="width:35%; border:none;">${repoStats.totalFilterCount}</td>
        </tr>
        <tr>
          <th style="width:15%;">URL:</th><td style="width:35%;"><a href="<c:out value="${repoStats.repositoryUrl}"/>"><c:out value="${repoStats.repositoryUrl}"/></a></td>
          <th style="width:15%;">Package Listings:</th><td style="width:35%;">${repoStats.totalPackageCount}</td>
        </tr>
        <tr>
          <th style="width:15%;">&quot;All&quot; Architectures:</th>
          <td style="width:85%;" colspan="3">${webhookTemplateBean.allArchsAsCSL}</td>
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
                    	<div><input type="text" id="debrepo.name" name="debrepo.name" value="${webhookTemplateBean.name}"/></div>
                    	The Repository name forms part of the URL used to access it. Renaming a repository will change its URL and all Debian servers which use this repository will need their /etc/apt/sources.list file updated.<br>
                        Names MUST be unique across a TeamCity instance and must only contain A-Za-z0-9_- characters.
                        
                    </td>
                 </tr>
                 <tr>    
                 	<th>Project<l:star/></th>                
                    <td>
                        <div>
                        	<select id="debrepo.project.id" name="debrepo.project.id">
                        	<c:forEach items="${sortedProjects}" var="project">
                        	            <c:choose>
                							<c:when test="${project.projectId == webhookTemplateBean.project.projectId}">
                        						<option selected value="${project.projectId}"><c:out value="${project.fullName}"/></option>
                        					</c:when>
                        					<c:otherwise>
                        						<option value="${project.projectId}"><c:out value="${project.fullName}"/></option>
                        					</c:otherwise>
                        				</c:choose>
                        	</c:forEach>
                        	</select>
                        </div>
                        The project this repository belongs to. Users with the Project Administrator Role for this project can edit this repository configuration. 
                        When adding/editing Artifact Filters, only builds from this project or sub-projects are available to choose from.</p>
                    </td>
                </tr>
                 <tr>    
                 	<th>&quot;All&quot; Architectures</th>                
                    <td>
                        <div>
                        	<ul class="editArchitectures">
                        	<c:forEach items="${webhookTemplateBean.allArchitectureList}" var="arch">
                        		<li>
                        			<label><input class="architectureCheckbox" type="checkbox" name="debrepo.arch.${arch.arch}" value="${arch.arch}" <c:if test="${arch.enabled}">checked</c:if>>&nbsp;${arch.arch}</label>
                        		</li>
                        	</c:forEach>
                        	</ul>
                        </div>
                        When a package's meta-data has an <em>Architecture</em> value of &quot;all&quot;, it will be 
                        indexed into all of the selected architectures above. A package's Architecture is defined 
                        inside the DEBIAN/control file inside the package.  
                    </td>
                </tr>
            </table>
            <input type="hidden" id="debrepo.uuid" name="debrepo.uuid" value="${repoConfig.uuid}"/>
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
    <%@ include file="editDebianRepositoryViewDetails.jsp" %>
</div>

<div class="filterDocsContainer">
	<h2>Artifact Filter Configuration</h2>
	<div class="filterDocsItem">
		<h3>Artifact Filename Match (regex)</h3>
		<p>This is the regular expression used to find matching artifacts to publish. 
		   The filter will be run against the list of artifacts copied to the TeamCity server when a build completes.</p>
		<p>If the filter matches, the artifact will be indexed by the Debian Repository using the <code>dist</code>, 
			<code>component</code> and meta-data extracted from the package file (name, version, architecture)</p>
		<dl class="regexExample"><dt>Example regex: <code>build/package-name-.+\.deb$</code></dt>
		   <dd>This will match a file in the build directory with a name beginning with <i>package-name</i> and ending with <i>.deb</i>.</dd>
		</dl>
	</div>
	
	<div class="filterDocsItem">
		<h3>Distribution (dist)</h3>
		<p>This is the <i>dist</i> value under which the matching package will be indexed. This represents a Debian (or Ubuntu and variants) 
		     distribution. Typical values for <i>dist</i> are the distribution name: eg, <code>jessie</code>, <code>wheezy</code>, <code>squeeze</code> for Debian, or <code>xenial</code>, <code>yakkety</code>, <code>zesty</code> for Ubuntu. Alternatively, a symbolic name can be used: eg, <code>devel</code></p>
		<p>This can be any name you choose. It forms part of the configuration line added to a machine's <code>/etc/apt/sources.list</code></p>
		<dl class="regexExample"><dt>Example dist: <code>jessie</code></dt>
		   <dd>If the filename matches the regex (above), the package will be indexed into the <i>jessie</i> distribution's package list.</dd>
		</dl>
	</div>
	
	<div class="filterDocsItem">
		<h3>Component</h3>
		<p>This is the <i>component</i> value under which the matching package will be indexed. This represents a Debian (or Ubuntu and variants) 
		     component name. Typical values for <i>component</i> are: main, stable, unstable, testing, experimental</p>
		<p>This can be any name you choose. It forms part of the configuration line added to a machine's <code>/etc/apt/sources.list</code></p>
		<dl class="regexExample"><dt>Example component: <code>main</code></dt>
		   <dd>If the filename matches the regex (above), the package will be indexed into the <i>main</i> component list.</dd>
		</dl>
	</div>
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
            <input type="hidden" name="projectId" id="projectId" value="${webhookTemplateBean.project.projectId}"/>
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