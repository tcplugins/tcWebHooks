WebHookPlugin = {
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
    reRunMigration: function(data) {
		WebHookPlugin.ReRunMigrationDialog.showDialog("Re-run Full Migration?", 'reRunFullMigration', data);
    },
    ReRunMigrationDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('confirmMigrationDialog');
        },

        formElement: function () {
            return $('confirmMigrationForm');
        },

        showDialog: function (title, action, data) {
            $j("input[id='MigrationAction']").val(action);
            $j(".dialogTitle").text(title);
            this.resetAndShow(data);
            //this.getWebHookTemplateData(data.templateId, data.templateNumber, action);

        },
		
		cancelDialog: function(row, dialog) {
			this.close();
		},

        resetAndShow: function (data) {
			//this.disableAndClearCheckboxes();
            //this.cleanFields(data);
            this.showCentered();
            //this.clearEditor();
        },

        cleanFields: function (data) {
            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#confirmMigrationForm .error").remove();
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
        	var next = $j("#ajaxConfirmMigrationResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxConfirmMigrationResult").after("<p class='error'>" + message + "</p>");
        	}
        },

		doPost: function() {
			var dialog = this;
			var parameters = {
			    action: $j("#confirmMigrationForm #MigrationAction").val()
			};
			dialog.cleanErrors();
			
			BS.ajaxRequest(window['base_uri'] + '/admin/manageWebHookStatistics.html', {
				parameters: parameters,
				onComplete: function(transport) {
					var shouldClose = true;
					var shouldRedirect = false;
					if (transport != null && transport.responseXML != null) {
						var response = transport.responseXML.getElementsByTagName("response");
						if (response != null && response.length > 0) {
							var responseTag = response[0];
							var error = responseTag.getAttribute("error");
							if (error != null) {
								shouldClose = false;
								dialog.ajaxError(error)
							} else if (responseTag.getAttribute("status") == "OK") {
								shouldClose = true;
								if (responseTag.getAttribute("redirect") == "true") {
									shouldRedirect = true;
								}
							} else if (responseTag.firstChild == null) {
								shouldClose = false;
								alert("Error: empty response");
							}
						}
					}
					if (shouldClose) {
						$("migrationStatusContainer").refresh();
						dialog.close();
					}
					
				}
			});
			return false;
		}

    }))
};
