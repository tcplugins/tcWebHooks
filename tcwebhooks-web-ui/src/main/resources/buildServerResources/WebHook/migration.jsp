<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHook Migrations" scope="request"/>
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
        ${jspHome}WebHook/js/editWebhook.js
        ${jspHome}WebHook/3rd-party/highlight/highlight.min.js"
      </bs:linkScript>

    <script type="text/javascript">
      BS.Navigation.items = [
    	  <c:if test="${not empty project}"> 
        	{title: "${project.name}", url: '<c:url value="../project/${project.externalId}"/>'},
        	{title: "WebHooks", url: '<c:url value="/webhooks/index.html?projectId=${project.externalId}"/>'},
          </c:if>
        {title: '${title}', selected: true}
      ];
    </script>
    </jsp:attribute>

    <jsp:attribute name="body_include">
    
    	<c:set var="showDetailLink" value="false" scope="request"/>
		<c:set var="showReRunMigration" value="true" scope="request"/>
    	
    	
	    <div>
	    	<c:if test="${not empty output}">
	    		<h2>Migration Output</h2>
	    		The following is the output from re-running of the migration.
	    		<pre><code class="language-plaintext"><c:out value="${output}"></c:out></code></pre>
				<script>hljs.highlightAll();</script>
	    	</c:if>
	    	
	    	<h2>Project WebHooks</h2>
		    <table class="settings templateTable">
				<tbody>
					<%@ include file="jsp-includes/projectMigrationDetail.jsp" %>
		    	</tbody>
			</table>
	    	<c:if test="${not empty kotlinDsls}">
	    		<h2>Existing WebHooks converted to KotlinDSL</h2>
	    		<pre><code class="language-kotlin"><c:out value="${kotlinDsls}"></c:out></code></pre>
				<script>hljs.highlightAll();</script>
	    	</c:if> 
	    	<c:if test="${not empty projectFeaturesXml}">
	    		<h2>Existing WebHooks converted to Project Features XML</h2>
	    		<pre><code class="language-xml"><c:out value="${projectFeaturesXml}"></c:out></code></pre>
				<script>hljs.highlightAll();</script>
	    	</c:if> 
	    </div>
	    
    </jsp:attribute>
</bs:page>