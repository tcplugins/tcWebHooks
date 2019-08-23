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

<c:set var="pageTitle" value="Diff WebHook Template" scope="request"/>

<bs:page>
  <jsp:attribute name="head_include">
    <bs:linkCSS>
      /css/admin/adminMain.css
      ${teamcityPluginResourcesPath}WebHook/css/styles.css
      ${teamcityPluginResourcesPath}WebHook/3rd-party/highlight/styles/tomorrow.css
      ${teamcityPluginResourcesPath}WebHook/3rd-party/diff2html-2.11.2/diff2html.css
    </bs:linkCSS>
    <bs:linkScript>
      ${teamcityPluginResourcesPath}WebHook/3rd-party/moment-2.22.2.min.js
      ${teamcityPluginResourcesPath}WebHook/js/editWebhookTemplate.js
      ${teamcityPluginResourcesPath}WebHook/3rd-party/highlight/highlight.pack.js
      ${teamcityPluginResourcesPath}WebHook/3rd-party/diff2html-2.11.2/diff2html.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Webhook Templates", url: '<c:url value="/webhooks/templates.html"/>'},
        {title: "<c:out value="${webhookTemplateBean.templateDescription}"/>", url: '<c:url value="/webhooks/template.html?template=${webhookTemplateBean.templateId}"/>'},
        {title: "Diff Template", selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="body_include">
  
  	    <script type=text/javascript>
		$j(document).ready( function() {
				var templateId = '${webhookTemplateBean.templateId}';
				$j.ajax ({
					url: window['base_uri'] + '/app/rest/webhooks/templates'
											+ '/id:' + templateId + ',status:PROVIDED'
											+ '/diff'
											+ '/id:' + templateId + ',status:USER_OVERRIDDEN'
											+ '?fields=$long,content&context=100',
					type: "GET",
					headers : {
						'Accept' : 'text/plain'
					},
					success: function (response) {
						var diffHtml = Diff2Html.getPrettyHtml(
								  response,
								  { 
									  inputFormat: 'diff', 
									  showFiles: false, 
									  matching: 'lines', 
									  outputFormat: 'line-by-line',
									  renderNothingWhenEmpty: false
								  }
								);
						$j('#url-diff-container').html(diffHtml);
					}
				});
				
				
		});
		var restApiDetected = ${isRestApiInstalled};
		</script>	
  
	<c:if test="${not isRestApiInstalled}">
		<div class="icon_before icon16 attentionRed">The <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> is not installed. Most settings on this page will non-functional.</div>
	</c:if>
	<div id="wrapper">
	<div class="original"></div>
	<div class="changed"></div>
	<div id="diff" class="diff"></div>
	</div>
	<div id="line-by-line"></div>
	<div id="url-diff-container" style="margin: 0 auto;">
	</div>
  </jsp:attribute>
</bs:page>