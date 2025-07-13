<bs:refreshable containerId="projectWebhooksContainer" pageUrl="${pageUrl}">
    <c:if test="${not projectBean.webHooksEnabledForProject}" >
        <div><strong>WARNING: Webhook processing is currently disabled for this project</strong></div>
    </c:if>
    <p>There are <strong>${fn:length(projectBean.webHookList)}</strong> WebHooks configured for all builds in this project.
        <c:if test="${showLinksToOldEditPage}">
            <a href="../webhooks/index.html?projectId=${projectExternalId}">Edit project WebHooks</a>.</p>
        </c:if>
    <table id="webHookTable" class="parametersTable settings webhooktable">
        <c:if test="${fn:length(projectBean.webHookList) > 0}" >

        <thead>
            <tr>
                <th style="font-weight: bold; width:40%">URL and Tags</th>
                <th style="font-weight: bold; width:20%">Format</th>
                <th style="font-weight: bold; width:20%">Build Events</th>
                <th style="font-weight: bold; width:20%" colspan=3>Enabled Builds</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach items="${projectBean.webHookList}" var="hook">
            <tr id="viewRow_${hook.uniqueKey}" class="webHookRow">
                <td><c:out value="${hook.url}" />
                <c:if test="${fn:length(hook.tags) > 0}" >
                    <div class="tagsContainer"><span class="tagsLabel">Tags: </span>
                        <ul class="commalist webhookTags">
                            <c:forEach items="${hook.tags}" var="tag">
                                <li title="<c:out value="${tag.type.description}" />"><a href="../webhooks/search.html?tag=<c:out value="${tag.name}" />"><c:out value="${tag.name}" /></a></li>	
                            </c:forEach>
                        </ul>	
                    </div>
                </c:if>
                </td>
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
                <c:if test="${showEditButton}">
                	<c:if test="${allowSave}">
	                    <td><a onclick="${webhookConfigDialogScope}.showEditDialog({'webhookId':'${hook.uniqueKey}','projectId':'${projectExternalId}'}, '#hookPane');" href="javascript://">edit</a></td>
	                    <td><a onclick="${webhookConfigDialogScope}.showDeleteDialog({'webhookId':'${hook.uniqueKey}','projectId':'${projectExternalId}'});" href="javascript://">delete</a></td>
	                </c:if>
	                <c:if test="${not allowSave}">
	                	<td colspan="2"><a onclick="${webhookConfigDialogScope}.showEditDialog({'webhookId':'${hook.uniqueKey}','projectId':'${projectExternalId}'}, '#hookPane');" href="javascript://">view</a></td>
	                </c:if>
                </c:if>

            </tr>
        </c:forEach>
        </tbody>
        </c:if>
        <c:if test="${showEditButton}">
            <tfoot>
                <tr class="newWebHookRow">
                    <td colspan="6" class="highlight newWebHookRow"><p onclick="${webhookConfigDialogScope}.showAddDialog({'projectId':'${projectExternalId}'}, '#hookPane');" class="addNew">Click to create a new WebHook for this project</p></td>
                </tr>
            </tfoot>
        </c:if>
    </table>
    <script>WebHooksPlugin.afterRefresh();</script>
</bs:refreshable>