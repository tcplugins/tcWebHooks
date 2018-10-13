WebHooksPlugin = {
	handleAjaxError: function(dialog, response) {
		dialog.cleanErrors();
		if (response.status === 422) {
			if (response.responseJSON.errored) {
				$j.each(response.responseJSON.errors, function(index, errorMsg){
					dialog.ajaxError(errorMsg)
				});
			}
		} else {
		  alert(response);
		}
	},
    showAddDialog: function() {
		WebHooksPlugin.EditWebHookDialog.showDialog("Add Web Hook", 'addWebHook', 'new', '#hookPane');
    },
    showEditDialog: function(data, tab) {
    	WebHooksPlugin.EditWebHookDialog.showDialog("Edit Web Hook", 'updateWebHook', data, tab);
    },
	showDeleteDialog: function(paramId){
		WebHooksPlugin.DeleteWebHookDialog.showDialog("Delete Web Hook", 'deleteWebHook', paramId);
	},

    DeleteWebHookDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('deleteWebHookDialog');
    	},
    	
    	formElement: function () {
    		return $('deleteWebHookForm');
    	},
    	
    	showDialog: function (title, action, id) {
    		$j("#editWebHookForm input[id='webHookId']").val("none"); // Unset the edit id, so that it doesn't get animated by delete.
    		$j("input[id='webHookaction']").val(action);
    		$j(".dialogTitle").html(title);
    		this.cleanFields(id);
    		this.cleanErrors();
    		this.showCentered();
			$j("#viewRow_" + id).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
    	},
    	
		cancelDialog : function() {
		    this.close();
		    $j('li.tab').removeAttr('style');
	        $j("#viewRow_" + $j("#deleteWebHookForm input[id='webHookId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
		},
    	
    	cleanFields: function (id) {
    		$j("#deleteWebHookForm input[id='webHookId']").val(id);
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#deleteWebHookForm .error").remove();
    	},
    	
    	error: function($element, message) {
    		var next = $element.next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$element.after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	ajaxError: function(message) {
    		var next = $j("#ajaxWebHookDeleteResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxWebHookDeleteResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		var dialog = this;
    		var webHookId = $j("#deleteWebHookForm input[id='webHookId']").val()
    	    var url = this.formElement().action + "&submitAction=removeWebHook&removedWebHookId=" + webHookId;
    		
    		$j.ajax ({
    			url: url,
    			type: "POST",
    			dataType: "xml",
    			success: function (xml) {
    				var isErrored = false;
				    $j(xml).find("error").each(function(error, thing){
					    isErrored = true;
					    dialog.ajaxError(thing.textContent);
				    });
    				if (! isErrored) { 
    					dialog.close();
    					
    					// Animate the removal of the webhook table row. 
    			        $j("#viewRow_" + $j("#deleteWebHookForm input[id='webHookId']").val())
    			            .children('td, th')
    			            .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
    			            .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
    			            .children()
    			            .slideUp(function() { 
    			            	$j(this).closest('tr').remove();
    			            	$('systemParams').updateContainer();
    			            }); 
    				}
    			},
    			error: function (response) {
    				console.log(response);
    				ajaxError(response);
    			}
    		});
    		
    		return false;
    	}
    })),	
	
    EditWebHookDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('editWebHookDialog');
        },

        formElement: function () {
            return $('editWebHookForm');
        },
        
		cancelDialog : function() {
		    this.close();
		    $j('li.tab').removeAttr('style');
	        $j("#viewRow_" + $j("#editWebHookForm input[id='webHookId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
		},

        showDialog: function (title, action, id, tab) {
        	
        	this.formElement().submitAction.value = action;
            $j(".dialogTitle").html(title);
            this.cleanFields(id);
            this.cleanErrors();
            
		    populateWebHookDialog(id);
		    doExtraCompleted();
		    
		    if (webhookDialogWidth < 0){
		    	webhookDialogWidth = $j('#editWebHookDialog').innerWidth();
		    } else {
		    	$j('#editWebHookDialog').innerWidth(webhookDialogWidth);
		    }
		    
		    this.showCentered();
		    $j('#hookPane').innerHeight($j('#templatePane').innerHeight());
		    $j('#buildPane').innerHeight($j('#templatePane').innerHeight());
		    $j('#extrasPane').innerHeight($j('#templatePane').innerHeight());
			$j('#tab-container').easytabs('select', tab);
			
			if (webhookDialogHeight < 0){
		    	webhookDialogHeight = $j('#editWebHookDialog').innerHeight();
		    	templatePaneOuterHeight = $j('#templatePane').outerHeight();
		    }
		    
		    $j('#webHookUrl').focus();            
            this.showCentered();
        },

        cleanFields: function (data) {
        	$j("#webhookPreviewRendered").empty();
        	$j("#webhookDialogAjaxResult").empty();
        	$j('#webhookTestProgress').css("display","none");
            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#webhookDialogAjaxResult .error").remove();
        },

        error: function($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },
        
        ajaxError: function(message) {
        	var next = $j("#webhookDialogAjaxResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#webhookDialogAjaxResult").after("<p class='error'>" + message + "</p>");
        	}
        },

        doValidate: function() {
            var errorFound = false;

            var name = $j('input[id="debrepo.name"]');
            if (name.val() == "") {
                this.error(name, "Please set the repository name");
                errorFound = true;
            }

            return !errorFound;
        },
		
		doPost: function() {
		    this.formElement().payloadTemplate.value = lookupTemplate(this.formElement().payloadFormatHolder.value);
		    this.formElement().payloadFormat.value = lookupFormat(this.formElement().payloadFormatHolder.value);
		    var dialog = this;

		    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener,
		 	{
		      onEmptyWebHookUrlError : function(elem) {
		        $("error_webHookUrl").innerHTML = elem.firstChild.nodeValue;
		        dialog.highlightErrorField($('webHookUrl'));
		        $j('li.tab').removeAttr('style');
		        $j("#hookPaneTab").animate({
		        	backgroundColor: "#eecccc"
		        }, 500);
		      },

		      onEmptyPayloadFormatError : function(elem) {
		        $("error_payloadFormat").innerHTML = elem.firstChild.nodeValue;
		        dialog.highlightErrorField($('payloadFormatTable'));
		        $j('li.tab').removeAttr('style');
		        $j("#hookPaneTab").animate({
		        	backgroundColor: "#eecccc"
		        }, 500);
		      },
		      
		      onEmptyAuthParameterError : function(elem) {
		        $("error_authParameter").innerHTML = elem.firstChild.nodeValue;
		        dialog.highlightErrorField($('extraAuthParameters'));
		        $j('li.tab').removeAttr('style');
		        $j("#extrasPaneTab").animate({
		        	backgroundColor: "#eecccc"
		        }, 500);
		      },

		      onCompleteSave : function(form, responseXML, err) {
		    	BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
		        form.enable();
		        if (!err) {
		        	var webHookId = "unknown"; 
		        	$j(responseXML).find("webhook").each(function(error, webhook){
		        		$j("#editWebHookForm input[id='webHookId']").val(webhook.id);
		        		console.log(webhook.id);
				    });
		        	$j('li.tab').removeAttr('style');	
		            $j("#viewRow_" + $j("#editWebHookForm input[id='webHookId']").val()).animate({
		                backgroundColor: "#cceecc"
		            }, 500 );	
		            $('systemParams').updateContainer();
		            dialog.close();
		        }
		      }
		    }));

		    return false;			

		},
		
		executeWebHook: function() {
			var dialog = this;
			if (
					$j("#webhookPreviewBuildEvent").val() &&
					$j("#webhookPreviewBuildId").val() &&
					$j("#payloadFormatHolder").val()
				)
			{
				var selectedBuildState = $j('#webhookPreviewBuildEvent').val();
				var selectedBuildId = $j('#webhookPreviewBuildId').val();
				if (selectedBuildId === "") {
					$j("#webhookDialogAjaxResult").empty().append("Please select a Build first");
				} else {
					$j("#webhookDialogAjaxResult").empty();
					$j('#webhookTestProgress').css("display","block");
					$j.ajax ({
						url: "testWebHook.html?action=execute",
						type: "POST",
						dataType: 'json',
						headers : {
							'Content-Type' : 'application/json',
							'Accept' : 'application/json'
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
										    "BEFORE_BUILD_FINISHED" : false,
										    "RESPONSIBILITY_CHANGED" : false,
											"BUILD_FIXED" : true,
										    "BUILD_INTERRUPTED" : false
									    }
									}),
						success:function(response){
							$j('#webhookTestProgress').css("display","none");
							dialog.cleanErrors();
							var ul = $j('<ul>');
							
							if (response.error) {
								ul.append($j('<li/>').html("Error: " + response.error.message + " (" + response.error.errorCode + ")"));
							} else {
								ul.append($j('<li/>').html("Success: " + response.statusReason + " (" + response.statusCode + ")"));
							}
							ul.append($j('<li/>').html("URL: " + response.url));
							ul.append($j('<li/>').html("Duration: " + response.executionTime + " @ " + moment(response.dateTime, moment.ISO_8601).format("dddd, MMMM Do YYYY, h:mm:ss a")));
							
							$j("#webhookDialogAjaxResult").empty().append(ul.html());
							console.log(response);
						},
						error: function (response) {
							$j('#webhookTestProgress').css("display","none");
							console.log(response);
							WebHooksPlugin.handleAjaxError(dialog, response);
						}
					});
				}	
			} else {
				$j("#webhookDialogAjaxResult").empty().append("Please select a Build first");
			}
			return false;
		}

    }))
};

