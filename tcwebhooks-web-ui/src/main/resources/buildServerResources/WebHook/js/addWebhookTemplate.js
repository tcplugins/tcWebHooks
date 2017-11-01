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
    addTemplate: function(data) {
    	WebHooksPlugin.AddTemplateDialog.showDialog("Add New Template", 'addTemplate');
    },
    AddTemplateDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('addTemplateDialog');
    	},
    	
    	formElement: function () {
    		return $('addTemplateForm');
    	},
    	
    	showDialog: function (title, action, data) {
    		$j("input[id='WebhookTemplateaction']").val(action);
    		$j(".dialogTitle").html(title);
    		$j("#addTemplateForm #addTemplateDialogSubmit").hide();
    		$j("#addTemplateForm #addTemplateDialogExpand").show();
    		$j("#addTemplateForm .templateDetails").hide();
    		$j("#addTemplateForm .templateShort").show();
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
    	},
    	
    	expandDialog: function (title, action, data) {
    		$j("#addTemplateForm #addTemplateDialogExpand").hide();
    		$j("#addTemplateForm #addTemplateDialogSubmit").show();
    		$j("#addTemplateForm .templateShort").hide();
    		$j("#addTemplateForm .templateDetails").show();
    		this.showCentered();
    	},
    	
    	cleanFields: function (data) {
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#addTemplateForm .error").remove();
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
    		var next = $j("#ajaxAddResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxAddResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		var dialog = this;
    		//console.log($j("input[id='WebhookTemplateaction']").val());
    		
    		var myJsonContent = {
    				name : $j("#addTemplateForm input[id='template.id']").val(),
					description : $j("#addTemplateForm input[id='template.name']").val(),
					toolTip : $j("#addTemplateForm input[id='template.tooltip']").val(),
					rank : $j("#addTemplateForm input[id='template.rank']").val(),
					format : "jsonTemplate",
    		};
			
    		console.log(myJsonContent);
    		
    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/templates',
    			type: "POST",
				data: JSON.stringify(myJsonContent),
				dataType: 'json',
    			headers : {
    				'Content-Type' : 'application/json',
    				'Accept' : 'application/json'
    			},
    			success: function (response) {
    				dialog.close();
    				window.location = window['base_uri'] + '/webhooks/template.html?' + myJsonContent.name;
    			},
    			error: function (response) {
    				console.log(response);
    				alert(response);
    			}
    		});
    		
    		return false;
    	}
    }))
};
