WebHookRestApiHealthStatus = {
    fixPluginFile: function(filePath) {
    	WebHookRestApiHealthStatus.FixPluginDialog.showDialog("Fix Plugin File", 'apiZipFix', filePath);
    },
    FixPluginDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('fixPluginDialog');
    	},
    	
    	formElement: function () {
    		return $('fixPluginForm');
    	},
    	
    	showDialog: function (title, action, filePath) {
    		$j("#fixPluginForm input[id='FixPluginaction']").val(action);
    		$j("#fixPluginDialog .dialogTitle").text(title);
    		this.cleanFields(filePath);
    		this.cleanErrors();
    		this.showCentered();
    	},
    	
    	cleanFields: function (filePath) {
    		$j("#fixPluginForm input[id='filePath']").val(filePath);
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#fixPluginForm .error").remove();
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
    		var next = $j("#apiFixResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#apiFixResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doValidate: function() {
    		var errorFound = false;
    		return !errorFound;
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		if (!this.doValidate()) {
    			return false;
    		}
    		
    		var parameters = {
    				action: $j("#fixPluginForm #FixPluginaction").val(),
    				"apiZipFile": $j("#fixPluginForm input[id='filePath']").val()
    		};
    		
    		var dialog = this;
    		
    		BS.ajaxRequest(window['base_uri'] + '/admin/manageWebHooksRestApi.html', {
    			parameters: parameters,
    			onComplete: function(transport) {
    				var shouldClose = true;
    				if (transport != null && transport.responseXML != null) {
    					var response = transport.responseXML.getElementsByTagName("response");
    					if (response != null && response.length > 0) {
    						var responseTag = response[0];
    						var error = responseTag.getAttribute("error");
    						if (error != null) {
    							$("healthReportContainer").refresh();
    							shouldClose = false;
    							dialog.ajaxError(error);
    						} else if (responseTag.getAttribute("status") == "OK") {
    							shouldClose = true;
    						} else if (responseTag.firstChild == null) {
    							shouldClose = false;
    							alert("Error: empty response");
    						}
    					}
    				}
    				if (shouldClose) {
    					dialog.close();
    					$("healthReportContainer").refresh();
    				}
    			}
    		});
    		
    		return false;
    	}
    }))
};
