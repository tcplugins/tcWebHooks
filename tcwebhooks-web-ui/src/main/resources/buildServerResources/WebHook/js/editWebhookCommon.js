Object.defineProperty(this, "log", {get: function () {
	return WebHooksPlugin.isDebug() ? console.log.bind(window.console, '[DEBUG]') 
				 : function(){};}
  });
const queryStringParams = new URLSearchParams(window.location.search);
const WebHooksPlugin = {
	localStore: {
		loading: {},
		myJson: {}
	},
	isDebug: function() {
		return queryStringParams.has('debug') && queryStringParams.get('debug') != "false";
	},
	logDebug: function(logObject) {
		this.isDebug() && console.debug(logObject);
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
			console.log(response.statusText);
			console.log("----- begin webhooks AJAX error response -----")
			console.log(response);
			console.log("----- end webhooks AJAX error response -----")
			alert("An unexpected error occured. Please see your browser's javascript console.");
		}
	},
	EditDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
		getRefreshContainer: function () {
			alert("Please override 'getRefreshContainer' function in your own dialog instance");
		},
		highlightRow: function(row, dialog) {
			row.animate({
				backgroundColor: "#ffffcc"
			}, 1000 );
		},
		closeCancel: function(row, dialog) {
			dialog.close();
			row.animate({
				backgroundColor: "#ffffff"
			}, 500 );
		},
		handleAjaxError: function(dialog, response) {
			WebHooksPlugin.handleAjaxError(dialog, response);
		},
		closeSuccess: function (row, dialog) {
			dialog.close();
			dialog.getRefreshContainer().refresh(function() {
				row
				.css({backgroundColor: '#cceecc'})
				.animate({
					backgroundColor: "#ffffff"
				}, 2500 );
				console.debug(dialog);
		})}
	}))
};