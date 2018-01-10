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
    ${jspHome}WebHook/highlight/styles/tomorrow.css
        
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
	<script type=text/javascript src="..${jspHome}WebHook/js/jquery.color.js"></script>
    <script type=text/javascript>
		var jQueryWebhook = jQuery.noConflict();
		var webhookDialogWidth = -1;
		var webhookDialogHeight = -1;
		var templatePaneOuterHeight = -1;
		var projectHistory = { "recentBuilds":[] };
		jQueryWebhook(document).ready( function() {
				jQueryWebhook('#tab-container').easytabs({
					  animate: false,
					  updateHash: false
				});
			<c:if test="${haveProject}"> 
				jQueryWebhook.getJSON( "ajax/projectHistory.html", {
					projectId: "${projectExternalId}"
				})
				.done(function(data){
					projectHistory = data;
					populateBuildHistory();
				});
			</c:if>
			<c:if test="${haveBuild}"> 
				jQueryWebhook.getJSON( "ajax/projectHistory.html", {
					buildTypeId: "${buildExternalId}"
				})
				.done(function(data){
					projectHistory = data;
					populateBuildHistory();
				});
			</c:if>
				jQueryWebhook('#payloadFormatHolder').change(function() {
					var formatName = jQueryWebhook(this).val();
  					jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(formatKey, template){
						if (formatName === formatKey){
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
							});
							return false;
						}
					});
				});
				
				jQueryWebhook('#extraAuthType').empty();
				jQueryWebhook('#extraAuthType').append(jQueryWebhook("<option />").val("").text("No Authentication"));
				jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes, function(key, authType){
					jQueryWebhook('#extraAuthType').append(jQueryWebhook("<option />").val(key).text(authType.description));
				});
 				
				jQueryWebhook('select.templateAjaxRefresh').change(function() {
					var selectedBuildState = jQueryWebhook('#currentTemplateBuildEvent').val();
					var selectedBuildId = jQueryWebhook('#currentTemplateBuildId').val();
					if (selectedBuildId === "") {
						jQueryWebhook('#currentTemplateRaw').html("");
						jQueryWebhook('#currentTemplateRendered').html("");
					} else {
						jQueryWebhook.getJSON( "renderTemplate.html", {
											projectId: "${projectExternalId}",
											buildState: selectedBuildState,
											buildId: selectedBuildId,
											payloadTemplate: lookupTemplate(jQueryWebhook('#payloadFormatHolder').val()),
											payloadFormat: lookupFormat(jQueryWebhook('#payloadFormatHolder').val())
										})
										.done(function(data){
												jQueryWebhook('#currentTemplateRaw').html(data.templatesOutput.webhookTemplate);
												jQueryWebhook('#currentTemplateRendered').html(data.templatesOutput.webhookTemplateRendered);
												
												  jQueryWebhook('#currentTemplateRendered pre code').each(function(i, block) {
												    hljs.highlightBlock(block);
												  });
										});
					}
				});
		});
		
		function populateBuildHistory() {
			jQueryWebhook('#currentTemplateRaw').html("");
			jQueryWebhook('#currentTemplateRendered').html("");
			jQueryWebhook('#currentTemplateBuildId').empty();
			jQueryWebhook('#currentTemplateBuildId').append(jQueryWebhook("<option />").val("").text("Choose a build..."));
			jQueryWebhook.each(projectHistory.recentBuilds, function(thing, build) {
				jQueryWebhook('#currentTemplateBuildId').append(jQueryWebhook("<option />").val(build.buildId).text(build.title + "#" + build.buildNumber + " (" + build.buildDate + ")"));
			});
		}
		
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
		/*
			If a webhook has an auth config, it will look like this:
					"authConfig": {
			            "type": "userpass",
			            "preemptive": false,
			            "parameters": {
			              "password": "pass1234",
			              "realm": "TeamCity",
			              "username": "user"
			            }
			          }
		
			So, using the map of registeredAuthTypes find the matching authType's options to show
			and populate its values from the webhook's config.
			
				    "registeredAuthTypes": {
				        "userpass": {
				          "description": "Username/Password Authentication (Basic Auth)",
				          "paramaters": [
				            {
				              "key": "username",
				              "required": true,
				              "hidden": false,
				              "name": "Username",
				              "toolTip": "The username to authenticate as"
				            },
				            {
				              "key": "password",
				              "required": true,
				              "hidden": true,
				              "name": "Password",
				              "toolTip": "The password to authenticate with"
				            },
				            {
				              "key": "realm",
				              "required": false,
				              "hidden": false,
				              "name": "Realm",
				              "toolTip": "The Realm the server must present. This is ignored if preemptive is enabled (the default)"
				            }
				          ]
				        }
				      }			
		*/
		
		var preemptiveToolTip = "Preemptively sends credentials on first request. " +
								"Without preemption, the webhook will only send credentials when a 401 UNAUTHORIZED response is received " +
								"and the Server\'s Realm (if any) matches, which would require two requests for each webhook event.";
		
		function populateWebHookAuthExtrasPane(webhookObj){
			if (webhookObj.hasOwnProperty("authConfig") && ProjectBuilds.templatesAndWebhooks.registeredAuthTypes.hasOwnProperty(webhookObj.authConfig.type)){
				jQueryWebhook('#extraAuthType').val(webhookObj.authConfig.type);
				jQueryWebhook('#extraAuthParameters > tbody').empty();
				jQueryWebhook('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip +'">Preemptive</label></td>' + 
																	 '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip +'" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
				jQueryWebhook('#extraAuthPreemptive').prop('checked', webhookObj.authConfig.preemptive);
				jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[webhookObj.authConfig.type].parameters, function(index, paramObj){
					jQueryWebhook('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
				});
			} else {
				jQueryWebhook('#extraAuthType').val("");
				jQueryWebhook('#extraAuthParameters > tbody').empty();
			}					
		}
		
		function populateWebHookAuthExtrasPaneFromChange(webhookObj){
			var authType = jQueryWebhook('#extraAuthType').val();
			if (authType === ''){
				jQueryWebhook('#extraAuthParameters > tbody').empty();
			} else {
				jQueryWebhook('#extraAuthParameters > tbody').empty();
				jQueryWebhook('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip +'">Preemptive</label></td>' + 
																	 '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip +'" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
				if (webhookObj.hasOwnProperty("authConfig") && webhookObj.authConfig.type == authType){
					jQueryWebhook('#extraAuthPreemptive').prop('checked', webhookObj.authConfig.preemptive);
					jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function(index, paramObj){
						jQueryWebhook('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
					});
				} else {
					jQueryWebhook('#extraAuthPreemptive').prop('checked', webhookObj, true);
					jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function(index, paramObj){
						jQueryWebhook('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, false));
					});					
				}
			
			}
		}
		
		function buildWebHookAuthParameterHtml(paramObj, webhookObj, setValue) {
			var value = '';
			var requireText = '';
			if (setValue) {
				value = 'value="' + webhookObj.authConfig.parameters[paramObj.key] + '" ';
			}
			
			if (paramObj.required) {
				requireText = '<span class="mandatoryAsterix" title="Mandatory field">*</span>';
			}
			return '<tr><td class="authParameterName"><label for="extraAuthParam_' 
					+ paramObj.key + '" title="'+ paramObj.toolTip + '">' 
					+ paramObj.name + requireText + '</label></td><td class="authParameterValueWrapper">' 
					+ '<input title="'+ paramObj.toolTip + '" type=text name="extraAuthParam_' 
					+ paramObj.key + '" ' + value + 'class="authParameterValue"></td></tr>';
					
		}
		
		function populateWebHookDialog(id){
			jQueryWebhook('#buildList').empty();
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList, function(webHookKey, webhook){
				if (id === webHookKey){
					
					jQueryWebhook("#viewRow_" + webhook.uniqueKey).animate({
			            backgroundColor: "#ffffcc"
			    	}, 1000 );
					
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
					
					populateWebHookAuthExtrasPane(webhook);
					jQueryWebhook('select#extraAuthType').change(function() {
						populateWebHookAuthExtrasPaneFromChange(webhook);
					});
				}
			});
			updateSelectedBuildTypes();
			populateBuildHistory();
		}

		function lookupTemplate(templateFormatCombinationKey){
			var name;
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
				if (templateFormatCombinationKey === templateKey){
					name = template.templateId;
					return false;
				}
			});
			return name;
		}
		
		function lookupFormat(templateFormatCombinationKey){
			var name;
			jQueryWebhook.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
				if (templateFormatCombinationKey === templateKey){
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
			var webhookItems = ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList;
			jQueryWebhook.each(webhookItems, function(webHookKey, webhook){
				if ('new' !== webHookKey){
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
				if (webhook.uniqueKey === jQueryWebhook('#webHookId').val()){
					jQueryWebhook("#viewRow_" + webhook.uniqueKey).css('background-color', '#cceecc');
		            jQueryWebhook("#viewRow_" + webhook.uniqueKey).animate({
		                backgroundColor: "#ffffff"
		            }, 1500 );
				}
			});
			
			populateBuildHistory();
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
			    jQueryWebhook('#extrasPane').innerHeight(jQueryWebhook('#templatePane').innerHeight());
				jQueryWebhook('#tab-container').easytabs('select', tab);
				
				if (webhookDialogHeight < 0){
			    	webhookDialogHeight = jQueryWebhook('#editWebHookDialog').innerHeight();
			    	templatePaneOuterHeight = jQueryWebhook('#templatePane').outerHeight();
			    }
			    
			    jQueryWebhook('#webHookUrl').focus();
			  },

			  cancelDialog : function() {
			    this.close();
			    jQueryWebhook('li.tab').removeAttr('style');
	            jQueryWebhook("#viewRow_" + jQueryWebhook('#webHookId').val()).animate({
	                backgroundColor: "#ffffff"
	            }, 500 );
			  },
			  
			  maximizeDialog : function() {
			    var maxDialogWidth = jQueryWebhook( document ).width() - 40;
				var maxDialogHeight = jQueryWebhook( window ).height() - 80;
			  	jQueryWebhook('#editWebHookDialog').innerWidth(maxDialogWidth);
			  	jQueryWebhook('#editWebHookDialog').innerHeight(maxDialogHeight);
			  	var dialogDiff = webhookDialogHeight - templatePaneOuterHeight;
			  	jQueryWebhook('#templatePane').innerHeight(templatePaneOuterHeight + dialogDiff);
			  	jQueryWebhook('#currentTemplateRaw').outerHeight(jQueryWebhook('#templatePane').innerHeight() /2.2 );
			  	jQueryWebhook('#currentTemplateRendered').outerHeight(jQueryWebhook('#templatePane').innerHeight() /2.2 );
			  	jQueryWebhook('.maxtoggle').toggle();
			  	this.showCentered();
			  	
			  },
			  
			  restoreDialog : function() {
			  	jQueryWebhook('#editWebHookDialog').innerWidth(webhookDialogWidth);
			  	jQueryWebhook('#editWebHookDialog').innerHeight(webhookDialogHeight);
			  	jQueryWebhook('#templatePane').outerHeight(templatePaneOuterHeight);
			  	jQueryWebhook('#currentTemplateRaw').outerHeight(jQueryWebhook('#templatePane').innerHeight() /2.2 );
			  	jQueryWebhook('#currentTemplateRendered').outerHeight(jQueryWebhook('#templatePane').innerHeight() /2.2 );
			  	jQueryWebhook('.maxtoggle').toggle();
			  	this.showCentered();
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
			        jQueryWebhook('li.tab').removeAttr('style');
			        jQueryWebhook("#hookPaneTab").animate({
			        	backgroundColor: "#eecccc"
			        }, 500);
			      },

			      onEmptyPayloadFormatError : function(elem) {
			        $("error_payloadFormat").innerHTML = elem.firstChild.nodeValue;
			        that.highlightErrorField($('payloadFormatTable'));
			        jQueryWebhook('li.tab').removeAttr('style');
			        jQueryWebhook("#hookPaneTab").animate({
			        	backgroundColor: "#eecccc"
			        }, 500);
			      },
			      
			      onEmptyAuthParameterError : function(elem) {
			        $("error_authParameter").innerHTML = elem.firstChild.nodeValue;
			        that.highlightErrorField($('extraAuthParameters'));
			        jQueryWebhook('li.tab').removeAttr('style');
			        jQueryWebhook("#extrasPaneTab").animate({
			        	backgroundColor: "#eecccc"
			        }, 500);
			      },

			      onCompleteSave : function(form, responseXML, err) {
			    	BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
			        form.enable();
			        if (!err) {
			        	jQueryWebhook('li.tab').removeAttr('style');	
			            jQueryWebhook("#viewRow_" + jQueryWebhook('#webHookId').val()).animate({
			                backgroundColor: "#cceecc"
			            }, 500 );	
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
    	<script type=text/javascript src="..${jspHome}WebHook/highlight/highlight.pack.js"></script>
    </jsp:attribute>
</bs:page>
