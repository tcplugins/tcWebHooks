WebHooksPlugin.Parameters = {
	localStore: {
		myJson: {}
	},
	handleAjaxError: function(dialog, response) {
		dialog.cleanErrors();
		if (response.status === 422 || response.status === 409) {
			if (response.responseJSON.errored) {
				$j.each(response.responseJSON.errors, function(index, errorMsg){
					dialog.ajaxError(errorMsg)
				});
			}
		} else if (response.status === 403) {
			alert("You are not permissioned to perform this operation. Message is: " + response.responseText);
		} else {
			console.log("----- begin webhooks AJAX error response -----")
			console.log(response);
			console.log("----- end webhooks AJAX error response -----")
			alert("An unexpected error occured. Please see your browser's javascript console.");
		}
	},
    editParameter: function(data) {
    	if (!restApiDetected) {
    		WebHooksPlugin.NoRestApi.NoRestApiDialog.showDialog();
    	} else {
    		WebHooksPlugin.Parameters.EditDialog.showDialog("Edit WebHook Parameter", 'editWebhookParameter', data);
    	}
    },
    addParameter: function(data) {
    	if (!restApiDetected) {
    		WebHooksPlugin.NoRestApi.NoRestApiDialog.showDialog();
    	} else {
    		WebHooksPlugin.Parameters.EditDialog.showDialog("Add WebHook Parameter", 'addWebhookParameter', data);
    	}
    },
    deleteParameter: function(data) {
    	if (!restApiDetected) {
    		WebHooksPlugin.NoRestApi.NoRestApiDialog.showDialog();
    	} else {
    		WebHooksPlugin.Parameters.DeleteDialog.showDialog("Delete WebHook Parameter", 'deleteWebhookParameter', data);
    	}
    },
    EditDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('editWebHookParameterDialog');
        },

        formElement: function () {
            return $('editWebHookParameterForm');
        },

        showDialog: function (title, action, data) {

            $j("input[id='parameterProjectId']").val(data.projectId);
            $j("input[id='WebHookParameteraction']").val(action);
            $j(".dialogTitle").text(title);
            $j("#editWebHookParameterDialogSubmit").val(action === "addWebhookParameter" ? "Add Parameter" : "Edit Parameter");
            this.resetAndShow(data);
            this.getWebHookParameterData(data.projectId, data.parameterId, action);
			$j("#viewRow_" + data.parameterId).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
        },
        
        cancelDialog: function () {
        	this.close();
	        $j("#viewRow_" + $j("#editWebHookParameterForm input[id='parameterId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
        },

        resetAndShow: function (data) {
			this.disableAndClearCheckboxes();
            this.cleanFields(data);
            this.showCentered();
        },

        cleanFields: function (data) {
        	$j("#editWebHookParameterForm input[class='editWebHookParameterFormField']").val("");
        	$j("#editWebHookParameterForm")[0].reset();
        	$j("#editWebHookParameterForm #parameterId").val(data.parameterId);
        	$j("#editWebHookParameterForm #parameterProjectId").val(data.projectId);
        	this.toggleHidden();
        	this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#editWebHookParameterForm .error").remove();
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
        	var next = $j("#ajaxWebHookParameterEditResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxWebHookParameterEditResult").after("<p class='error'>" + message + "</p>");
        	}
        },

		getWebHookParameterData: function (projectId, parameterId, action) {
			if (action === 'addWebhookParameter') {
				WebHooksPlugin.Parameters.localStore.myJson = { "id": "_new", "projectId": projectId};
			} else {
				this.getParameterData(projectId, parameterId, action);
			}
		},
		putWebHookParameterData: function () {
			this.updateJsonDataFromForm();
			this.putParameterData();
		},
		postWebHookParameterData: function () {
			this.updateJsonDataFromForm();
			this.postParameterData();
		},
		disableAndClearCheckboxes: function () {
			$j("#editWebHookParameterForm input.buildState").prop("disabled", true).prop( "checked", false);
			$j("#editWebHookParameterForm label").addClass("checkboxLooksDisabled");
		},
		disableCheckboxes: function () {
			$j("#editWebHookParameterForm input.buildState").prop("disabled", true);
			$j("#editWebHookParameterForm label").addClass("checkboxLooksDisabled");
		},
		enableCheckboxes: function () {
			$j("#editWebHookParameterForm input.buildState").prop("disabled", false);
		},
		updateJsonDataFromForm: function () {
			var myJson = {};
			myJson.id = $j('#editWebHookParameterForm #parameterId').val();
			myJson.projectId = $j('#editWebHookParameterForm #parameterProjectId').val();
			myJson.href = $j('#editWebHookParameterForm #parameterHref').val();
			myJson.secure = $j("#editWebHookParameterForm #parameterDialogType").val() === "password";
			myJson.name = $j("#editWebHookParameterForm #parameterDialogTypeName").val();
			myJson.value = $j("#editWebHookParameterForm #parameterDialogTypeValue").val();
			myJson.includedInLegacyPayloads = $j("#editWebHookParameterForm #parameterDialogVisibility").val() === "legacy";
			myJson.templateEngine = $j("#editWebHookParameterForm #parameterDialogTemplateEngine").val();
			WebHooksPlugin.Parameters.localStore.myJson = myJson;
		},
		getParameterData: function (projectId, parameterId, action) {
			var dialog = this;
    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/parameters/' + projectId + '/id:' + parameterId + '?fields=**',
    		    type: "GET",
    		    headers : {
    		        'Accept' : 'application/json'
    		    },
    		    success: function (response) {
    				WebHooksPlugin.Parameters.localStore.myJson = response;
    				dialog.handleGetSuccess(action);
    		    },
				error: function (response) {
					console.log(response);
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
    		});
		},

		toggleHidden: function () {
			var type = $j("#editWebHookParameterForm #parameterDialogType").val();
		    switch (type) {
		        case 'password':
		        {
		            $j("#editWebHookParameterForm #parameterDialogTypeValue").attr('type', 'password');
		            return;
		        }
		        case 'text':
		        {
		            $j("#editWebHookParameterForm #parameterDialogTypeValue").attr('type', 'text');
		            return;
		        }
		    }
		},
		handleGetSuccess: function (action) {
			var myJson = WebHooksPlugin.Parameters.localStore.myJson;
			$j('#editWebHookParameterForm #parameterId').val(myJson.id);
			$j('#editWebHookParameterForm #parameterHref').val(myJson.href);
			$j("#editWebHookParameterForm #parameterDialogType").val(myJson.secure ? "password" : "text");
			$j("#editWebHookParameterForm #parameterDialogTypeName").val(myJson.name);
			$j("#editWebHookParameterForm #parameterDialogTypeValue").val(myJson.value);
			$j("#editWebHookParameterForm #parameterDialogVisibility").val(myJson.includedInLegacyPayloads ? "legacy" : "template");
			$j("#editWebHookParameterForm #parameterDialogTemplateEngine").val(myJson.templateEngine);
			this.toggleHidden();
		},
		putParameterData: function () {
			var dialog = this;
			$j.ajax ({
				url: window['base_uri'] + WebHooksPlugin.Parameters.localStore.myJson.href,
				type: "PUT",
				data: JSON.stringify(WebHooksPlugin.Parameters.localStore.myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					dialog.close();
					$("projectWebhookParametersContainer").refresh(function() {
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
		postParameterData: function () {
			var dialog = this;
			// For creating, the ID must be empty
			WebHooksPlugin.Parameters.localStore.myJson.id = "";
			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/parameters/' + WebHooksPlugin.Parameters.localStore.myJson.projectId,
				type: "POST",
				data: JSON.stringify(WebHooksPlugin.Parameters.localStore.myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					console.log(response);
					dialog.close();
					$("projectWebhookParametersContainer").refresh(function() {
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
			$j("#templateHeading").text(WebHooksPlugin.Parameters.localStore.myJson.parentTemplateDescription);
			this.updateEditor();
		},
		doPost: function() {
			if (WebHooksPlugin.Parameters.localStore.myJson.id == '_new' || WebHooksPlugin.Parameters.localStore.myJson.id == '_copy') {
				this.postWebHookParameterData();
			} else {
				this.putWebHookParameterData();
			}
			return false;
		}

    })),

    NoRestApiDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('noRestApiDialog');
    	},

    	formElement: function () {
    		return $('noRestApiForm');
    	},

    	showDialog: function () {
    		this.showCentered();
    	}

    })),
    DeleteDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('deleteWebHookParameterDialog');
    	},

    	formElement: function () {
    		return $('deleteWebHookParameterForm');
    	},

    	showDialog: function (title, action, data) {
    		$j("#deleteWebHookParameterForm input[id='WebHookParameteraction']").val(action);
    		$j(".dialogTitle").text(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
			$j("#viewRow_" + data.parameterId).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
    	},

        cancelDialog: function () {
        	this.close();
	        $j("#viewRow_" + $j("#deleteWebHookParameterForm input[id='parameterId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
        },
        
    	cleanFields: function (data) {
    		$j("#deleteWebHookParameterForm input[id='projectId']").val(data.projectId);
    		$j("#deleteWebHookParameterForm input[id='parameterId']").val(data.parameterId);
    		$j("#deleteWebHookParameterForm #confirmationWebHookParameterName").text(data.parameterName);
    		
    		this.cleanErrors();
    	},

    	cleanErrors: function () {
    		$j("#deleteWebHookParameterForm .error").remove();
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
    		var next = $j("#ajaxDeleteResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxDeleteResult").after("<p class='error'>" + message + "</p>");
    		}
    	},

    	doPost: function() {
    		this.cleanErrors();

			var dialog = this;
			var parameterId = $j("#deleteWebHookParameterForm input[id='parameterId']").val()
			var projectId = $j("#deleteWebHookParameterForm input[id='projectId']").val()

			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/parameters/' + projectId + '/id:' + parameterId,
				type: "DELETE",
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					let refreshDone = false;
					dialog.close();
					// Animate the removal of the webhook parameter table row.
					// Then do the div refresh after the row is gone.
			        $j("#viewRow_" + response.id)
			            .children('td, th')
			            .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
			            .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
			            .children()
			            .slideUp(function() {
			            	if (!refreshDone) {
			            		$j(this).closest('tr').remove();
			            		$("projectWebhookParametersContainer").refresh();
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
    }))
};
