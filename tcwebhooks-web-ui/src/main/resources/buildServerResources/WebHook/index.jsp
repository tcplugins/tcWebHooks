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
		jQueryWebhook(document).ready( function() {
			//jQueryWebhook("input.buildState
		});

		function showEditForm(formID){
			jQueryWebhook('tr#viewRow_' + formID).hide();
			jQueryWebhook('tr#editRow_' + formID).show();
		}

		function selectAllBuildStates(){
			//var state = jQueryWebhook('#selectAll').is(':checked');
			//jQueryWebhook('#webHookFormContents input.buildState').each(function(i){
			//	this.checked = state;
			//});
			doExtraCompleted();
		}

		function selectBuildState(){
			//if(jQueryWebhook('#webHookFormContents input.buildState:checked').length == 6){
			//	jQueryWebhook('#selectAll').attr('checked', true);
			//} else {
			//	jQueryWebhook('#selectAll').attr('checked', false);
			//}
			doExtraCompleted();
		}

		function doExtraCompleted(){
			if(jQueryWebhook('#BuildSuccessful').is(':checked')){
				jQueryWebhook('.onBuildFixed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').removeAttr('disabled');
			} else {
				jQueryWebhook('.onBuildFixed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFixed td input').attr('disabled', 'disabled');
			} 
			if(jQueryWebhook('#BuildFailed').is(':checked')){
				jQueryWebhook('.onBuildFailed').removeClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').removeAttr('disabled');
			} else {
				jQueryWebhook('.onBuildFailed').addClass('onCompletionDisabled');
				jQueryWebhook('tr.onBuildFailed td input').attr('disabled', 'disabled');
			}
			//selectBuildState(); 
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
			
			/*
		    jQueryWebhook('.checkall').on('click', function () {
		        jQueryWebhook(this).closest('fieldset').find(':checkbox').prop('checked', this.checked);
		    });
		
			jQueryWebhook.each('.buildType_single', function(){
				if (!jQueryWebhook(this).is(':checked'){
					break;
				} else {
					
				}
				
			}); */
		}
		
		function doAddBuildTypes(id){
			jQueryWebhook('#buildList').empty();
			jQueryWebhook.each(ProjectBuilds, function() {
				 if (jQueryWebhook.inArray(this.buildTypeId, jQueryWebhook('#buildTypes_'+id).val().split(','))){
				 alert("In array " + this.buildTypeId + " : " + jQueryWebhook('#buildTypes_'+id).val().split(',')); 
				 	 jQueryWebhook('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input checked onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
				 } else {
				 alert("Not In array " + this.buildTypeId); 
				 	 jQueryWebhook('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
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

		BS.EditWebHookDialog = OO.extend(BS.AbstractModalDialog, {
			  getContainer : function() {
			    return $('editWebHookDialog');
			  },

			  showDialog : function(id) {
				BS.WebHookForm.clearErrors();
			    
			    $('webHookUrl').value = $('url_'+id).value;
			    jQueryWebhook('#webHooksEnabled').attr('checked', jQueryWebhook('#webHooksEnabled_'+id).is(':checked'));
			    //jQueryWebhook('#selectAll').attr('checked', jQueryWebhook('#selectAll_'+id).is(':checked'));
			    jQueryWebhook('#BuildStarted').attr('checked', jQueryWebhook('#BuildStarted_'+id).is(':checked'));
			    jQueryWebhook('#BuildFinished').attr('checked', jQueryWebhook('#BuildFinished_'+id).is(':checked'));
			    jQueryWebhook('#StatusChanged').attr('checked', jQueryWebhook('#StatusChanged_'+id).is(':checked'));
			    jQueryWebhook('#BuildInterrupted').attr('checked', jQueryWebhook('#BuildInterrupted_'+id).is(':checked'));
			    jQueryWebhook('#BeforeFinished').attr('checked', jQueryWebhook('#BeforeFinished_'+id).is(':checked'));
			    jQueryWebhook('#ResponsibilityChanged').attr('checked', jQueryWebhook('#ResponsibilityChanged_'+id).is(':checked'));
			    jQueryWebhook('#BuildSuccessful').attr('checked', jQueryWebhook('#BuildSuccessful_'+id).is(':checked'));
			    jQueryWebhook('#BuildFixed').attr('checked', jQueryWebhook('#BuildFixed_'+id).is(':checked'));
			    jQueryWebhook('#BuildFailed').attr('checked', jQueryWebhook('#BuildFailed_'+id).is(':checked'));
			    jQueryWebhook('#BuildBroken').attr('checked', jQueryWebhook('#BuildBroken_'+id).is(':checked'));
			    //jQueryWebhook('#payloadFormat').attr('checked', jQueryWebhook('#payloadFormat_'+id).is(':checked'));
			    selectCorrectRadio(id);
			    //jQueryWebhook('#payloadFormatNVPAIRS').attr('checked', jQueryWebhook('#payloadFormatNVPAIRS_'+id).is(':checked'));
			    
			    $('webHookId').value = id;
			    
			    doExtraCompleted();
			    doAddBuildTypes(id);
			    
			    var title = id == "new" ? "Add New" : "Edit";
			    title += " WebHook";

			    $('webHookDialogTitle').innerHTML = title;
			    
				jQueryWebhook('#tab-container').easytabs();

			    this.showCentered();
			    
			    jQueryWebhook('#buildList').innerHeight(jQueryWebhook('#hookPane').innerHeight()); 
			    
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
			        //that.updateParamLists();
			        //jQueryWebhook('#viewRow_' + paramId).hide();
			        //BS.util.hide('viewRow_' + paramId);
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
	          new BS.ajaxUpdater(this, '<c:url value="/webhooks/settingsList.html?projectId=${projectId}&refresh=true"/>', {
	            evalScripts: true
	          });
	        }

	</script>
    </jsp:attribute>
</bs:page>
