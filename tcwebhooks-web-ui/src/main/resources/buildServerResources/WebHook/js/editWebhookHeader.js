WebHooksPlugin.Headers = OO.extend(WebHooksPlugin, {
    EditDialog: OO.extend(WebHooksPlugin.EditDialog, {
        getContainer: function () {
            return $('editWebHookHeaderDialog');
        },

        formElement: function () {
            return $('editWebHookHeaderForm');
        },

		afterShow: function() {
			alert ("Header.EditDialog after show");
			$(this.formElement()).setAttribute("onsubmit", "xxxxreturn WebHooksPlugin.Headers.EditDialog.doPost()");
        },

        showDialog: function (title, action, data) {
			this.getStore().loading['headerId'] = data.headerId;
			this.getStore().loading['action'] = action;

            $j("input[id='headerProjectId']").val(data.projectId);
            $j("input[id='WebHookHeaderaction']").val(action);
			$j("input[id='headerAction']").val(action);
            $j("div#editWebHookHeaderDialog h3.dialogTitle").text(title);
            $j("#editWebHookHeaderDialogSubmit").val(action === "addWebhookHeader" ? "Add Header" : "Edit Header");
            this.resetAndShow(data);
            let header =this.getWebHookHeaderData(data.projectId, data.headerId, action);
			this.populateForm(action, header);
			this.highlightRow($j("tr[data-header-id='" + data.headerId + "']"), this);
			this.afterShow();
        },
        
        cancelDialog: function () {
			this.closeCancel($j("tr[data-header-id='" + this.getStore().loading['headerId'] + "']"), this);
        },

        resetAndShow: function (data) {
            this.cleanFields(data);
            this.showCentered();
        },

        cleanFields: function (data) {
        	$j("#editWebHookHeaderForm input[class='editWebHookHeaderFormField']").val("");
        	$j("#editWebHookHeaderForm")[0].reset();
        	$j("#editWebHookHeaderForm #headerId").val(data.headerId);
        	$j("#editWebHookHeaderForm #headerProjectId").val(data.projectId);
        	this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#editWebHookHeaderForm .error").remove();
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
        	var next = $j("#ajaxWebHookHeaderEditResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxWebHookHeaderEditResult").after("<p class='error'>" + message + "</p>");
        	}
        },

		getStore: function () {
			logDebug("getStore: Getting WebHooksPlugin.Configurations.localStore", WebHooksPlugin.Configurations.localStore);
			return WebHooksPlugin.Configurations.localStore;
		},

		getWebHookHeaderData: function (projectId, headerId, action) {
			if (action === 'addWebhookHeader') {
				this.getStore().myJson = { "id": "_new", "projectId": projectId};
			} else {
				this.getHeaderData(projectId, headerId, action);
			}
		},
        populateJsonDataFromForm: function() {
            let header = {
                id: parseInt($j('#editWebHookHeaderForm #headerId').val()),
                name: $j('#editWebHookHeaderForm #headerDialogName').val(),
                value: $j('#editWebHookHeaderForm #headerDialogValue').val(),
            };
            return header;
        },

		populateForm: function (action, myHeader) {
			$j('#editWebHookHeaderForm #headerAction').val(action);
			$j('#editWebHookHeaderForm #headerId').val(myHeader.id);
			$j('#editWebHookHeaderForm #headerHref').val(myHeader.href);
			$j("#editWebHookHeaderForm #headerDialogName").val(myHeader.name);
			$j("#editWebHookHeaderForm #headerDialogValue").val(myHeader.value);
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
    		return $('deleteWebHookHeaderDialog');
    	},

    	formElement: function () {
    		return $('deleteWebHookHeaderForm');
    	},

    	showDialog: function (title, action, data) {
    		$j("#deleteWebHookHeaderForm input[id='WebHookHeaderaction']").val(action);
    		$j("div#deleteWebHookHeaderDialog h3.dialogTitle").text(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
			$j("#viewRow_" + data.headerId).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
    	},

        cancelDialog: function () {
        	this.close();
	        $j("#viewRow_" + $j("#deleteWebHookHeaderForm input[id='headerId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
        },
        
    	cleanFields: function (data) {
    		$j("#deleteWebHookHeaderForm input[id='projectId']").val(data.projectId);
    		$j("#deleteWebHookHeaderForm input[id='headerId']").val(data.headerId);
    		$j("#deleteWebHookHeaderForm #confirmationWebHookHeaderName").text(data.headerName);
    		
    		this.cleanErrors();
    	},

    	cleanErrors: function () {
    		$j("#deleteWebHookHeaderForm .error").remove();
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
			var headerId = $j("#deleteWebHookHeaderForm input[id='headerId']").val()
			var projectId = $j("#deleteWebHookHeaderForm input[id='projectId']").val()

			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/headers/' + projectId + '/id:' + headerId,
				type: "DELETE",
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					let refreshDone = false;
					dialog.close();
					// Animate the removal of the webhook header table row.
					// Then do the div refresh after the row is gone.
			        $j("#viewRow_" + response.id)
			            .children('td, th')
			            .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
			            .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
			            .children()
			            .slideUp(function() {
			            	if (!refreshDone) {
			            		$j(this).closest('tr').remove();
			            		$("projectWebhookHeadersContainer").refresh();
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
