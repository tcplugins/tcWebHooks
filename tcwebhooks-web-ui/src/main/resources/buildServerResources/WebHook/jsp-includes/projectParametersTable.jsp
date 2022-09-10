<h2 class="webhookHeading" id="parameters">WebHook Parameters available to WebHooks in the <i><c:out value="${project.fullName}"/></i> project</h2>
<bs:refreshable containerId="projectWebhookParametersContainer" pageUrl="${pageUrl}">

<c:if test="${fn:length(projectWebhookParameters) == 0}" >
    <p>There are no WebHook Parameters configured for this project.</p>
    <c:if test="${showLinksToOldEditPage}">
        <a href="../webhooks/index.html?projectId=${projectExternalId}#parameters">Add project Parameters</a>.
    </c:if>
</c:if>
<c:if test="${fn:length(projectWebhookParameters) > 0}" >
    <p>There are <strong>${fn:length(projectWebhookParameters)}</strong> Parameters available to <c:out value="${project.fullName}"/> and sub-projects.
        <c:if test="${showLinksToOldEditPage}">
            <a href="../webhooks/index.html?projectId=${projectExternalId}#parameters">Edit project Parameters</a>.</p>
        </c:if>
</c:if>
    <table class="settings parametersTable webhooktable">
    <c:if test="${fn:length(projectWebhookParameters) > 0}" >
        <thead>
            <tr>
                <th style="font-weight: bold; width:20%">Parameter Name</th>
                <th style="font-weight: bold; width:40%">Parameter Value</th>
                <th style="font-weight: bold; width:15%" title="Legacy Parameters are shown in Legacy Payloads as well as being available to templates">Legacy Parameter</th>
                <th style="font-weight: bold; width:20%" colspan="3">Forced Resolve</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${projectWebhookParameters}" var="myParam">
                <tr id="viewRow_${myParam.parameter.id}" class="highlight webHookRow">
                    <td onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});"><c:out value="${myParam.parameter.name}" /></td>
                    <c:choose>
                        <c:when test="${myParam.parameter.secure}">
                            <td>*****</td>
                        </c:when>
                        <c:otherwise>
                            <td onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});"><c:out value="${myParam.parameter.value}"/></td>
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${myParam.parameter.includedInLegacyPayloads}">
                            <td title="This parameter will appear in Legacy Payloads as well as being available to templates" onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});">Yes</td>
                        </c:when>
                        <c:otherwise>
                            <td title="This parameter will be hidden from Legacy Payloads, but will be available to templates." onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});">No</td>
                        </c:otherwise>							
                    </c:choose>
                    <c:choose>
                        <c:when test="${myParam.parameter.forceResolveTeamCityVariable}">
                            <td style="width:8%" onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});">Yes</td>
                        </c:when>
                        <c:otherwise>
                            <td style="width:8%" onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});">No</td>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${showEditButton}">
                        <td onclick="${parameterConfigDialogScope}.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}','enableSecure': true});"><a href="javascript://">edit</a></td>
                        <td onclick="${parameterConfigDialogScope}.deleteParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}', 'parameterName': '<c:out value="${myParam.parameter.name}" />'});"><a href="javascript://">delete</a></td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
    <c:if test="${showEditButton}">
        <tfoot>
            <tr class="newWebHookRow">
                <td colspan="6" class="highlight newWebHookRow"><p onclick="${parameterConfigDialogScope}.addParameter({'projectId':'${projectExternalId}','enableSecure': true}, '#hookPane');" class="addNew">Click to create new WebHook Parameter for this project</p></td>
            </tr>
        </tfoot>
    </c:if>
    </table>
</bs:refreshable>
