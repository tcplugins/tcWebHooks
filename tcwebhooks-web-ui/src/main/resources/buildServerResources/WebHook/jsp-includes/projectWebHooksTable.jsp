<bs:refreshable containerId="projectWebhooksContainer" pageUrl="${pageUrl}">
    <c:if test="${not projectBean.webHooksEnabledForProject}" >
        <div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
    </c:if>
    <p>There are <strong>${fn:length(projectBean.webHookList)}</strong> WebHooks configured for all builds in this project.
        <c:if test="${showEditLinks}">
            <a href="../webhooks/index.html?projectId=${projectExternalId}">Edit project WebHooks</a>.</p>
        </c:if>
    <table id="webHookTable" class="parametersTable settings webhooktable">
        <thead>
            <tr>
                <th class=name style="font-weight: bold; width:40%">URL</th>
                <th style="font-weight: bold; width:20%">Format</th>
                <th style="font-weight: bold; width:20%">Build Events</th>
                <th style="font-weight: bold; width:20%" colspan=3>Enabled Builds</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach items="${projectBean.webHookList}" var="hook">
            <tr id="viewRow_${hook.uniqueKey}" class="webHookRow">
                <td><c:out value="${hook.url}" /></td>
                <c:choose>
                    <c:when test="${hook.payloadTemplate == 'none'}">
                        <td><c:out value="${hook.payloadFormatForWeb}"/></td>
                    </c:when>
                    <c:otherwise>
                        <td><a title='<c:out value="${hook.templateToolTip}"/>' href="../webhooks/template.html?template=<c:out value="${hook.payloadTemplate}"/>"><c:out value="${hook.payloadFormatForWeb}"/></a></td>
                    </c:otherwise>
                </c:choose>
                <td><c:out value="${hook.enabledEventsListForWeb}"/></td>
                <td title="${hook.buildTypeCountAsToolTip}"><c:out value="${hook.buildTypeCountAsFriendlyString}"/></td>
                <td><a onclick="${webhookConfigDialogScope}.showEditDialog({'webhookId':'${hook.uniqueKey}','projectId':'${projectExternalId}'}, '#hookPane');" href="javascript://">edit</a></td>
                <td><a onclick="${webhookConfigDialogScope}.showDeleteDialog({'webhookId':'${hook.uniqueKey}','projectId':'${projectExternalId}'});" href="javascript://">delete</a></td>
            </tr>
        </c:forEach>
        </tbody>
        <tfoot>
            <tr class="newWebHookRow">
                <td colspan="6" class="highlight newWebHookRow"><p onclick="${webhookConfigDialogScope}.showAddDialog({'projectId':'${projectExternalId}'}, '#hookPane');" class="addNew">Click to create new WebHook for this project</p></td>
            </tr>
        </tfoot>
    </table>
    <script>WebHooksPlugin.afterRefresh();</script>
</bs:refreshable>