<%@ include file="/include.jsp" %>
<c:set var="pageTitle" value="WebHooks Javascript Tests" scope="request"/>
<bs:page>

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
      ${jspHome}WebHook/3rd-party/highlight/styles/tomorrow.css
      ${jspHome}WebHook/3rd-party/mocha@9.1.3/mocha.css
      ${jspHome}WebHook/css/mocha.css
    </bs:linkCSS>
    <bs:linkScript>
      /js/bs/blocks.js
      /js/bs/blocksWithHeader.js
      /js/bs/forms.js
      /js/bs/modalDialog.js
      /js/bs/editBuildType.js
      /js/bs/editProject.js
      /js/bs/adminActions.js
      
      ${jspHome}WebHook/js/tests/test-data.js
      ${jspHome}WebHook/js/editWebhookCommon.js
      ${jspHome}WebHook/js/editWebhookParameter.js
      ${jspHome}WebHook/js/editWebhookHeader.js
      ${jspHome}WebHook/js/editWebhookFilter.js
      ${jspHome}WebHook/js/editWebhookConfiguration.js
      ${jspHome}WebHook/js/noRestApi.js
    </bs:linkScript>

  </jsp:attribute> 
      
  <jsp:attribute name="body_include">
	  <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/jquery.easytabs.min.js"></script>
  	<script type=text/javascript src="../..${jspHome}WebHook/3rd-party/jquery.color.js"></script>
	  <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/moment-2.22.2.min.js"></script>
	  <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/highlight/highlight.pack.js"></script>
    <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/mocha@9.1.3/chai.js"></script>
    <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/mocha@9.1.3/mocha.js"></script>
    <script type=text/javascript src="../..${jspHome}WebHook/3rd-party/mocha@9.1.3/sinon-9.2.4.js"></script>
	
    <div id="mocha"></div>


    <script class="mocha-init">
      mocha.setup('bdd');
      mocha.checkLeaks();
    </script>
	<%@ include file="jsp-includes/editWebHookDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookParameterDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookHeaderDialog.jsp" %>
	<%@ include file="jsp-includes/editWebHookFilterDialog.jsp" %>
    <script type="module" type=text/javascript src="../..${jspHome}WebHook/js/tests/editWebhookConfiguration.tests.js"></script>
    
    <script class="mocha-exec">
      mocha.run();
    </script>
    
    
    </jsp:attribute>
</bs:page>
