<h3>WebHook Parameters available to WebHooks in this Project</h3>
<c:if test="${fn:length(projectWebhookParameters) == 0}" >
    <p>There are no WebHook Parameters configured for this project.</p>
    <a href="../webhooks/index.html?projectId=${projectExternalId}#parameters">Add project Parameters</a>.
</c:if>
<c:if test="${fn:length(projectWebhookParameters) > 0}" >
    <p>There are <strong>${fn:length(projectWebhookParameters)}</strong> Parameters available to <c:out value="${project.fullName}"/> and sub-projects.
        <a href="../webhooks/index.html?projectId=${projectExternalId}#parameters">Edit project Parameters</a>.</p>
    <table class="highlightable parametersTable webhooktable">
        <thead>
            <tr>
                <th class=name style="font-weight: bold; width:20%">Parameter Name</th>
                <th style="font-weight: bold; width:40%">Parameter Value</th>
                <th style="font-weight: bold; width:15%" title="Legacy Parameters are shown in Legacy Payloads as well as being available to templates">Legacy Parameter</th>
                <th style="font-weight: bold; width:20%" colspan="3">Forced Resolve</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach items="${projectWebhookParameters}" var="myParam">
            <tr id="viewRow_${myParam.parameter.id}" class="highlight webHookRow">
                <td onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});"><c:out value="${myParam.parameter.name}" /></td>
                <c:choose>
                    <c:when test="${myParam.parameter.secure}">
                        <td>*****</td>
                    </c:when>
                    <c:otherwise>
                        <td onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});"><c:out value="${myParam.parameter.value}"/></td>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${myParam.parameter.includedInLegacyPayloads}">
                        <td title="This parameter will appear in Legacy Payloads as well as being available to templates" onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});">Yes</td>
                    </c:when>
                    <c:otherwise>
                        <td title="This parameter will be hidden from Legacy Payloads, but will be available to templates." onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});">No</td>
                    </c:otherwise>							
                </c:choose>
                <c:choose>
                    <c:when test="${myParam.parameter.forceResolveTeamCityVariable}">
                        <td style="width:8%" onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});">Yes</td>
                    </c:when>
                    <c:otherwise>
                        <td style="width:8%" onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});">No</td>
                    </c:otherwise>
                </c:choose>
                <td onclick="WebHooksPlugin.Parameters.editParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}'});"><a href="javascript://">edit</a></td>
                <td onclick="WebHooksPlugin.Parameters.deleteParameter({'parameterId':'${myParam.parameter.id}','projectId':'${myParam.sproject.externalId}', 'parameterName': '<c:out value="${myParam.parameter.name}" />'});"><a href="javascript://">delete</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>