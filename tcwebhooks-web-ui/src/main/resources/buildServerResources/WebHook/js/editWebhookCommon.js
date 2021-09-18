const WebHooksPlugin = {
	localStore: {
		loading: {},
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
	}
};