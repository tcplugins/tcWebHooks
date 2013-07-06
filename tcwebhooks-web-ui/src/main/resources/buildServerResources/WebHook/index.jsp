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
    /css/addSidebar.css
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
		});

		function selectBuildState(){
			doExtraCompleted();
		}

		function doExtraCompleted(){
			if(jQueryWebhook('#buildSuccessful').is(':checked')){
				jQueryWebhook('.onBuildFixed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').removeAttr('disabled');
			} else {
				jQueryWebhook('.onBuildFixed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').attr('disabled', 'disabled');
			} 
			if(jQueryWebhook('#buildFailed').is(':checked')){
				jQueryWebhook('.onBuildFailed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').removeAttr('disabled');
			} else {
				jQueryWebhook('.onBuildFailed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').attr('disabled', 'disabled');
			}
		}
		
		function toggleAllBuildTypesSelected(){
			jQueryWebhook.each(jQueryWebhook('.buildType_single'), function(){
				jQueryWebhook(this).attr('checked', jQueryWebhook('input.buildType_all').is(':checked'))
			});
			updateSelectedBuildTypes();
		}
		
		function updateSelectedBuildTypes(){
		
			if(jQueryWebhook('#webHookFormContents input.buildType_single:checked').length == jQueryWebhook('#webHookFormContents input.buildType_single').length){
				jQueryWebhook('input.buildType_all').attr('checked', true);
				jQueryWebhook('span#selectedBuildCount').html("all");
			} else {
				jQueryWebhook('input.buildType_all').attr('checked', false);
				jQueryWebhook('span#selectedBuildCount').html(jQueryWebhook('#webHookFormContents input.buildType_single:checked').length);
			}

		}
		
		function populateWebHookDialog(id){
			jQueryWebhook('#buildList').empty();
			jQueryWebhook.each(ProjectBuilds.projectWebhookConfig.webHookList, function(thing, config){
				if (id === config[0]){
					var webhook = config[1];
				
					jQueryWebhook('#webHookId').val(webhook.uniqueKey);	
					jQueryWebhook('#webHookUrl').val(webhook.url);
				    jQueryWebhook('#webHooksEnabled').attr('checked', webhook.enabled);
				    jQueryWebhook.each(webhook.states, function(name, value){
				    	console.log(value.buildStateName, value.enabled);
				    	jQueryWebhook('#' + value.buildStateName).attr('checked', value.enabled);
				    });
				    
				    jQueryWebhook('#webHookFormContents input.payloadFormat').each(function(i){
						if(this.value === webhook.payloadFormat){
							this.checked = true;
						} else {
							this.checked = false;
						}
					});
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
		}

		function selectCorrectRadio(id){
			jQueryWebhook('#webHookFormContents input.payloadFormat').each(function(i){
				if(this.value == jQueryWebhook('#payloadFormat_'+id).val()){
					this.checked = true;
				} else {
					this.checked = false;
				}
			});
		}
		
		function addWebHooksFromJsonCallback(){
			jQueryWebhook.each(ProjectBuilds.projectWebhookConfig.webHookList, function(thing, config){
				if ('new' !== config[0]){
					var webhook = config[1];
					console.log(webhook.enabledEventsListForWeb);
					jQueryWebhook('.webHookRowTemplate')
									.clone()
									.removeAttr("id")
									.attr("id", "viewRow_" + webhook.uniqueKey)
									.removeClass('webHookRowTemplate')
									.addClass('webHookRow')
									.appendTo('#webHookTable > tbody');
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemUrl").html(webhook.url).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey, '#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").html(webhook.payloadFormatForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEvents").html(webhook.enabledEventsListForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemBuilds").html(webhook.enabledBuildsListForWeb).click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey, '#buildPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEdit > a").click(function(){BS.EditWebHookDialog.showDialog(webhook.uniqueKey,'#hookPane');});
					jQueryWebhook("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemDelete > a").click(function(){BS.WebHookForm.removeWebHook(webhook.uniqueKey,'#hookPane');});
					
				}
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
			    jQueryWebhook('#buildPane').innerHeight(jQueryWebhook('#hookPane').innerHeight());
				jQueryWebhook('#tab-container').easytabs('select', tab);
			    
			    $('webHookUrl').focus();
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
    
	    <h2 class="noBorder">WebHooks</h2>

  		<div id="messageArea"></div>
	    <div id="systemParams"><!--  begine systemParams div -->

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
						<strong>You must have Project Administrator permission to edit WebHooks</strong>
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
				          	<li><a href="http://blog.webhooks.org/">Jeff Lindsay's WebHooks blog</a></li>
				          	<li><a href="http://www.postbin.org/">PostBin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'DEFAULT'}">
				          <p>Further Reading:
				          <ul><li><a href="http://netwolfuk.wordpress.com/teamcity-plugins/">tcWebHooks plugin</a></li>
				          	<li><a href="http://blog.webhooks.org/">Jeff Lindsay's WebHooks blog</a></li>
				          	<li><a href="http://www.postbin.org/">PostBin</a></li>
				          </ul>	
				</c:when>
		
				<c:when test="${ShowFurtherReading == 'SINGLE'}">
				          <p>Further Reading:
				          <ul>${moreInfoText}</ul>
				</c:when>
			</c:choose>

      </div>
    </div>
    <script type=text/javascript>
	        $('systemParams').updateContainer = function() {
	          	jQueryWebhook.get("settingsList.html?projectId=${projectId}", function(data) {
	          		ProjectBuilds = data;
	          		jQueryWebhook('.webHookRow').remove();
	          		addWebHooksFromJsonCallback();
				});
	        }

	</script>
    </jsp:attribute>
</bs:page>