function populateBuildHistoryAjax(locator) {
	$j('#webhookRendered').empty();
	
   		if (!locator) { // We got a null or undefined locator. Empty the build list and return
			$j("#webhookPreviewBuildId").empty();
   			console.log("populateBuildHistoryAjax() : locator is null. This should not happen");
			return;
		}
   		
   		if (locator === 'project:_Root,') {
   			locator = '';
   		}
   		
		$j("#webhookPreviewBuildId").empty().append($j('<option></option>').val(null).html("Loading build history..."))
		$j.ajax ({
			url: window['base_uri'] + '/app/rest/builds?locator=' + locator 
					+ "state:finished&fields=build(id,number,status,finishDate,buildType(id,name))",
			type: "GET",
			headers : {
				'Accept' : 'application/json'
			},
			success: function (response) {
				var myselect = $j('<select>');
				myselect.append( $j('<option></option>').val(null).html("Choose a Build...") );
				$j(response.build).each(function(index, build) {
					//console.log(build);
					var desc = build.buildType.name 
							  + "#" + build.number 
							  + " - " + build.status + " ("
							  + moment(build.finishDate, moment.ISO_8601).fromNow()
							  + ")";
					//console.log(desc);
					myselect.append( $j('<option></option>').val(build.id).html(desc) );
				});
				$j("#webhookPreviewBuildId").empty().append(myselect.html());
			},
			error: function (response) {
				if (response.status == 404) {
					$j("#webhookPreviewBuildId").empty().append(
							$j('<option></option>').val(null).html("No builds found. Choose a different project")
						);
				} else {
    				console.log(response);
    				alert(response);
				}
			}
		});

}


