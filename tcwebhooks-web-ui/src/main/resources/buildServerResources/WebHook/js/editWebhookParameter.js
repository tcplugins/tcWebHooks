WebHooksPlugin.Parameters = OO.extend(WebHooksPlugin, {
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
    EditDialog: OO.extend(WebHooksPlugin.EditDialog, {
        getContainer: function () {
            return $('editWebHookParameterDialog');
        },

        formElement: function () {
            return $('editWebHookParameterForm');
        },

		afterShow: function() {
			$(this.formElement()).setAttribute("onsubmit", "return WebHooksPlugin.Parameters.EditDialog.doPost()");
        },

        showDialog: function (title, action, data) {
			this.getStore().loading['parameterId'] = data.parameterId;
			this.getStore().loading['action'] = action;

            $j("input[id='parameterProjectId']").val(data.projectId);
            $j("input[id='WebHookParameteraction']").val(action);
			$j("input[id='parameterAction']").val(action);
            $j("div#editWebHookParameterDialog h3.dialogTitle").text(title);
            $j("#editWebHookParameterDialogSubmit").val(action === "addWebhookParameter" ? "Add Parameter" : "Edit Parameter");
            this.resetAndShow(data);
            this.getWebHookParameterDataThenPopulateForm(data.projectId, data.parameterId, action, data);

			this.highlightRow($j("tr[data-parameter-id='" + data.parameterId + "']"), this);
			this.afterShow();
        },
        
        cancelDialog: function () {
			this.closeCancel($j("tr[data-parameter-id='" + this.getStore().loading['parameterId'] + "']"), this);
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

		getStore: function () {
			return WebHooksPlugin.Parameters.localStore;
		},

		getWebHookParameterDataThenPopulateForm: function (projectId, parameterId, action, data) {
			if (action === 'addWebhookParameter') {
				let param = { "id": "_new", "projectId": projectId};
				this.getStore().myJson = param;
				this.populateForm(action, param, data);
			} else {
				this.getParameterData(projectId, parameterId, action, data);
			}
		},
		putWebHookParameterData: function () {
			WebHooksPlugin.Parameters.localStore.myJson = this.populateJsonDataFromForm();
			this.putParameterData();
		},
		postWebHookParameterData: function () {
			WebHooksPlugin.Parameters.localStore.myJson = this.populateJsonDataFromForm();
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
		populateJsonDataFromForm: function () {
			var myJson = {};
			myJson.id = $j('#editWebHookParameterForm #parameterId').val();
			myJson.projectId = $j('#editWebHookParameterForm #parameterProjectId').val();
			myJson.href = $j('#editWebHookParameterForm #parameterHref').val();
			myJson.secure = $j("#editWebHookParameterForm #parameterDialogType").val() === "password";
			myJson.name = $j("#editWebHookParameterForm #parameterDialogName").val();
			myJson.value = $j("#editWebHookParameterForm #parameterDialogValue").val();
			myJson.includedInLegacyPayloads = $j("#editWebHookParameterForm #parameterDialogVisibility").val() === "legacy";
			myJson.forceResolveTeamCityVariable = $j("#editWebHookParameterForm #parameterDialogResolve").val() === "forced";
			myJson.templateEngine = $j("#editWebHookParameterForm #parameterDialogTemplateEngine").val();
			return myJson;
		},
		getParameterData: function (projectId, parameterId, action, data) {
			var dialog = this;
    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/parameters/' + projectId + '/id:' + parameterId + '?fields=**',
    		    type: "GET",
    		    headers : {
    		        'Accept' : 'application/json'
    		    },
    		    success: function (response) {
    				dialog.getStore().myJson = response;
    				dialog.populateForm(action, response, data);
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
		populateForm: function (action, myJson, data) {
			$j('#editWebHookParameterForm #parameterAction').val(action);
			$j('#editWebHookParameterForm #parameterId').val(myJson.id);
			$j('#editWebHookParameterForm #parameterHref').val(myJson.href);
			if (data.enableSecure === true) {
				$j("#editWebHookParameterForm #parameterDialogType option[value='password']").attr("enabled", "enabled");
				$j("#editWebHookParameterForm #parameterDialogType").val(myJson.secure ? "password" : "text");
			} else {
				$j("#editWebHookParameterForm #parameterDialogType").val("text");
				$j("#editWebHookParameterForm #parameterDialogType option[value='password']").attr("disabled", "disabled");
			}
			$j("#editWebHookParameterForm #parameterDialogName").val(myJson.name);
			$j("#editWebHookParameterForm #parameterDialogValue").val(myJson.value);
			$j("#editWebHookParameterForm #parameterDialogVisibility").val(myJson.includedInLegacyPayloads ? "legacy" : "template");
			$j("#editWebHookParameterForm #parameterDialogResolve").val(myJson.forceResolveTeamCityVariable ? "forced" : "unforced");
			$j("#editWebHookParameterForm #parameterDialogTemplateEngine").val(myJson.templateEngine === "VELOCITY" ? myJson.templateEngine : "STANDARD");
			this.toggleHidden();
		},
		putParameterData: function () {
			var dialog = this;
			$j.ajax ({
				url: window['base_uri'] + this.getStore().myJson.href,
				type: "PUT",
				data: JSON.stringify(this.getStore().myJson),
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
			this.getStore().myJson.id = "";
			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/parameters/' + this.getStore().myJson.projectId,
				type: "POST",
				data: JSON.stringify(this.getStore().myJson),
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
			$j("#templateHeading").text(this.getStore().myJson.parentTemplateDescription);
			this.updateEditor();
		},
		doPost: function() {
			this.afterShow();
			if (this.getStore().myJson.id == '_new' || this.getStore().myJson.id == '_copy') {
				this.postWebHookParameterData();
			} else {
				this.putWebHookParameterData();
			}
			return false;
		},
		getNextId: function(items) {
			let maxId = 0;
			items.each(function(item, idx) {
				if (parseInt(item.id) > maxId) {
					maxId = parseInt(item.id);
				}
			});
			return (maxId + 1).toString();
		}

    }),

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
    		$j("div#deleteWebHookParameterDialog h3.dialogTitle").text(title);
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
});
