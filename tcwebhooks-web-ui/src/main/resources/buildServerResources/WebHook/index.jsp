<%@ include file="/include.jsp" %>
<c:set var="title" value="WebHooks" scope="request"/>
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
		  {title: "Projects", url: '<c:url value="/overview.html"/>'},
		  <c:if test="${haveProject}"> 
		  	{title: "${projectName}", url: '<c:url value="/project.html?projectId=${projectExternalId}"/>'},
		  </c:if>
		  <c:if test="${haveBuild}"> 
		  	{title: "${buildName}", url: '<c:url value="/viewType.html?buildTypeId=${buildExternalId}"/>'},
		  </c:if>
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">
    <c:if test="${includeJquery}">
    	<script type=text/javascript src="..${jspHome}WebHook/js/jquery-1.4.3.min.js"></script>
    </c:if>
	<script type=text/javascript src="..${jspHome}WebHook/js/jquery.easytabs.min.js"></script>
    <script type=text/javascript>
		var jQueryWebhook = jQuery.noConflict();
		var webhookDialogWidth = -1;
		jQueryWebhook(document).ready( function() {
				jQueryWebhook('#tab-container').easytabs({
					  animate: false,
					  updateHash: false
				});
				jQueryWebhook('#payloadFormatHolder').change(function() {
					var formatName = jQueryWebhook(this).val();
  					jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(thing, config){
						if (formatName === config[0]){
							var template = config[1];
							jQueryWebhook("#hookPane .buildState").each(function(thing, state){
								if ((jQueryWebhook.inArray(state.id, template.supportedStates) >= 0) &&
									(jQueryWebhook.inArray(state.id, template.supportedBranchStates) >= 0))
								{
										jQueryWebhook("td." + state.id).removeClass('buildStateDisabled');
										jQueryWebhook("input#" + state.id).prop('disabled', false);
										jQueryWebhook("#currentTemplateBuildEvent option[value=" + state.id + "]").prop('disabled', false);
								} else {
										jQueryWebhook("td." + state.id).addClass('buildStateDisabled');
										jQueryWebhook("input#" + state.id).prop('disabled', 'disabled');
										jQueryWebhook("#currentTemplateBuildEvent option[value=" + state.id + "]").prop('disabled', 'disabled');
								}
							//console.log(state);
							//console.log(state.id);
							});
							
							//if (jQueryWebhook.inArray
							//jQueryWebhook.each(template.supportedStates, function(thingy, state){
							//console.log("My state is: " + state);
							//		jQueryWebhook("td." + state).removeClass('buildStateDisabled');
							//		jQueryWebhook("input#" + state).prop('disabled', false);
							//}); 
							return false;
						}
					});
				});
				
				
				jQueryWebhook('select.templateAjaxRefresh').change(function() {
					var selectedBuildState = jQueryWebhook('#currentTemplateBuildEvent').val();
					var selectedBuildId = jQueryWebhook('#currentTemplateBuildId').val();
					jQueryWebhook.getJSON( "renderTemplate.html", {
										projectId: "${projectExternalId}",
										buildState: selectedBuildState,
										buildId: selectedBuildId,
										payloadTemplate: lookupTemplate(jQueryWebhook('#payloadFormatHolder').val()),
										payloadFormat: lookupFormat(jQueryWebhook('#payloadFormatHolder').val())
									})
									.done(function(data){
											console.log(data);
											console.log(data.templatesOutput);
											jQueryWebhook('#currentTemplateRaw').html(data.templatesOutput.webhookTemplate);
											jQueryWebhook('#currentTemplateRendered').html(data.templatesOutput.webhookTemplateRendered);
									});
				});
		});

		function selectBuildState(){
			doExtraCompleted();
		}

		function doExtraCompleted(){
			if(jQueryWebhook('#buildSuccessful').is(':checked')){
				jQueryWebhook('.onBuildFixed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').prop('disabled', false);
			} else {
				jQueryWebhook('.onBuildFixed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').prop('disabled', true);
			} 
			if(jQueryWebhook('#buildFailed').is(':checked')){
				jQueryWebhook('.onBuildFailed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').prop('disabled', false);
			} else {
				jQueryWebhook('.onBuildFailed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').prop('disabled', true);
			}
		}
		
		function toggleAllBuildTypesSelected(){
			jQueryWebhook.each(jQueryWebhook('.buildType_single'), function(){
				jQueryWebhook(this).prop('checked', jQueryWebhook('input.buildType_all').is(':checked'))
			});
			updateSelectedBuildTypes();
		}
		
		function updateSelectedBuildTypes(){
			var subText = "";
		    if(jQueryWebhook('#buildTypeSubProjects').is(':checked')){
		    	subText = " &amp; sub-projects";
		    }
		
			if(jQueryWebhook('#webHookFormContents input.buildType_single:checked').length == jQueryWebhook('#webHookFormContents input.buildType_single').length){
				jQueryWebhook('input.buildType_all').prop('checked', true);
				jQueryWebhook('span#selectedBuildCount').html("all" + subText);
			} else {
				jQueryWebhook('input.buildType_all').prop('checked', false);
				jQueryWebhook('span#selectedBuildCount').html(jQueryWebhook('#webHookFormContents input.buildType_single:checked').length + subText);
			}

		}
		
		function populateWebHookDialog(id){
			jQueryWebhook('#buildList').empty();
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList, function(thing, config){
				if (id === config[0]){
					var webhook = config[1];
				
					jQueryWebhook('#webHookId').val(webhook.uniqueKey);	
					jQueryWebhook('#webHookUrl').val(webhook.url);
				    jQueryWebhook('#webHooksEnabled').prop('checked', webhook.enabled);
				    jQueryWebhook.each(webhook.states, function(name, value){
				    	jQueryWebhook('#' + value.buildStateName).prop('checked', value.enabled);
				    });
				    
					jQueryWebhook('#webHookFormContents select#payloadFormatHolder').val(webhook.payloadTemplate + "_" + webhook.payloadFormat).change();
					
					jQueryWebhook('#buildTypeSubProjects').prop('checked', webhook.subProjectsEnabled);
					jQueryWebhook.each(webhook.builds, function(){
						 if (this.enabled){
					 	 	jQueryWebhook('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input checked onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
						 } else {
						 	 jQueryWebhook('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
						 }
					});
				}
			});
			updateSelectedBuildTypes();
			jQueryWebhook('#currentTemplateBuildId').empty();
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.projectHistory.recentBuilds, function(thing, build){
				jQueryWebhook('#currentTemplateBuildId').append(jQueryWebhook("<option />").val(build.buildId).text(build.title + "#" + build.buildNumber + " (" + build.buildDate + ")"));
			});
			
		}

		function lookupTemplate(templateFormatCombinationKey){
			var name;
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(thing, config){
				if (templateFormatCombinationKey === config[0]){
					var template = config[1];
					name = template.templateShortName;
					return false;
				}
			});
			return name;
		}
		
		function lookupFormat(templateFormatCombinationKey){
			var name;
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(thing, config){
				if (templateFormatCombinationKey === config[0]){
					var template = config[1];
					name = template.formatShortName;
					return false;
				}
			});
			return name;
		}
		
		function htmlEscape(str) {
    		return String(str)
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
		}
		
		function addWebHooksFromJsonCallback(){
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList, function(thing, config){
				if ('new' !== config[0]){
					var webhook = config[1];
					jQueryWebhook('.webHookRowTemplate')
									.clone()
									.prop("id", "viewRow_" + webhook.uniqueKey)
									.removeClass('webHookRowTemplate')
									.addClass('webHookRow')
									.appendTo('#webHookTable > tbody');
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemUrl").html(htmlEscape(webhook.url)).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey, '#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").html(webhook.payloadFormatForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEvents").html(webhook.enabledEventsListForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemBuilds").html(webhook.enabledBuildsListForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey, '#buildPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEdit > a").click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemDelete > a").click(function(){BS.WebHookForm.removeWebHook(webhook.uniqueKey,'#hookPane');});
					
				}
			});
			
			jQueryWebhook('#currentTemplateBuildId').empty();
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.projectHistory.recentBuilds, function(thing, build){
				jQueryWebhook('#currentTemplateBuildId').append(jQueryWebhook("<option />").val(build.buildId).text(build.title + "#" + build.buildNumber + " (" + build.buildDate + ")"));
			});
		}

		BS.EditWebHookDialog = OO.extend(BS.AbstractModalDialog, {
			  getContainer : function() {
			    return $('editWebHookDialog');
			  },

			  showDialog : function(id, tab) {
				BS.WebHookForm.clearErrors();
			    
			    populateWebHookDialog(id);
			    doExtraCompleted();
			    
			    var title = id == "new" ? "Add New" : "Edit";
			    title += " WebHook";

			    $('webHookDialogTitle').innerHTML = title;


			    if (webhookDialogWidth < 0){
			    	webhookDialogWidth = jQueryWebhook('#editWebHookDialog').innerWidth();
			    } else {
			    	jQueryWebhook('#editWebHookDialog').innerWidth(webhookDialogWidth);
			    }
			    
			    this.showCentered();
			    jQueryWebhook('#hookPane').innerHeight(jQueryWebhook('#templatePane').innerHeight());
			    jQueryWebhook('#buildPane').innerHeight(jQueryWebhook('#templatePane').innerHeight());
				jQueryWebhook('#tab-container').easytabs('select', tab);
			    
			    jQueryWebhook('#webHookUrl').focus();
			  },

			  cancelDialog : function() {
			    this.close();
			  }
			});

		BS.BaseSaveWebHookListener = OO.extend(BS.SaveConfigurationListener, {
			  onBeginSave : function(form) {
			    form.formElement().webHookUrl.value = BS.Util.trimSpaces(form.formElement().webHookUrl.value);
			    form.clearErrors();
			    form.hideSuccessMessages();
			    form.disable();
			    form.setSaving(true);
			  }
			});

		BS.WebHookForm = OO.extend(BS.AbstractWebForm, {
			  setSaving : function(saving) {
			    if (saving) {
			      BS.Util.show('webHookSaving');
			    } else {
			      BS.Util.hide('webHookSaving');
			    }
			  },

			  formElement : function() {
			    return $('WebHookForm');
			  },

			  saveWebHook : function() {
			    this.formElement().submitAction.value = 'updateWebHook';
			    this.formElement().payloadTemplate.value = lookupTemplate(this.formElement().payloadFormatHolder.value);
			    this.formElement().payloadFormat.value = lookupFormat(this.formElement().payloadFormatHolder.value);
			    var that = this;

			    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener,
			 	{
			      onEmptyWebHookUrlError : function(elem) {
			        $("error_webHookUrl").innerHTML = elem.firstChild.nodeValue;
			        that.highlightErrorField($('webHookUrl'));
			      },

			      onEmptyPayloadFormatError : function(elem) {
			        $("error_payloadFormat").innerHTML = elem.firstChild.nodeValue;
			        that.highlightErrorField($('payloadFormatTable'));
			      },

			      onCompleteSave : function(form, responseXML, err) {
			    	BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
			        form.enable();
			        if (!err) {
			          $('systemParams').updateContainer();
			          BS.EditWebHookDialog.close();
			        }
			      }
			    }));

			    return false;
			  },

			  removeWebHook : function(paramId) {
			    var that = this;

			    if (!confirm("Are you sure you want to delete this WebHook?")) return;

			    var url = this.formElement().action + "&submitAction=removeWebHook&removedWebHookId=" + paramId;

			    BS.ajaxRequest(url, {
			      onComplete: function() {
			        $('systemParams').updateContainer();
			        BS.EditWebHookDialog.close();
			      }
			    });
			  }
			});
	</script>
    <div class="editBuildPageGeneral" style="background-color:white; float:left; margin:0; padding:0; width:70%;">
    
        <c:choose>  
    		<c:when test="${haveBuild}"> 
			    <h2 class="noBorder">WebHooks applicable to build ${buildName}</h2>
			    To edit all webhooks for builds in the project <a href="index.html?projectId=${projectExternalId}">edit Project webhooks</a>.
         	</c:when>  
         	<c:otherwise>  
			    <h2 class="noBorder">WebHooks configured for project ${projectName}</h2>
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
	          	jQueryWebhook.get("settingsList.html?buildTypeId=${buildExternalId}", function(data) {
         	</c:when>  
         	<c:otherwise>  
	          	jQueryWebhook.get("settingsList.html?projectId=${projectId}", function(data) {
         	</c:otherwise>  
		</c:choose>  	        
	          		ProjectBuilds = data;
	          		jQueryWebhook('.webHookRow').remove();
	          		addWebHooksFromJsonCallback();
				});
	        }

	</script>
    </jsp:attribute>
</bs:page>
