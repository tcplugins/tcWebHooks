const person = {
  isHuman: false,
  localStore: {
		myJson: {}
  },
  printIntroduction: function() {
    console.log(`My name is ${this.name}. Am I human? ${this.isHuman}`);
  },
	handleAjaxError: function(dialog, response) {
		dialog.cleanErrors();
		if (response.status === 422) {
			if (response.responseJSON.errored) {
				$j.each(response.responseJSON.errors, function(index, errorMsg){
					dialog.ajaxError(errorMsg)
				});
			}
		} else {
			console.log("----- begin webhooks AJAX error response -----")
			console.log(response);
			console.log("----- end webhooks AJAX error response -----")
			alert("An unexpected error occured. Please see your browser's javascript console.");
		}
	}
};

const me = Object.create(person);

me.name = 'Matthew'; // "name" is a property set on "me", but not on "person"
me.isHuman = true; // inherited properties can be overwritten

me.printIntroduction();
// expected output: "My name is Matthew. Am I human? true"
