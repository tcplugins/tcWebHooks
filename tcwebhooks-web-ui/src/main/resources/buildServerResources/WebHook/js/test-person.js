let WebHookConfigurations = Object.create(webHookConfigurations);
let TestPerson = {
		showPerson: function() {
			const test = Object.create(person);

			test.name = 'Sausage'; // "name" is a property set on "me", but not on "person"
			test.isHuman = false; // inherited properties can be overwritten
			test.printIntroduction();
			WebHookConfigurations.showAddDialog();
		}
};


