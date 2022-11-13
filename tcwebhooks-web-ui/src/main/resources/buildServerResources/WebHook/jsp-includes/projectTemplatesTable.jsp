<c:choose>
    <c:when test="${projectExternalId == '_Root'}">
        <h2 class="webhookHeading">WebHook Templates available for every TeamCity build (_Root project)</h2>
    </c:when>
    <c:otherwise>
        <h2 class="webhookHeading">WebHook Templates available for the <i><c:out value="${project.fullName}"/></i> project and sub-projects</h2>
    </c:otherwise>
</c:choose>

<c:if test="${fn:length(webHookTemplates) == 0}" >
        <p>There are no Templates associated with this project and <strong>${parentTemplateCount}</strong> templates inherited from parent projects.</p>
        To associate a template with this project, 
        <a href="../webhooks/templates.html">View WebHook Templates</a> and either select this project
        when creating a new	template, or edit an existing template and change the associated project.
</c:if>
<c:if test="${fn:length(webHookTemplates) > 0}" >
        <p>There are <a href="../webhooks/templates.html?projectId=${projectExternalId}" title="Click to view a list of templates associated with this project"><strong>${fn:length(webHookTemplates)}</strong> templates associated with this project</a> 
            and <strong>${parentTemplateCount}</strong> templates inherited from parent projects.</p>
        <table id="webHookTemplateTable" class="settings parametersTable webhooktable">
            <thead>
            <tr style="background-color: rgb(245, 245, 245);">
            <th style="font-weight: bold;">Description</th>
            <th style="font-weight: bold;">Payload Format</th>
            <th style="font-weight: bold;">Supported Build Events</th>
            <th style="font-weight: bold;">Type</th>
            <th style="font-weight: bold; width:20%;" colspan="3">Usage</th>
            </tr>
            </thead>
            <tbody>
                <c:forEach items="${webHookTemplates}" var="template">
                  <tr id="viewRow_${template.templateId}" class="webHookTemplate">
                    <td class="nowrap heading" title="<c:out value="${template.templateToolTip}"/> (id: <c:out value="${template.templateId}"/>)"><c:out value="${template.templateDescription}" /></td>
                    <td>${template.formatDescription}</td>
                    <td>
                        <ul class="commalist">
                        <c:forEach items="${template.supportedBuildEnumStates}" var="state">	
                            <li>${state.shortDescription}</li>
                        </c:forEach>
                        </ul>
                    </td>
                    <td>${template.templateState.description}</td>
                    
                    <td><a href="../webhooks/search.html?templateId=${template.templateId}">${template.webhookUsageCount}&nbsp;webhook(s)</a></td>
    
            <c:choose>  
                <c:when test="${template.templateDescription == 'Legacy Webhook'}"> 		
                    <td>No template available</td>
                </c:when>
                <c:when test="${template.templateDescription == 'Webhook Statistics'}"> 		
                    <td>Template not editable</td>
                </c:when>
                <c:otherwise>  		
                    <td><a href="../webhooks/template.html?template=${template.templateId}">View</a></td>
                </c:otherwise>  
            </c:choose>
            
                  </tr>	
                </c:forEach>
            </tbody>
        </table>
</c:if>		