function selectBuildState(){
	doExtraCompleted();
}

function doExtraCompleted(){
	if($j('#buildSuccessful').is(':checked')){
		$j('.onBuildFixed').removeClass('onCompletionDisabled');
		$j('tr.onBuildFixed td input').prop('disabled', false);
	} else {
		$j('.onBuildFixed').addClass('onCompletionDisabled');
		$j('tr.onBuildFixed td input').prop('disabled', true);
	} 
	if($j('#buildFailed').is(':checked')){
		$j('.onBuildFailed').removeClass('onCompletionDisabled');
		$j('tr.onBuildFailed td input').prop('disabled', false);
	} else {
		$j('.onBuildFailed').addClass('onCompletionDisabled');
		$j('tr.onBuildFailed td input').prop('disabled', true);
	}
}

function toggleAllBuildTypesSelected(){
	$j.each($j('.buildType_single'), function(){
		$j(this).prop('checked', $j('input.buildType_all').is(':checked'))
	});
	updateSelectedBuildTypes();
}

function updateSelectedBuildTypes(){
	var subText = "";
    if($j('#buildTypeSubProjects').is(':checked')){
    	subText = " &amp; sub-projects";
    }

	if($j('#webHookFormContents input.buildType_single:checked').length == $j('#webHookFormContents input.buildType_single').length){
		$j('input.buildType_all').prop('checked', true);
		$j('span#selectedBuildCount').html("all" + subText);
	} else {
		$j('input.buildType_all').prop('checked', false);
		$j('span#selectedBuildCount').html($j('#webHookFormContents input.buildType_single:checked').length + subText);
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
		$j('#extraAuthType').val(webhookObj.authConfig.type);
		$j('#extraAuthParameters > tbody').empty();
		$j('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip +'">Preemptive</label></td>' + 
															 '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip +'" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
		$j('#extraAuthPreemptive').prop('checked', webhookObj.authConfig.preemptive);
		$j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[webhookObj.authConfig.type].parameters, function(index, paramObj){
			$j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
		});
	} else {
		$j('#extraAuthType').val("");
		$j('#extraAuthParameters > tbody').empty();
	}					
}

