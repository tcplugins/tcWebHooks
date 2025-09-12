<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHook Configurations" scope="request"/>
<bs:page>
	<jsp:attribute name="page_title">
	  ${title}
	</jsp:attribute>

    <jsp:attribute name="head_include">
      <bs:linkCSS>
        /css/admin/adminMain.css
        /css/admin/projectConfig.css
        /css/forms.css
        /css/admin/vcsRootsTable.css

        /css/visibleProjects.css
        /css/settingsTable.css
        /css/profilePage.css
        /css/userRoles.css

        ${jspHome}WebHook/css/styles.css
        ${jspHome}WebHook/3rd-party/highlight/styles/default.css
      </bs:linkCSS>

      <bs:linkScript>
        /js/bs/blocks.js
        /js/bs/blocksWithHeader.js
        /js/bs/forms.js
        /js/bs/modalDialog.js
        /js/bs/editBuildType.js
        /js/bs/editProject.js
        /js/bs/adminActions.js
      </bs:linkScript>

    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "WebHooks", url: '<c:url value="/webhooks/index.html"/>'},
        {title: '${title}', selected: true}
      ];
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
    	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/highlight/highlight.min.js"></script>
    	<c:set var="showDetailLink" value="false" scope="request"/>
    	
    	<div>
	    <table class="noborder">
	    	<thead>
	    	<tr><th>Information</th><th>WebHook as Code</th></tr>
	    	</thead>
			<tbody>
			<c:forEach items="${migrationData}" var="webhooks">
					<c:forEach items="${webhooks.value}" var="hook">
							<tr id="viewRow_${hook.key}" class="webHookRow">
							<td class="value webHookMetaData">
								<table class="settings">
									<tr>
										<td class="right">WebHook Id:</td>
										<td>${hook.key}</td>
									</tr>
									<tr>
										<td>Project Feature Id:</td>
										<td><c:if test="${not empty hook.value.migrated}">${hook.value.migrated.featureDescriptorId}</c:if></td>
									</tr>
									<tr>
										<td>Cached:</td>
										<td>
											<c:if test="${not empty hook.value.cached}"><span>This webhook is loaded into the webhook cache</span></c:if>
											<c:if test="${ empty hook.value.cached}"><span>This webhook NOT is loaded into the webhook cache</span></c:if>
										</td>
									</tr>
								</table>
							<td class="name webhookCode"><pre><code class="language-${hook.value.format}"><c:out value="${hook.value.code}"></c:out></code></pre></td>
							</tr>
					</c:forEach>
					<tr class="blankline">
						<td colspan="5" style="border-left:0px; border-right:0px;">&nbsp;</td>
					</tr>
		</c:forEach>
		</tbody>
		</table>
	    </div>
	    <script>hljs.highlightAll();</script>
    </jsp:attribute>
</bs:page>