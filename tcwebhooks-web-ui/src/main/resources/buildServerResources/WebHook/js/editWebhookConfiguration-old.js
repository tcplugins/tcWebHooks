WebHooksPlugin.Configurations = {
	localStore: {
		myJson: {}
	},
	handleAjaxError: function(dialog, response) {
		dialog.cleanErrors();
		if (response.status === 422) {
			if (response.responseJSON.errored) {
				$j.each(response.responseJSON.errors, function(index, errorMsg){
					dialog.ajaxError(errorMsg)
				});
			}
		} else {
			console.log("----- begin webhooks AJAX error response -----")
			console.log(response);
			console.log("----- end webhooks AJAX error response -----")
			alert("An unexpected error occured. Please see your browser's javascript console.");
		}
	},
    showAddDialog: function() {
		WebHooksPlugin.Configurations.EditDialog.showDialog("Add Web Hook", 'addWebHook', 'new', '#hookPane');
    },
    showEditDialog: function(data, tab) {
    	WebHooksPlugin.Configurations.EditDialog.showDialog("Edit Web Hook", 'updateWebHook', data, tab);
    },
	showDeleteDialog: function(data){
		WebHooksPlugin.Configurations.DeleteDialog.showDialog("Delete Web Hook", 'deleteWebHook', data);
	},
/*
    DeleteDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('deleteWebHookDialog');
    	},

    	formElement: function () {
    		return $('deleteWebHookForm');
    	},

    	showDialog: function (title, action, data) {
    		$j("#editWebHookForm input[id='webHookId']").val("none"); // Unset the edit id, so that it doesn't get animated by delete.
    		$j("input[id='webHookaction']").val(action);
    		$j(".dialogTitle").text(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
			$j("#viewRow_" + data.webhookId).animate({
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

    	cleanFields: function (data) {
    		$j("#deleteWebHookForm input[id='webHookId']").val(data.webhookId);
    		$j("#deleteWebHookForm input[id='projectId']").val(data.projectId);

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
			var webhookId = $j("#deleteWebHookForm input[id='webHookId']").val()
			var projectId = $j("#deleteWebHookForm input[id='projectId']").val()

			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/configurations/' + projectId + '/id:' + webhookId,
				type: "DELETE",
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					let refreshDone = false;
					dialog.close();
					// Animate the removal of the webhook table row.
					// Then do the div refresh after the row is gone.
			        $j("#viewRow_" + response.id)
			            .children('td, th')
			            .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
			            .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
			            .children()
			            .slideUp(function() {
			            	if (!refreshDone) {
			            		$j(this).closest('tr').remove();
			            		$("projectWebhooksContainer").refresh();
			            		refreshDone = true;
			            	}
			            });
				},
				error: function (response) {
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
			});

    		return false;
    	}
    })),
/*
    EditDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('editWebHookDialog');
        },

        formElement: function () {
            return $('editWebHookForm');
        },

        showDialog: function (title, action, data) {

            $j("input[id='webhookProjectId']").val(data.projectId);
            $j("input[id='WebHookaction']").val(action);
            $j(".dialogTitle").text(title);
            $j("#editWebHookDialogSubmit").val(action === "addWebhook" ? "Add WebHook" : "Edit WebHook");
            this.resetAndShow(data);
            this.getWebHookData(data.projectId, data.webhookId, action);
			$j("#viewRow_" + data.webhookId).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
        },
        
        cancelDialog: function () {
        	this.close();
	        $j("#viewRow_" + $j("#editWebHookForm input[id='webhookId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
        },

        resetAndShow: function (data) {
			this.disableAndClearCheckboxes();
            this.cleanFields(data);
            this.showCentered();
        },

        cleanFields: function (data) {
        	$j("#editWebHookForm input[class='editWebHookFormField']").val("");
        	$j("#editWebHookForm")[0].reset();
        	$j("#editWebHookForm #webhookId").val(data.webhookIdId);
        	$j("#editWebHookForm #webhookProjectId").val(data.projectId);
        	//this.toggleHidden();
        	this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#editWebHookForm .error").remove();
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
        	var next = $j("#ajaxWebHookEditResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxWebHookEditResult").after("<p class='error'>" + message + "</p>");
        	}
        },

		getWebHookData: function (projectId, webhookId, action) {
			if (action === 'addWebhook') {
				WebHooksPlugin.Configurations.localStore.myJson = { "id": "_new", "projectId": projectId};
			} else {
				this.getData(projectId, webhookId, action);
			}
		},
		putWebHookData: function () {
			this.updateJsonDataFromForm();
			this.putData();
		},
		postWebHookData: function () {
			this.updateJsonDataFromForm();
			this.postData();
		},
		disableAndClearCheckboxes: function () {
			$j("#editWebHookForm input.buildState").prop("disabled", true).prop( "checked", false);
			$j("#editWebHookForm label").addClass("checkboxLooksDisabled");
		},
		disableCheckboxes: function () {
			$j("#editWebHookForm input.buildState").prop("disabled", true);
			$j("#editWebHookForm label").addClass("checkboxLooksDisabled");
		},
		enableCheckboxes: function () {
			$j("#editWebHookForm input.buildState").prop("disabled", false);
		},
		updateJsonDataFromForm: function () {
			var myJson = {};
			myJson.id = $j('#editWebHookForm #webhookId').val();
			myJson.projectId = $j('#editWebHookForm #webhookProjectId').val();
			myJson.href = $j('#editWebHookForm #webhookHref').val();
			WebHooksPlugin.Configurations.localStore.myJson = myJson;
		},
		getData: function (projectId, webhookId, action) {
			var dialog = this;
    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/configurations/' + projectId + '/id:' + webhookId + '?fields=**',
    		    type: "GET",
    		    headers : {
    		        'Accept' : 'application/json'
    		    },
    		    success: function (response) {
    				WebHooksPlugin.Configurations.localStore.myJson = response;
    				dialog.handleGetSuccess(action);
    		    },
				error: function (response) {
					console.log(response);
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
    		});
		},

		handleGetSuccess: function (action) {
			var myJson = WebHooksPlugin.Configurations.localStore.myJson;
			$j('#editWebHookParameterForm #parameterId').val(myJson.id);
			$j('#editWebHookParameterForm #parameterHref').val(myJson.href);
			$j("#editWebHookParameterForm #parameterDialogType").val(myJson.secure ? "password" : "text");
			$j("#editWebHookParameterForm #parameterDialogTypeName").val(myJson.name);
			$j("#editWebHookParameterForm #parameterDialogTypeValue").val(myJson.value);
			$j("#editWebHookParameterForm #parameterDialogVisibility").val(myJson.includedInLegacyPayloads ? "legacy" : "template");
			$j("#editWebHookParameterForm #parameterDialogTemplateEngine").val(myJson.templateEngine);
		},
		putData: function () {
			var dialog = this;
			$j.ajax ({
				url: window['base_uri'] + WebHooksPlugin.Configurations.localStore.myJson.href,
				type: "PUT",
				data: JSON.stringify(WebHooksPlugin.Configurations.localStore.myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					dialog.close();
					$("projectWebhookContainer").refresh(function() {
			            $j("#viewRow_" + response.id)
			            .css({backgroundColor: '#cceecc'})
			            .animate({
			                backgroundColor: "#ffffff"
			            }, 1500 );
					});
				},
				error: function (response) {
					console.log(response);
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
			});
		},
		postData: function () {
			var dialog = this;
			// For creating, the ID must be empty
			WebHooksPlugin.Configurations.localStore.myJson.id = "";
			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/configurations/' + WebHooksPlugin.Configurations.localStore.myJson.projectId,
				type: "POST",
				data: JSON.stringify(WebHooksPlugin.Configurations.localStore.myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					console.log(response);
					dialog.close();
					$("projectWebhooksContainer").refresh(function() {
			            $j("#viewRow_" + response.id)
			            .css({backgroundColor: '#cceecc'})
			            .animate({
			                backgroundColor: "#ffffff"
			            }, 2500 );
					});
				},
				error: function (response) {
					console.log(response);
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
			});
		},
		handlePutSuccess: function () {
			$j("#templateHeading").text(WebHooksPlugin.Configurations.localStore.myJson.parentTemplateDescription);
			this.updateEditor();
		},
		doPost: function() {
			if (WebHooksPlugin.Configurations.localStore.myJson.id == '_new' || WebHooksPlugin.Configurations.localStore.myJson.id == '_copy') {
				this.postWebHookData();
			} else {
				this.putWebHookData();
			}
			return false;
		}

	})) */
	/*,
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
            $j(".dialogTitle").text(title);
            this.cleanFields(id);
            this.cleanErrors();

		    //populateWebHookDialog(id);
		    //doExtraCompleted();
		    $j("div.tabPane").css('display', 'none');
		    $j('#tab-container').easytabs('select', tab);
		    $j(tab).css('display', 'block');
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
										    "BUILD_ADDED_TO_QUEUE" : false,
										    "BUILD_REMOVED_FROM_QUEUE" : false,
										    "BEFORE_BUILD_FINISHED" : false,
										    "RESPONSIBILITY_CHANGED" : false,
											"BUILD_FIXED" : true,
										    "BUILD_INTERRUPTED" : false,
										    "BUILD_PINNED" : false,
										    "BUILD_UNPINNED" : false,
										    "SERVICE_MESSAGE_RECEIVED" : false
									    }
									}),
						success:function(response){
							$j('#webhookTestProgress').css("display","none");
							dialog.cleanErrors();
							var ul = $j('<ul>');

							if (response.error) {
								ul.append($j('<li/>').text("Error: " + response.error.message + " (" + response.error.errorCode + ")"));
							} else {
								ul.append($j('<li/>').text("Success: " + response.statusReason + " (" + response.statusCode + ")"));
							}
							ul.append($j('<li/>').text("URL: " + response.url));
							ul.append($j('<li/>').text("Duration: " + response.executionTime + " @ " + moment(response.dateTime, moment.ISO_8601).format("dddd, MMMM Do YYYY, h:mm:ss a")));

							$j("#webhookDialogAjaxResult").empty().append(ul);
						},
						error: function (response) {
							$j('#webhookTestProgress').css("display","none");
							WebHooksPlugin.handleAjaxError(dialog, response);
						}
					});
				}
			} else {
				$j("#webhookDialogAjaxResult").empty().append("Please select a Build first");
			}
			return false;
		}

    }))*/
/*};
/*
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

		$j("#webhookPreviewBuildId").empty().append($j('<option></option>').val(null).text("Loading build history..."))
		$j.ajax ({
			url: window['base_uri'] + '/app/rest/builds?locator=' + locator
					+ "state:finished&fields=build(id,number,status,finishDate,buildType(id,name))",
			type: "GET",
			headers : {
				'Accept' : 'application/json'
			},
			success: function (response) {
				var myselect = $j("#webhookPreviewBuildId");
				myselect.empty().append( $j('<option></option>').val(null).text("Choose a Build...") );
				$j(response.build).each(function(index, build) {
					var desc = build.buildType.name
							  + "#" + build.number
							  + " - " + build.status + " ("
							  + moment(build.finishDate, moment.ISO_8601).fromNow()
							  + ")";
					myselect.append( $j('<option></option>').val(build.id).text(desc) );
				});
			},
			error: function (response) {
				if (response.status == 404) {
					$j("#webhookPreviewBuildId").empty().append(
							$j('<option></option>').val(null).text("No builds found. Choose a different project.")
						);
				} else {
					WebHooksPlugin.handleAjaxError(dialog, response);
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
    	subText = " & sub-projects";
    }

	if($j('#webHookFormContents input.buildType_single:checked').length == $j('#webHookFormContents input.buildType_single').length){
		$j('input.buildType_all').prop('checked', true);
		$j('span#selectedBuildCount').text("all" + subText);
	} else {
		$j('input.buildType_all').prop('checked', false);
		$j('span#selectedBuildCount').text($j('#webHookFormContents input.buildType_single:checked').length + subText);
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
/*
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
	populateBuildHistoryAjax();
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

			$j('#webHookFormContents select#payloadFormatHolder').val(webhook.payloadTemplate).change();

			$j('#buildTypeSubProjects').prop('checked', webhook.subProjectsEnabled);
			$j.each(webhook.builds, function() {
				var isChecked = '';
				if (this.enabled) {
					isChecked = ' checked';
				}
				var cbox = $j('<input' + isChecked + ' onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">');
				var label = $j('<label></label>');
				label.text(this.buildTypeName);
				label.prepend(cbox);
				var container = $j('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"></p>');
				container.append(label);
				$j('#buildList').append(container);
			});

			populateWebHookAuthExtrasPane(webhook);
			$j('select#extraAuthType').change(function() {
				populateWebHookAuthExtrasPaneFromChange(webhook);
			});
			if ($j('#payloadFormatHolder').val()) {
				$j('#currentTemplateName').text(lookupTemplateName($j('#payloadFormatHolder').val()));
			} else {
				$j('#currentTemplateName').html("&nbsp;");
			}
		}
	});
	updateSelectedBuildTypes();
	renderPreviewOnChange(); // Empty the div if no build selected.

}

function lookupTemplateName(templateId){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateId === templateKey){
			name = template.templateDescription;
			return false;
		}
	});
	return name;
}

function lookupTemplate(templateId){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateId === templateKey){
			name = template.templateId;
			return false;
		}
	});
	return name;
}

function lookupFormat(templateId){
	var name;
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		if (templateId === templateKey){
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

			if (webhook.uniqueKey === $j("#editWebHookForm input[id='webHookId']").val() && "addWebHook" === $j("#editWebHookForm input[id='submitAction']").val() ){
				isNew = true;
				newRow.hide();
			}
			newRow.appendTo('#webHookTable > tbody');

			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemUrl").text(webhook.url).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey, '#hookPane');});
			if (webhook.payloadTemplate === 'none') {
				$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").text(webhook.payloadFormatForWeb);
			} else {
				$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemFormat").html("").append($j("<a href='template.html?template=" + webhook.payloadTemplate +"'></a>").text(webhook.payloadFormatForWeb));
			}
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemEvents").text(webhook.enabledEventsListForWeb).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey,'#hookPane');});
			$j("#viewRow_" + webhook.uniqueKey + " > td.webHookRowItemBuilds").text(webhook.enabledBuildsListForWeb).click(function(){WebHooksPlugin.showEditDialog(webhook.uniqueKey, '#buildPane');});
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

$j(document).ready( function() {
	$j('#tab-container').easytabs({
		  animate: false,
		  updateHash: false
	});
});

*/