function populateWebHookAuthExtrasPaneFromChange(webhookObj){
	var authType = $j('#extraAuthType').val();
	if (authType === ''){
		$j('#extraAuthParameters > tbody').empty();
	} else {
		$j('#extraAuthParameters > tbody').empty();
		$j('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip +'">Preemptive</label></td>' + 
															 '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip +'" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
		if (webhookObj.hasOwnProperty("authConfig") && webhookObj.authConfig.type == authType){
			$j('#extraAuthPreemptive').prop('checked', webhookObj.authConfig.preemptive);
			$j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function(index, paramObj){
				$j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
			});
		} else {
			$j('#extraAuthPreemptive').prop('checked', webhookObj, true);
			$j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function(index, paramObj){
				$j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, false));
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
			+ paramObj.key + '" ' + value + 'class="authParameterValue authParameterItemValue"></td></tr>';
			
}

function populateWebHookDialog(id){
	populateBuildHistory();
	$j('#buildList').empty();
	$j.each(ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList, function(webHookKey, webhook){
		if (id === webHookKey){
			
			$j("#viewRow_" + webhook.uniqueKey).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
			
			$j("#editWebHookForm input[id='webHookId']").val(webhook.uniqueKey);	
			$j('#webHookUrl').val(webhook.url);
		    $j('#webHooksEnabled').prop('checked', webhook.enabled);
		    $j.each(webhook.states, function(name, value){
		    	$j('#' + value.buildStateName).prop('checked', value.enabled);
		    });
		    
			$j('#webHookFormContents select#payloadFormatHolder').val(webhook.payloadTemplate + "_" + webhook.payloadFormat).change();
			
			$j('#buildTypeSubProjects').prop('checked', webhook.subProjectsEnabled);
			$j.each(webhook.builds, function(){
				 if (this.enabled){
			 	 	$j('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input checked onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
				 } else {
				 	 $j('#buildList').append('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">' + this.buildTypeName + '</label></p>');
				 }
			});
			
			populateWebHookAuthExtrasPane(webhook);
			$j('select#extraAuthType').change(function() {
				populateWebHookAuthExtrasPaneFromChange(webhook);
			});
			if ($j('#payloadFormatHolder').val()) {
				$j('#currentTemplateName').html(lookupTemplateName($j('#payloadFormatHolder').val()));
			} else {
				$j('#currentTemplateName').html("&nbsp;");
			}
		}
	});
	updateSelectedBuildTypes();
	renderPreviewOnChange(); // Empty the div if no build selected.
	
}

function lookupTemplateName(templateFormatCombinationKey){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateFormatCombinationKey === templateKey){
			name = template.templateDescription;
			return false;
		}
	});
	return name;
}

function lookupTemplate(templateFormatCombinationKey){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateFormatCombinationKey === templateKey){
			name = template.templateId;
			return false;
		}
	});
	return name;
}

function lookupFormat(templateFormatCombinationKey){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateFormatCombinationKey === templateKey){
			name = template.formatShortName;
			return false;
		}
	});
	return name;
}

function lookupAuthType(authTypeValue) {
	if (authTypeValue) {
		return authTypeValue;
	}
	return null;
}

function lookupAuthEnabled(authTypeValue) {
	if (authTypeValue) {
		return true;
	}
	return false;
}

