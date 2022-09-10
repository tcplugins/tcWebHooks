Object.defineProperty(this, "log", { get: function () {
	return console.debug.bind(window.console);
}});
Object.defineProperty(this, "logDebug", { get: function () {
	return WebHooksPlugin.isDebug() 
		? console.debug.bind(window.console) 
		: function(){};
	}
});
const queryStringParams = new URLSearchParams(window.location.search);
const WebHooksPlugin = {
	localStore: {
		loading: {},
		afterRefresh: null,
		myJson: {}
	},
	isDebug: function() {
		return queryStringParams.has('debug') && queryStringParams.get('debug') != "false";
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
	afterRefresh: function() {
		if (this.localStore.afterRefresh !== null) {
			$j.each(this.localStore.afterRefresh, function(idx, row){
				// fetch the row from the DOM again since it will
				// have a new object after the dom reloaded
				// apply the CSS and animations to that new object.
				$j('#'+row.id)
					.css({ backgroundColor: '#cceecc' })
					.animate({
						backgroundColor: "#ffffff"
					}, 2500);
				});
			this.localStore.afterRefresh = null;
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
			WebHooksPlugin.localStore.afterRefresh = row;
			dialog.close();
			dialog.getRefreshContainer().refresh();
		}
	})),

	DeleteDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
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
		}
	}))
};