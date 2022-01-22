WebHooksPlugin.Filters = OO.extend(WebHooksPlugin, {
    EditDialog: OO.extend(WebHooksPlugin.EditDialog, {
        getContainer: function () {
            return $('editWebHookFilterDialog');
        },

        formElement: function () {
            return $('editWebHookFilterForm');
        },

		afterShow: function() {
			alert ("Filter.EditDialog after show");
			$(this.formElement()).setAttribute("onsubmit", "xxxxreturn WebHooksPlugin.Filters.EditDialog.doPost()");
        },

        showDialog: function (title, action, data) {
			this.getStore().loading['filterId'] = data.filterId;
			this.getStore().loading['action'] = action;

            $j("input[id='filterProjectId']").val(data.projectId);
            $j("input[id='WebHookFilteraction']").val(action);
			$j("input[id='filterAction']").val(action);
            $j("div#editWebHookFilterDialog h3.dialogTitle").text(title);
            $j("#editWebHookFilterDialogSubmit").val(action === "addWebhookFilter" ? "Add Filter" : "Edit Filter");
            this.resetAndShow(data);
            let filter = this.getWebHookFilterData(data.projectId, data.filterId, action);
			this.populateForm(action, filter);
			this.highlightRow($j("tr[data-filter-id='" + data.filterId + "']"), this);
			this.afterShow();
        },
        
        cancelDialog: function () {
			this.closeCancel($j("tr[data-filter-id='" + this.getStore().loading['filterId'] + "']"), this);
        },

        resetAndShow: function (data) {
            this.cleanFields(data);
            this.showCentered();
        },

        cleanFields: function (data) {
        	$j("#editWebHookFilterForm input[class='editWebHookFilterFormField']").val("");
        	$j("#editWebHookFilterForm")[0].reset();
        	$j("#editWebHookFilterForm #filterId").val(data.filterId);
        	$j("#editWebHookFilterForm #filterProjectId").val(data.projectId);
        	this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#editWebHookFilterForm .error").remove();
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
        	var next = $j("#ajaxWebHookFilterEditResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxWebHookFilterEditResult").after("<p class='error'>" + message + "</p>");
        	}
        },

		getStore: function () {
			debugLog("getStore: Getting WebHooksPlugin.Configurations.localStore");
			return WebHooksPlugin.Configurations.localStore;
		},

		getWebHookFilterData: function (projectId, filterId, action) {
			if (action === 'addWebhookFilter') {
				this.getStore().myJson = { "id": "_new", "projectId": projectId};
			} else {
				this.getFilterData(projectId, filterId, action);
			}
		},
        populateJsonDataFromForm: function() {
            let filter = {
                id: parseInt($j('#editWebHookFilterForm #filterId').val()),
                enabled: $j('#editWebHookFilterForm #filterDialogEnabled').is(':checked'),
                value: $j('#editWebHookFilterForm #filterDialogValue').val(),
                regex: $j('#editWebHookFilterForm #filterDialogRegex').val(),
            };
            return filter;
        },

		populateForm: function (action, myFilter) {
			$j('#editWebHookFilterForm #filterAction').val(action);
			$j('#editWebHookFilterForm #filterId').val(myFilter.id);
			$j('#editWebHookFilterForm #filterHref').val(myFilter.href);
			if (action === 'addWebhookFilter') {
				$j("#editWebHookFilterForm #filterDialogEnabled").prop('checked', true);
			} else {
				$j("#editWebHookFilterForm #filterDialogEnabled").prop('checked', myFilter.enabled);
			}
			$j("#editWebHookFilterForm #filterDialogValue").val(myFilter.value);
			$j("#editWebHookFilterForm #filterDialogRegex").val(myFilter.regex);
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
    		return $('deleteWebHookFilterDialog');
    	},

    	formElement: function () {
    		return $('deleteWebHookFilterForm');
    	},

    	showDialog: function (title, action, data) {
    		$j("#deleteWebHookFilterForm input[id='WebHookFilteraction']").val(action);
    		$j("div#deleteWebHookFilterDialog h3.dialogTitle").text(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
			$j("#viewRow_" + data.filterId).animate({
	            backgroundColor: "#ffffcc"
	    	}, 1000 );
    	},

        cancelDialog: function () {
        	this.close();
	        $j("#viewRow_" + $j("#deleteWebHookFilterForm input[id='filterId']").val()).animate({
	            backgroundColor: "#ffffff"
	        }, 500 );
        },
        
    	cleanFields: function (data) {
    		$j("#deleteWebHookFilterForm input[id='projectId']").val(data.projectId);
    		$j("#deleteWebHookFilterForm input[id='filterId']").val(data.filterId);
    		$j("#deleteWebHookFilterForm #confirmationWebHookFilterName").text(data.filterName);
    		
    		this.cleanErrors();
    	},

    	cleanErrors: function () {
    		$j("#deleteWebHookFilterForm .error").remove();
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
			var filterId = $j("#deleteWebHookFilterForm input[id='filterId']").val()
			var projectId = $j("#deleteWebHookFilterForm input[id='projectId']").val()

			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/filters/' + projectId + '/id:' + filterId,
				type: "DELETE",
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					let refreshDone = false;
					dialog.close();
					// Animate the removal of the webhook filter table row.
					// Then do the div refresh after the row is gone.
			        $j("#viewRow_" + response.id)
			            .children('td, th')
			            .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
			            .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
			            .children()
			            .slideUp(function() {
			            	if (!refreshDone) {
			            		$j(this).closest('tr').remove();
			            		$("projectWebhookFiltersContainer").refresh();
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
