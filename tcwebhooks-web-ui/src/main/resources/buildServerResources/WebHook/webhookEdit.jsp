<%@ include file="/include.jsp" %>
<c:set var="pageTitle" value="Edit WebHooks" scope="request"/>
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
      ${jspHome}WebHook/js/editWebhookParameter.js
      </bs:linkScript>

      <script type="text/javascript">
        BS.Navigation.items = [
		  {title: "Projects", url: '<c:url value="/overview.html"/>'},
		  <c:if test="${haveProject}"> 
		  	{title: "<c:out value="${projectName}"/>", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		  </c:if>
		  <c:if test="${haveBuild}"> 
		  	{title: "<c:out value="${buildName}"/>", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		  </c:if>
          {title: "${pageTitle}", selected:true}
        ];
    
      </script>
    </jsp:attribute> 
      
    <jsp:attribute name="body_include">
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/jquery.easytabs.min.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/jquery.color.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/moment-2.22.2.min.js"></script>
	<script type=text/javascript src="..${jspHome}WebHook/3rd-party/highlight/highlight.pack.js"></script>

	<script type="text/javascript">
	var webhookDialogWidth = -1;
	var webhookDialogHeight = -1;
	var templatePaneOuterHeight = -1;
	$j(document).ready( function() {
		$j('#tab-container').easytabs({
			  animate: false,
			  updateHash: false
		});
		
		$j('#payloadFormatHolder').change(function() {
			var templateId = $j(this).val();
				$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
				if (templateId === templateKey){
					$j("#hookPane .buildState").each(function(thing, state){
						if (($j.inArray(state.id, template.supportedStates) >= 0) &&
							($j.inArray(state.id, template.supportedBranchStates) >= 0))
						{
								$j("td." + state.id).removeClass('buildStateDisabled');
								$j("input#" + state.id).prop('disabled', false);
								$j("#webhookPreviewBuildEvent option[value=" + state.id + "]").prop('disabled', false);
						} else {
								$j("td." + state.id).addClass('buildStateDisabled');
								$j("input#" + state.id).prop('disabled', 'disabled');
								$j("#webhookPreviewBuildEvent option[value=" + state.id + "]").prop('disabled', 'disabled');
						}
					});
					return false;
				}
			});
		});
		
		$j('#extraAuthType').empty();
		$j('#extraAuthType').append($j("<option />").val("").text("No Authentication"));
		$j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes, function(key, authType){
			$j('#extraAuthType').append($j("<option />").val(key).text(authType.description));
		});
			
		$j('select.templateAjaxRefresh').change(function() {
			renderPreviewOnChange()
		});		
		
	});
	
	var restApiDetected = true;
	
	function populateBuildHistory() {
		
		<c:if test="${not haveBuild && haveProject}"> 
			populateBuildHistoryAjax("project:${projectExternalId},");
		</c:if>
		<c:if test="${haveBuild}">				
			populateBuildHistoryAjax("buildType:${buildExternalId},");
		</c:if>
	}
	
	function renderPreviewOnChange() {
		if ($j('#payloadFormatHolder').val()) {
			$j('#currentTemplateName').text(lookupTemplateName($j('#payloadFormatHolder').val()));
		} else {
			$j('#currentTemplateName').html("&nbsp;");
		}
		
		if (
				$j("#webhookPreviewBuildEvent").val() &&
				$j("#webhookPreviewBuildId").val() &&
				$j("#payloadFormatHolder").val()
			)
		{
		
			var selectedBuildState = $j('#webhookPreviewBuildEvent').val();
			var selectedBuildId = $j('#webhookPreviewBuildId').val();
			if (selectedBuildId === "") {
				$j('#webhookPreviewRendered').html("");
			} else {
				$j.ajax ({
					url: "testWebHook.html?action=preview",
					type: "POST",
					dataType: 'json',
					headers : {
						'Content-Type' : 'application/json',
						'Accept' : 'text/html'
					},    				
					data: JSON.stringify({
									"url": $j('#webHookUrl').val(),
									"projectExternalId": $j("#editWebHookForm input[id='projectExternalId']").val(),
									"uniqueKey": $j("#editWebHookForm input[id='webHookId']").val(),
									"testBuildState": selectedBuildState,
									"buildId": selectedBuildId,
									"templateId": lookupTemplate($j('#payloadFormatHolder').val()),
									"payloadFormat": lookupFormat($j('#payloadFormatHolder').val()),
									"authType" : lookupAuthType($j("#editWebHookForm :input#extraAuthType").val()),
									"authEnabled" : lookupAuthEnabled($j("#editWebHookForm :input#extraAuthType").val()),
									"authPreemptive" : $j("#editWebHookForm :input#extraAuthPreemptive").is(':checked'),
									"authParameters" : lookupAuthParameters($j("#editWebHookForm :input#extraAuthType").val(), $j("#editWebHookForm :input.authParameterItemValue")),
									"configBuildStates" : {
										"BUILD_SUCCESSFUL" : true, 
										"CHANGES_LOADED" : false, 
									    "BUILD_FAILED" : true,
									    "BUILD_BROKEN" : true,
									    "BUILD_STARTED" : false,
									    "BUILD_ADDED_TO_QUEUE" : false,
									    "BUILD_REMOVED_FROM_QUEUE" : false,
									    "BEFORE_BUILD_FINISHED" : false,
									    "RESPONSIBILITY_CHANGED" : false,
										"BUILD_FIXED" : true,
									    "BUILD_INTERRUPTED" : false,
									    "BUILD_PINNED" : false,
									    "BUILD_UNPINNED" : false
								    }
								}),
					success:(function(data){
									if(data.errored) {
										$j('#webhookPreviewRendered').html(
												"<b>An error occured building the payload preview</b><br>").append(
												$j("<div style='padding:1em;'></div>").text(data.exception.detailMessage));
									} else {
										$j('#webhookPreviewRendered').html(data.html);
										
										$j('#webhookPreviewRendered pre code').each(function(i, block) {
										    hljs.highlightBlock(block);
										});
									}
								})
				});
			}
		} else {
			$j('#webhookPreviewRendered').html();
		}
		
	}

	</script>

    <div class="editBuildPageGeneral" style="background-color:white; float:left; margin:0; padding:0; width:70%;">
    
        <c:choose>  
    		<c:when test="${haveBuild}"> 
			    <h2 class="noBorder">WebHooks applicable to build <c:out value="${buildName}"/></h2>
			    To edit all webhooks for builds in the project <a href="index.html?projectId=${projectExternalId}">edit Project webhooks</a>.
         	</c:when>  
         	<c:otherwise>  
			    <h2 class="noBorder">WebHooks configured for project <c:out value="${projectName}"/></h2>
         	</c:otherwise>  
		</c:choose>  


  		<div id="messageArea"></div>
	    <div id="systemParams"><!--  begin systemParams div -->

		<c:choose>
			<c:when test="${not haveProject}">
				<strong>${errorReason}</strong><br/>Please access this page via the WebHooks tab on a project or build overview page. 
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test="${hasPermission}">
					<%@ include file="webHookInclude.jsp" %>
					</c:when>
					<c:otherwise>
						<c:choose>
							<c:when test="${projectName == '_Root'}">
								<strong>You must have System Administrator permission to edit _Root WebHooks</strong>
							</c:when>
							<c:otherwise>	
								<strong>You must have Project Administrator permission to edit WebHooks</strong>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>

        </div><!--  end systemParams div -->
      </div>
    <div id=sidebarAdmin>
      <div class=configurationSection>
      	<h2>WebHook Information</h2>
          <p>WebHooks are simply HTTP POST requests or "callbacks" triggered by events. They allow one web application (in this case TeamCity) to notify another web app of events.</p>
          <p>When an event occurs, the tcWebHooks plugin will submit an HTTP POST to the URL configured. The receiving webapp is then able to use the information for any purpose. It could be used to light a lava lamp, or post a message on an IRC channel.</p>

			<c:choose>
				<c:when test="${ShowFurtherReading == 'ALL'}">
				          <p>Further Reading:
				          <ul>${moreInfoText}
				          	<li><a href="http://netwolfuk.wordpress.com/teamcity-plugins/">tcWebHooks plugin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'DEFAULT'}">
				          <p>Further Reading:
				          <ul>
				          	<li><a href="http://netwolfuk.wordpress.com/teamcity-plugins/">tcWebHooks plugin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'SINGLE'}">
				          <p>Further Reading:</p>
				          <ul>${moreInfoText}</ul>
				</c:when>
			</c:choose>
			
		  <h2>WebHook Templates</h2>
		  <p><a href="templates.html">WebHook Templates</a> are a way of packaging up a set of payloads together. The template can then be re-used by any number of webhooks.</p>
		  
		  
		  <h2>Testing Endpoint</h2>	
		  <p>It is possible to test webhooks by posting them back to the tcWebHooks plugin inside TeamCity. See <a href="endpoint-viewer.html"/>here for details</a>.</p>
		  
		  <h2>Plugin Information</h2>	
		  <p>tcWebHooks version: <strong>${pluginVersion}</strong></p>
      </div>
    </div>

    <script type=text/javascript>
	        $('systemParams').updateContainer = function() {
        <c:choose>  
    		<c:when test="${haveBuild}"> 
	          	$j.get("settingsList.html?buildTypeId=<c:out value="${buildExternalId}"/>", function(data) {
         	</c:when>  
         	<c:otherwise>  
	          	$j.get("settingsList.html?projectId=<c:out value="${projectId}"/>", function(data) {
         	</c:otherwise>  
		</c:choose>  	        
	          		ProjectBuilds = data;
	          		$j('.webHookRow').remove();
	          		addWebHooksFromJsonCallback();
				});
	        }
	</script>
    	
    </jsp:attribute>
</bs:page>