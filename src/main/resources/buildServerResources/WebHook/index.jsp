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
			    //jQuery('#WebHookIsEnabled').attr('checked', jQuery('#ResponsibilityChanged_'+id).is(':checked'));
			    
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

		BS.BaseSaveWebHookListener = OO.extend(BS.SaveBuildTypeListener, {
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
			      onEmptyParameterNameError : function(elem) {
				      alert(elem.firstChild.nodeValue);
			        $("error_webHookName").innerHTML = elem.firstChild.nodeValue;
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

<%@ include file="webHookInclude.jsp" %>

        </div><!--  end systemParams div -->
      </div>
    <div id=sidebarAdmin>
      <div class=configurationSection>
      	<h2>WebHook Information</h2>
          <p>This is some more blurb about webhooks.</p>
          <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec condimentum consequat justo, nec semper lacus tristique eu. Donec eget mi ante, at dignissim orci. Nulla facilisi. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Proin vel adipiscing tortor. Donec scelerisque, tellus vitae pharetra adipiscing, odio tellus mollis ipsum, eget fermentum nisi dolor vel mi. Nunc porta imperdiet odio, ullamcorper malesuada erat blandit ut. Nulla facilisi. Aliquam mauris nisi, sollicitudin quis dictum tristique, scelerisque non libero. Curabitur suscipit, erat accumsan pretium condimentum, felis urna eleifend metus, at adipiscing justo elit vitae lorem. Sed felis sem, tincidunt ut semper sed, adipiscing eget nulla. Fusce nibh leo, molestie eget blandit ut, consectetur sit amet arcu. Sed consequat, ipsum sed bibendum adipiscing, ipsum ligula egestas neque, eu elementum est enim a ligula. Curabitur fringilla erat quis quam tincidunt dictum. In dolor nibh, adipiscing in ultrices in, viverra sed lacus. In mattis risus varius nulla facilisis convallis. Sed sit amet enim eget justo lacinia sollicitudin at a justo. Proin molestie eleifend velit vitae posuere.</p>
		  <p>Praesent porta, turpis at laoreet laoreet, metus velit accumsan mauris, eu posuere nisl velit et lorem. Nunc a justo velit, viverra dapibus purus. Maecenas tempus, erat eget molestie eleifend, magna libero elementum libero, id pulvinar orci quam nec nisl. Maecenas eget quam mi, sed pulvinar felis. Mauris eu orci id est tristique imperdiet eget in ipsum. Morbi eu mauris diam, a vulputate metus. Vestibulum pulvinar, nisi ullamcorper iaculis aliquet, justo massa luctus orci, sit amet feugiat urna mi in augue. Sed laoreet, arcu eu consequat venenatis, leo lectus porttitor massa, lacinia iaculis felis quam in eros. Curabitur ac dapibus ligula. Aliquam tristique ante mollis nibh posuere ac tincidunt nibh bibendum. Fusce ultricies dolor vitae dolor feugiat vitae blandit nisi congue. Quisque ut consequat massa. Nullam non erat at dui luctus auctor. Phasellus elementum lobortis turpis, id sollicitudin urna tempus quis. Pellentesque purus neque, gravida vel faucibus at, tincidunt ac nisl. Integer posuere, nunc vitae ullamcorper laoreet, ipsum magna semper augue, ac adipiscing nisl mi ac leo. Nullam enim eros, laoreet at congue ac, varius a urna.</p>
		  <p>Nunc sit amet tempor massa. Nullam sed lacus odio. Sed quis ullamcorper dolor. Integer ut felis leo, et laoreet nulla. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam varius consectetur justo ut imperdiet. Suspendisse porta dolor vel sapien laoreet vitae accumsan purus rutrum. Duis in mi libero. Donec cursus eleifend erat at consequat. Praesent ultricies tellus in orci viverra elementum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed bibendum lacus eu eros auctor quis pulvinar mi aliquam. Suspendisse potenti. Nam rutrum, enim eu aliquet dapibus, enim elit tempor urna, eget ornare metus est et mi.</p> 
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
