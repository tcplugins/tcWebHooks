<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<c:set var="showDetailLink" value="true" scope="request"/>
<c:set var="showReRunMigration" value="false" scope="request"/>

<c:choose>
<c:when test="${not empty migrationReport}">

	    		<h2>Migration Report</h2>
	    		The following is the output from the migration report.
	    		<pre><code class="language-plaintext"><c:out value="${migrationReport}"></c:out></code></pre>
				<script>hljs.highlightAll();</script>

</c:when>
<c:otherwise>
	    <div>
	    <p>
	    The first time the tcWebHooks version 3 plugin starts, it will run the automatic migration process. 
	    This will convert existing webhooks from the old configuration format (which stores the configuration in <code>plugin-settings.xml</code>) to the new "Project Features" format (which stores its configuration in <code>project-config.xml</code> or in the Kotlin <code>settings.kts</code> file in the project's VCS).
	    </p>
	    <p>
	    There are five scenarios for migration:
	    <ol>
	    <li>
	    	<strong>Project settings are not stored in VCS.</strong> - NON_VCS_XML_MIGRATABLE
	    	<br>Migration will be attempted.
	    	<br>Existing webhooks should migrate automatically.
	    </li>
	    <li>
	    	<strong>Project settings are stored in VCS as Kotlin and <em>Allow editing project settings via UI</em> is enabled.</strong> - VCS_KOTLIN_SYNC_ENABLED_NOT_MIGRATABLE
	    	<br>Migration will NOT be attempted.
	    	<br>Example Kotlin scripts will be generated, and will need to be copied into the <code>setting.kts</code> file and committed to VCS. 
	    	<br>Existing <code>plugin-settings.xml</code> files in VCS will need to be cleaned up.
	    </li>
	    <li>
	    	<strong>Project settings are stored in VCS as Kotlin and <em>Allow editing project settings via UI</em> is disabled.</strong> - VCS_KOTLIN_SYNC_DISABLED_NOT_MIGRATABLE
	    	<br>Migration will NOT be attempted.
	    	<br>Example Kotlin scripts will be generated, and will need to be copied into the <code>setting.kts</code> file and committed to VCS. 
	    	<br>Existing <code>plugin-settings.xml</code> files in VCS will need to be cleaned up.
	    </li>
	    <li>
	    	<strong>Project settings are stored in VCS as XML and <em>Allow editing project settings via UI</em> is enabled.</strong> - VCS_XML_SYNC_ENABLED_MIGRATABLE
	    	<br>Migration will be attempted. 
	    	<br>If migration is not successful, example XML files can be generated and will need to be copied into the <code>project-config.xml</code> file and committed to VCS.
	    	<br>If migration is not successful, existing <code>plugin-settings.xml</code> files in VCS will need to be cleaned up and committed to VCS.
	    </li>
	    <li>
	    	<strong>Project settings are stored in VCS as XML and <em>Allow editing project settings via UI</em> is disabled.</strong> - VCS_XML_SYNC_DISABLE_NOT_MIGRATABLE
	    	<br>Migration will NOT be attempted.
	    	<br>Example XML files can be generated and will need to be copied into the <code>project-config.xml</code> file and committed to VCS.
	    	<br>Existing <code>plugin-settings.xml</code> files in VCS will need to be cleaned up and committed to VCS.
	    </li>
	    </ol>
	    </p>
	    <bs:refreshable containerId="migrationStatusContainer" pageUrl="${pageUrl}">
	    <h2>Migration Status</h2>
	    <p>The webhook migration is currently marked as <strong>${overallStatus}</strong>.<br>
	    <c:choose>
			<c:when test="${'started' eq overallStatus}">
			This means that the migration has started but was not able to complete successfully.
			It is important to note that the automatic migration will abort at the first error, and some projects will have been migrated whereas others may not have been reached.
			In this scenario, review the list of project migration statuses below and follow the guidance on how to migrate them manually. 
			</c:when>
			<c:when test="${'completed' eq overallStatus}">
			This is good news. It means that the automatic migration was successful and was able to be marked as completed. Any projects that were able to be migrated have completed.
			However for those projects that cannot be migrated, manual remediation will still be required. Please review the list of project migration statuses below and follow the guidance on how to migrate them manually.
			</c:when>
			<c:when test="${'disabled' eq overallStatus}">
			A 'disabled' status indicates that someone has manually set the value in TeamCity's <code>internal.properties</code> file.
			This disables the auto-migration.
			</c:when>
			<c:when test="${'unknown' eq overallStatus}">
			tcWebHooks is not able to determine the status of the auto-migration. No value was specified in TeamCity's <code>internal.properties</code> file, or it is set to a value that tcWebHooks does not understand.
			</c:when>
		</c:choose>
	    </p>
	    <p>
	    There are <c:out value="${fn:length(migrationStatuses)}"/> projects found on this TeamCity that might have webhooks.
	    To view detailed information to aid in migration, click the <strong><em>Migration details for...</em></strong> link in the top right of each project's migration status.
	    This information includes example webhook configurations (in Kotlin or XML, whichever is appropriate) and has the ability to re-run the migration for that project (if that is relevant to its status).
	    </p>
	    <h2>Migration Reports</h2>
	    <div style="float:right; padding-left: 2em;">
	    <button id="reRunMigration" onclick="WebHookPlugin.reRunMigration();" style="float:right; margin-top: 1em;" class="btn btn_primary submitButton">Re-run Full Migration</button>
	     </div>
	    When the migration runs, a plain text report file is generated on the TeamCity server in <code>TEAMCITY_DATA_PATH/system</code> listing all the actions performed as part of the migration, and giving example configuration (where appropriate).<br> 
	    Previous migration reports are listed below:
	    <ul>
	    <c:forEach items="${reportFileNames}" var="reportFileName">
	    	<li><a href="./admin.html?item=tcWebHooksV3Migration&viewReport=<c:out value="${reportFileName.left}"/>" title="<c:out value="${reportFileName.left}"/>">Migration Report from <c:out value="${reportFileName.right}"/></a>
	    </c:forEach>
	    </ul>
	    
	    <h2>Project Migration statuses</h2>
	    <table class="settings templateTable">
			<tbody>
			<c:forEach items="${migrationStatuses}" var="migrationStatus">
				<%@ include file="jsp-includes/projectMigrationDetail.jsp" %>
			</c:forEach>
	    	</tbody>
		</table>
	    </bs:refreshable>
	    </div>

</c:otherwise>
</c:choose>

    <bs:dialog dialogId="confirmMigrationDialog"
               dialogClass="confirmMigrationDialog"
               title="Confirm Re-run Migration"
               closeCommand="WebHookPlugin.ReRunMigrationDialog.cancelDialog()">
        <forms:multipartForm id="confirmMigrationForm"
                             action="save.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHookPlugin.ReRunMigrationDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteWebHookWarningMessage">Are you sure you want to re-run the Webhook Migration?
                <p>Running the migration from this page will force the migration to run and not stop on errors. 
                All migrations will be attempted, and the results will need to be carefully reviewed to see what 
                manual migrations and cleanup is still required.
                        <div id="ajaxConfirmMigrationResult"></div>
                </td></tr>
            </table>
            <input type="hidden" name="action" id="MigrationAction"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="confirmMigrationDialogSubmit" label="Re-run WebHook Migration"/>
                <forms:cancel onclick="WebHookPlugin.ReRunMigrationDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
