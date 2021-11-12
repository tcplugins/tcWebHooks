chai.should();
var expect = chai.expect;

function expectEqual(jQueryFormField, value) {
	expect($j(jQueryFormField).val()).to.equal(value);
}

function expectIsChecked(jQueryFormField) {
	expect($j(jQueryFormField).is(':checked')).to.equal(true);
}

function expectIsUnChecked(jQueryFormField) {
	expect($j(jQueryFormField).is(':checked')).to.equal(false);
}

describe('Populate WebHook Form when Adding an new Webhook', function() {

	beforeEach('Load Store with empty webhook data', function() {
		var dialog = WebHooksPlugin.Configurations.EditDialog;
		dialog.getStore().myJson = dialog.createEmptyWebhook('myProjectId');
		dialog.handleGetSuccess('add');
	});

	it('URL is set correctly', function() {
		expectEqual('#webHookUrl','');
	})

	it('buildSuccessful is checked', function() {
		expectIsChecked('#buildSuccessful');
	})
	it('buildRemovedFromQueue is checked', function() {
		expectIsChecked('#buildRemovedFromQueue');
	})
	it('buildStarted is checked', function() {
		expectIsChecked('#buildStarted');
	})
	it('changesLoaded is checked', function() {
		expectIsChecked('#changesLoaded');
	})
	it('buildInterrupted is checked', function() {
		expectIsChecked('#buildInterrupted');
	})
	it('beforeBuildFinish is checked', function() {
		expectIsChecked('#beforeBuildFinish');
	})
	it('buildSuccessful is checked', function() {
		expectIsChecked('#buildSuccessful');
	})
	it('buildFailed is checked', function() {
		expectIsChecked('#buildFailed');
	})
	it('responsibilityChanged is checked', function() {
		expectIsChecked('#responsibilityChanged');
	})
	it('buildPinned is checked', function() {
		expectIsChecked('#buildPinned');
	})
	it('buildUnpinned is checked', function() {
		expectIsChecked('#buildUnpinned');
	})
	it('serviceMessageReceived is checked', function() {
		expectIsChecked('#serviceMessageReceived');
	})
	it('buildFixed is unchecked', function() {
		expectIsUnChecked('#buildFixed');
	})
	it('buildBroken is unchecked', function() {
		expectIsUnChecked('#buildBroken');
	})
})

describe('Populate WebHook Form when Editing an existing Webhook (01)', function() {

	beforeEach('Load Store with mocked data from webHook01', function() {
		var dialog = WebHooksPlugin.Configurations.EditDialog;
		dialog.getStore().myJson = webHook01;
		dialog.handleGetSuccess('edit');
	});

	it('URL is set correctly', function() {
		expectEqual('#webHookUrl','http://localhost:8111/webhooks/endpoint.html');
	})

	it('buildSuccessful is checked', function() {
		expectIsChecked('#buildSuccessful');
	})
	it('buildRemovedFromQueue is checked', function() {
		expectIsChecked('#buildRemovedFromQueue');
	})
	it('buildStarted is checked', function() {
		expectIsChecked('#buildStarted');
	})
	it('changesLoaded is checked', function() {
		expectIsChecked('#changesLoaded');
	})
	it('buildInterrupted is checked', function() {
		expectIsChecked('#buildInterrupted');
	})
	it('beforeBuildFinish is checked', function() {
		expectIsChecked('#beforeBuildFinish');
	})
	it('buildSuccessful is checked', function() {
		expectIsChecked('#buildSuccessful');
	})
	it('buildFailed is checked', function() {
		expectIsChecked('#buildFailed');
	})
	it('responsibilityChanged is checked', function() {
		expectIsChecked('#responsibilityChanged');
	})
	it('buildPinned is checked', function() {
		expectIsChecked('#buildPinned');
	})
	it('buildUnpinned is checked', function() {
		expectIsChecked('#buildUnpinned');
	})
	it('serviceMessageReceived is checked', function() {
		expectIsChecked('#serviceMessageReceived');
	})
	it('buildFixed is unchecked', function() {
		expectIsUnChecked('#buildFixed');
	})
	it('buildBroken is unchecked', function() {
		expectIsUnChecked('#buildBroken');
	})
})

describe('Populate WebHook Form when Editing an existing Webhook (02)', function() {
	var dialog = WebHooksPlugin.Configurations.EditDialog;

	beforeEach('Load Store with mocked data from webHook02', function() {
		dialog.getStore().myJson = webHook02;
		dialog.handleGetSuccess('edit');
	});

	afterEach('Clear Store and Form', function() {
		dialog.getStore().myJson = {};
		dialog.disableAndClearCheckboxes();
		dialog.cleanFields({ 'projectId' : "unsetProject", 'webhookId' : 'unsetWebhookId'});
	});

	it('URL is set correctly', function() {
		expectEqual('#webHookUrl','http://localhost:8111/webhooks/endpoint.html');
	})

	it('buildSuccessful is unchecked', function() {
		expectIsUnChecked('#buildSuccessful');
	})
	it('buildRemovedFromQueue is unchecked', function() {
		expectIsUnChecked('#buildRemovedFromQueue');
	})
	it('buildStarted is unchecked', function() {
		expectIsUnChecked('#buildStarted');
	})
	it('changesLoaded is unchecked', function() {
		expectIsUnChecked('#changesLoaded');
	})
	it('buildInterrupted is unchecked', function() {
		expectIsUnChecked('#buildInterrupted');
	})
	it('beforeBuildFinish is unchecked', function() {
		expectIsUnChecked('#beforeBuildFinish');
	})
	it('buildSuccessful is unchecked', function() {
		expectIsUnChecked('#buildSuccessful');
	})
	it('buildFailed is unchecked', function() {
		expectIsUnChecked('#buildFailed');
	})
	it('responsibilityChanged is unchecked', function() {
		expectIsUnChecked('#responsibilityChanged');
	})
	it('buildPinned is unchecked', function() {
		expectIsUnChecked('#buildPinned');
	})
	it('buildUnpinned is unchecked', function() {
		expectIsUnChecked('#buildUnpinned');
	})
	it('serviceMessageReceived is unchecked', function() {
		expectIsUnChecked('#serviceMessageReceived');
	})
	it('buildFixed is checked', function() {
		expectIsChecked('#buildFixed');
	})
	it('buildBroken is checked', function() {
		expectIsChecked('#buildBroken');
	})
})

describe('Populate WebHook Form when Editing an existing Webhook (03)', function() {
	var dialog = WebHooksPlugin.Configurations.EditDialog;

	beforeEach('Load Store with mocked data from webHook03', function() {
		dialog.getStore().myJson = webHook03;
		dialog.handleGetSuccess('edit');
	});

	afterEach('Clear Store and Form', function() {
		dialog.getStore().myJson = {};
		dialog.disableAndClearCheckboxes();
		dialog.cleanFields({ 'projectId' : "unsetProject", 'webhookId' : 'unsetWebhookId'});
	});

	it('URL is set correctly', function() {
		expectEqual('#webHookUrl','http://localhost:8111/webhooks/endpoint.html');
	})
})