function lookupAuthParameters(authTypeValue, webHookForm) {
	var authParams = {};
	if (! lookupAuthEnabled(authTypeValue)) {
		return authParams;
	}
	$j.each(webHookForm, function(index, item) {
		var mySubStringOffSet = item.name.indexOf("extraAuthParam_");
		if (mySubStringOffSet != -1) {
			authParams[item.name.substring(15)] = item.value;
		}
	});
	return authParams;
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
	$j.each(webhookItems, function(webHookKey, webhook){
		if ('new' !== webHookKey){
			var isNew = false;
			var newRow = $j('.webHookRowTemplate')
							.clone()
							.prop("id", "viewRow_" + webhook.uniqueKey)
							.removeClass('webHookRowTemplate')
							.addClass('webHookRow');
			
			console.log(webhook.uniqueKey + " :: " + $j("#editWebHookForm input[id='webHookId']").val() );
			console.log("addWebHook :: " + $j("#editWebHookForm input[id='submitAction']").val() );
			
			if (webhook.uniqueKey === $j("#editWebHookForm input[id='webHookId']").val() && "addWebHook" === $j("#editWebHookForm input[id='submitAction']").val() ){
				isNew = true;
				newRow.hide();
			}
			newRow.appendTo('#webHookTable > tbody');
			
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemUrl").html(htmlEscape(webhook.url)).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey, '#hookPane');});
			if (webhook.payloadTemplate === 'none') {
				$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").html(webhook.payloadFormatForWeb);
			} else {
				$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").html("<a href='template.html?template=" + webhook.payloadTemplate +"'>" + webhook.payloadFormatForWeb + "</a>");
			}
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEvents").html(webhook.enabledEventsListForWeb).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey,'#hookPane');});
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemBuilds").html(webhook.enabledBuildsListForWeb).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey, '#buildPane');});
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEdit > a").click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey,'#hookPane');});
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemDelete > a").click(function(){WebHooksPlugin.showDeleteDialog(webhook.uniqueKey);});
			
			if (webhook.uniqueKey === $j("#editWebHookForm input[id='webHookId']").val()){
				$j("#viewRow_" + webhook.uniqueKey).css('background-color', '#cceecc');
				$j("#viewRow_" + webhook.uniqueKey).fadeIn(500);
			}
			
		}
		if (isNew) {
			newRow.slideDown("slow", function(){
	            $j("#viewRow_" + webhook.uniqueKey).animate({
	                backgroundColor: "#ffffff"
	            }, 2500 );				
			});
		} else if (webhook.uniqueKey === $j("#editWebHookForm input[id='webHookId']").val()){
            $j("#viewRow_" + webhook.uniqueKey).animate({
                backgroundColor: "#ffffff"
            }, 1500 );
		}
	});
	
	//populateBuildHistory();
}

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
	    return $j('WebHookForm');
	  },

	  saveWebHook : function() {
	    //this.formElement().submitAction.value = 'updateWebHook';
	    this.formElement().payloadTemplate.value = lookupTemplate(this.formElement().payloadFormatHolder.value);
	    this.formElement().payloadFormat.value = lookupFormat(this.formElement().payloadFormatHolder.value);
	    var dialog = this;

	    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener,
	 	{
	      onEmptyWebHookUrlError : function(elem) {
	        $("error_webHookUrl").innerHTML = elem.firstChild.nodeValue;
	        dialog.highlightErrorField($('webHookUrl'));
	        $j('li.tab').removeAttr('style');
	        $j("#hookPaneTab").animate({
	        	backgroundColor: "#eecccc"
	        }, 500);
	      },

	      onEmptyPayloadFormatError : function(elem) {
	        $("error_payloadFormat").innerHTML = elem.firstChild.nodeValue;
	        dialog.highlightErrorField($('payloadFormatTable'));
	        $j('li.tab').removeAttr('style');
	        $j("#hookPaneTab").animate({
	        	backgroundColor: "#eecccc"
	        }, 500);
	      },
	      
	      onEmptyAuthParameterError : function(elem) {
	        $("error_authParameter").innerHTML = elem.firstChild.nodeValue;
	        dialog.highlightErrorField($('extraAuthParameters'));
	        $j('li.tab').removeAttr('style');
	        $j("#extrasPaneTab").animate({
	        	backgroundColor: "#eecccc"
	        }, 500);
	      },

	      onCompleteSave : function(form, responseXML, err) {
	    	BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
	        form.enable();
	        if (!err) {
	        	$j('li.tab').removeAttr('style');	
	            $j("#viewRow_" + $j("#editWebHookForm input[id='webHookId']").val()).animate({
	                backgroundColor: "#cceecc"
	            }, 500 );	
	            $('systemParams').updateContainer();
	            BS.EditWebHookDialog.close();
	        }
	      }
	    }));

	    return false;
	  }

	});