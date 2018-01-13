<%--
~ Copyright 2017 Net Wolf UK
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>
<%@ include file="/include.jsp" %>

<c:set var="pageTitle" value="WebHook Template Not Found" scope="request"/>

<bs:page>
  <jsp:attribute name="head_include">
    <bs:linkCSS>
      /css/admin/adminMain.css
      ${teamcityPluginResourcesPath}WebHook/css/styles.css
    </bs:linkCSS>
    <bs:linkScript>
      ${teamcityPluginResourcesPath}WebHook/js/editWebhookTemplate.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Webhook Templates", url: '<c:url value="/webhooks/templates.html"/>'},
        {title: 'Template Not Found', selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="body_include">

	The requested WebHook Template was not found. Please visit the <a href="templates.html">WebHook Templates</a> page to view an alternative template or create a new one. 
	
  </jsp:attribute>
</bs:page>