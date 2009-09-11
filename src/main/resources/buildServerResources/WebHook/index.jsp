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
		  	{title: "${projectName}", url: '<c:url value="/project.html?projectId=${projectId}"/>'},
		  </c:if>
          {title: "${title}", selected:true}
        ];
    
      </script>
    </jsp:attribute>
    
    
      
    <jsp:attribute name="body_include">
    <script type=text/javascript src="..${jspHome}WebHook/jquery-1.3.2.min.js"></script>
    <script type=text/javascript>
		jQuery.noConflict();
		jQuery(document).ready( function() {
			//jQuery("input.buildState
		});

		function showEditForm(formID){
			jQuery('tr#viewRow_' + formID).hide();
			jQuery('tr#editRow_' + formID).show();
		}

		function selectAllBuildStates(){
			var state = jQuery('#selectAll').is(':checked');
			jQuery('#webHookFormContents input.buildState').each(function(i){
				this.checked = state;
			});
		}

		function selectBuildState(){
			if(jQuery('#webHookFormContents input.buildState:checked').length == 6){
				jQuery('#selectAll').attr('checked', true);
			} else {
				jQuery('#selectAll').attr('checked', false);
			}
		}

		function selectCorrectRadio(id){
			jQuery('#webHookFormContents input.payloadFormat').each(function(i){
				if(this.value == jQuery('#payloadFormat_'+id).val()){
					this.checked = true;
				} else {
					this.checked = false;
				}
			});
		}

		BS.EditWebHookDialog = OO.extend(BS.AbstractModalDialog, {
			  getContainer : function() {
			    return $('editParameterDialog');
			  },

			  showDialog : function(id) {
			    BS.BuildParamsForm.clearErrors();

				//alert($('form_'+id).innerHTML);
				//Element.replace('webHookFormContents', $('form_' + id).innerHTML);
			    //$('modalDialogBody').replace($('form_' + id).innerHTML);
			    
			    $('webHookUrl').value = $('url_'+id).value;
			    jQuery('#webHooksEnabled').attr('checked', jQuery('#webHooksEnabled_'+id).is(':checked'));
			    jQuery('#selectAll').attr('checked', jQuery('#selectAll_'+id).is(':checked'));
			    jQuery('#BuildStarted').attr('checked', jQuery('#BuildStarted_'+id).is(':checked'));
			    jQuery('#BuildFinished').attr('checked', jQuery('#BuildFinished_'+id).is(':checked'));
			    jQuery('#StatusChanged').attr('checked', jQuery('#StatusChanged_'+id).is(':checked'));
			    jQuery('#BuildInterrupted').attr('checked', jQuery('#BuildInterrupted_'+id).is(':checked'));
			    jQuery('#BeforeFinished').attr('checked', jQuery('#BeforeFinished_'+id).is(':checked'));
			    jQuery('#ResponsibilityChanged').attr('checked', jQuery('#ResponsibilityChanged_'+id).is(':checked'));
			    //jQuery('#payloadFormat').attr('checked', jQuery('#payloadFormat_'+id).is(':checked'));
			    selectCorrectRadio(id);
			    //jQuery('#payloadFormatNVPAIRS').attr('checked', jQuery('#payloadFormatNVPAIRS_'+id).is(':checked'));
			    
			    $('webHookId').value = id;
			    
			    
			    //$('BuildStarted').checked = $('BuildStarted_'+id).checked;


			    //BuildStarted_${hook.uniqueKey}
			    //$('currentName').value = name;
			    //$('parameterName').value = name;
			    //$('parameterValue').value = value;
			    //$('systemProperty').value = systemProperty;

			    var title = id == "new" ? "Add New" : "Edit";
			    title += " WebHook";

			    $('webHookDialogTitle').innerHTML = title;

			    this.showCentered();
			    $('webHookUrl').focus();
			  },

			  cancelDialog : function() {
			    this.close();
			  }
			});

		BS.SaveWebHookListener = OO.extend(BS.ErrorsAwareListener, {
			  onSaveProjectErrorError : function(elem) {
			    alert(elem.firstChild.nodeValue);
			  },

			  onProjectNotFoundError : function(elem) {
			    document.location.reload();
			  },

			  onBuildTypeNotFoundError : function(elem) {
			    document.location.reload();
			  },

			  onMaxNumberOfBuildTypesReachedError: function(elem) {
			    alert(elem.firstChild.nodeValue);
			  }
			});
					
		
		BS.BaseSaveWebHookListener = OO.extend(BS.SaveWebHookListener, {
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

			    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.BaseSaveWebHookListener,
			 {
			      onEmptyWebHookUrlError : function(elem) {
				      alert(elem.firstChild.nodeValue);
			        $("error_webHookUrl").innerHTML = elem.firstChild.nodeValue;
			        that.highlightErrorField($('webHookUrl'));
			      },

			      onCompleteSave : function(form, responseXML, err) {
			        BS.SaveBuildTypeListener.onCompleteSave(form, responseXML, err);

			        form.enable();
			        if (!err) {
			          //that.updateParamLists();
			          $('systemParams').updateContainer();
			          BS.EditParameterDialog.close();
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
			        //jQuery('#viewRow_' + paramId).hide();
			        //BS.util.hide('viewRow_' + paramId);
			        $('systemParams').updateContainer();
			        BS.EditParameterDialog.close();
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
			<c:when test="${hasPermission}">
			<%@ include file="webHookInclude.jsp" %>
			</c:when>
			<c:otherwise>
				<strong>You must have Project Administrator permission to edit WebHooks</strong>
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
