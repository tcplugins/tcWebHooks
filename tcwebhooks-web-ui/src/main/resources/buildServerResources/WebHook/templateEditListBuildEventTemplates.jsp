<%@ include file="/include.jsp" %>


<bs:refreshable containerId="buildEventTemplatesContainer" pageUrl="${pageUrl}">
  
	  <table class="settings templateTable">
        <tr class="templateHeading"><td colspan=6 class="templateTableTemplateTitle">Default Template (optional)</td></tr>
        <c:set var="defaultTemplateEventNum" value="${fn:length(webhookTemplateBean.defaultTemplateStates)}"/>
      	<c:choose>
		  <c:when test="${empty webhookTemplateBean.defaultTemplateItem}">
	      	<tr><td colspan="4">
	      		There is no default template defined. This template will only support build events for which there is a Build Event Template defined below.
	      	</td>
      			<td class="buildTemplateAction"><a id="addDefaultTempalte" href="#" onclick="DebRepoFilterPlugin.editFilter({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">create</a></td>
	      	</tr>     
		  </c:when>
		  <c:when test="${defaultTemplateEventNum == 0}">
	      	<tr><td>
	      		The default template is defined, but no build events will use it. All events are associated with a Build Event Template defined below.
	      	</td>
		      			<td class="buildTemplateAction"><a class="viewBuildEventTemplate" href="#" onclick="WebHooksData.getWebHookTemplateData({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">view</a></td>
		      			<td class="buildTemplateAction"><a class="editBuildEventTemplate" href="#" onclick="WebHooksPlugin.editBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'defaultTemplate', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">edit</a></td>
		      			<td class="buildTemplateAction"><a class="copyBuildEventTemplate" href="#" onclick="WebHooksPlugin.copyBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '_copy', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'defaultTemplate', dist:'${filter.dist}', component:'${filter.component}' }); return false">copy</a></td>
		      			<td class="buildTemplateAction"><a class="deleteBuildEventTemplate" href="#" onclick="WebHooksPlugin.deleteTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}' }); return false">delete</a></td>
	      	</tr>     
		  </c:when>
		  <c:otherwise>
	      	<tr><td><ul class="templateBuildStates">
		      	<c:forEach items="${webhookTemplateBean.defaultTemplateStates}" var="buildState">
		      		<li>${buildState.shortDescription}</li>
		      	</c:forEach>
		      	</ul></td>
		      			<td class="buildTemplateAction"><a class="viewBuildEventTemplate" href="#" onclick="WebHooksData.getWebHookTemplateData({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">view</a></td>
		      			<td class="buildTemplateAction"><a class="editBuildEventTemplate" href="#" onclick="WebHooksPlugin.editBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'defaultTemplate', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">edit</a></td>
		      			<td class="buildTemplateAction"><a class="copyDBuildEventTemplate" href="#" onclick="WebHooksPlugin.copyBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '_copy', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'defaultTemplate', dist:'${filter.dist}', component:'${filter.component}' }); return false">copy</a></td>
		      			<td class="buildTemplateAction"><a class="deleteBuildEventTemplate" href="#" onclick="WebHooksPlugin.deleteTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}' }); return false">delete</a></td>
	      	</tr>   		  
	      </c:otherwise>
		</c:choose>
      	<tr class="blankline"><td colspan=6>
 
	  <p>The default template is used for all build events, unless a more specific Build Event Template is specified below.</p> 
	  <p>If no Default Template is defined above, then only the build events with a Build Event Template are selectable in the WebHook UI.</p> 
	  </div>   
     
      <bs:messages key="defaultTemplateUpdateResult"/>
      
      <bs:messages key="buildEventTemplateUpdateResult"/>
      
      </td></tr>
      <tr class="templateHeading"><td colspan=6 class="templateTableTemplateTitle">Build Event Templates</td></tr>
      <c:set var="buildEventTemplateNum" value="${fn:length(webhookTemplateBean.buildEventTemplates)}"/>
      <c:forEach items="${webhookTemplateBean.buildEventTemplates}" var="buildEventTemplate">
      	<tr><td><ul class="templateBuildStates">
	      	<c:forEach items="${buildEventTemplate.buildStates}" var="buildState">
	      		<li>${buildState.shortDescription}</li>
	      	</c:forEach>
	      	</ul></td>
	      			<td class="buildTemplateAction"><a class="viewBuildEventTemplate" href="#" onclick="WebHooksData.getWebHookTemplateData('${webhookTemplateBean.templateId}', '${buildEventTemplate.webHookTemplateItem.id}'); return false">view</a></td>
	      			<td class="buildTemplateAction"><a class="editBuildEventTemplate" href="#" onclick="WebHooksPlugin.editBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'id:${buildEventTemplate.webHookTemplateItem.id}', regex: '${filter.regex}', dist:'${filter.dist}', component:'${filter.component}' }); return false">edit</a></td>
	      			<td class="buildTemplateAction"><a class="copyBuildEventTemplate" href="#" onclick="WebHooksPlugin.copyBuildEventTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '_copy', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'id:${buildEventTemplate.webHookTemplateItem.id}', dist:'${filter.dist}', component:'${filter.component}' }); return false">copy</a></td>
	      			<td class="buildTemplateAction"><a class="deleteDBuildEventTemplate" href="#" onclick="WebHooksPlugin.deleteTemplate({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}' }); return false">delete</a></td>
      	</tr>     
      </c:forEach>
      	<tr><td colspan=6><a href="#" onclick="WebHooksPlugin.addBuildEventTemplate({ uuid: '${repoConfig.uuid}', name: '${debRepoBean.name}', id: '_new', templateName: '${webhookTemplateBean.templateId}', templateNumber: 'defaultTemplate' }); return false">Add Build Event Template</a></td></tr>
      	<tr class="blankline"><td colspan=5>&nbsp;</td></tr>
      
      </table>
</bs:refreshable>
