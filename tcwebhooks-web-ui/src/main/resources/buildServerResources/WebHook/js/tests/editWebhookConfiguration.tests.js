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

function compareBuildState(oldBuildStateList, newBuildStateList, expectedEnabledCount) {
	let oldStateCount = 0;
	for(const oldBuildState of oldBuildStateList) {
		for(const newBuildState of newBuildStateList) {
			console.log(oldBuildState, newBuildState);
			if (oldBuildState.type == newBuildState.type) {
				oldStateCount++;
				expect(oldBuildState.enabled).to.equal(newBuildState.enabled);
			}
		}
	}
	expect(oldStateCount).to.equal(expectedEnabledCount);

	let newStateCount = 0;
	for(const newBuildState of newBuildStateList) {
		for(const oldBuildState of oldBuildStateList) {
			console.log(oldBuildState, newBuildState);
			if (oldBuildState.type == newBuildState.type) {
				newStateCount++;
				expect(oldBuildState.enabled).to.equal(newBuildState.enabled);
			}
		}
	}
	expect(newStateCount).to.equal(expectedEnabledCount);

}

function loadTemplatesIntoFormatHolder() {
	$j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function(templateKey, template){
		console.log(template.templateId + ' :: ' + template.description);
			$j('#webHookFormContents select#payloadFormatHolder').append($j('<option>', { 
					value: template.templateId,
					text : template.description 
			}));
	});
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

describe('Build WebHook response object from Form when Editing an existing Webhook (03)', function() {
	var dialog = WebHooksPlugin.Configurations.EditDialog;
	var json, webhook;
	
	beforeEach('Load Store with mocked data from webHook03', function() {
		dialog.getStore().myJson = webHook03;
		dialog.handleGetSuccess('edit');
		json = dialog.getStore().myJson;
		webhook = convertFormToWebHook(json);
	});

	afterEach('Clear Store and Form', function() {
		json = {};
		webhook = {};
		dialog.getStore().myJson = {};
		dialog.disableAndClearCheckboxes();
		dialog.cleanFields({ 'projectId' : "unsetProject", 'webhookId' : 'unsetWebhookId'});
	});

	it('convertFormToWebHook matches config', function() {
		expect(json.url).to.equal(webhook.url);
		expect(json.id).to.equal(webhook.id);
		expect(json.enabled).to.equal(webhook.enabled);
		expect(json.buildTypes.allEnabled).to.equal(webhook.buildTypes.allEnabled);
		expect(json.buildTypes.subProjectsEnabled).to.equal(webhook.buildTypes.subProjectsEnabled);
		expect(json.projectId).to.equal(webhook.projectId);
	})
});


//	it('convertFormToWebHook buildState matches config', function() {

describe('zzz- Build WebHook response object from Form when Editing an existing Webhook (03)', function() {

	var dialog = WebHooksPlugin.Configurations.EditDialog;
	loadTemplatesIntoFormatHolder();

	var xhr, requests, body;
	//dialog.showCentered();

	beforeEach('Load Store with mocked data from webHook03', function() {
		dialog.getStore().myJson = webHook03;
		dialog.handleGetSuccess('edit');
		
		xhr = sinon.useFakeXMLHttpRequest();
		requests = [];
		xhr.onCreate = function (req) {
			requests.push(req);
		};
		
		dialog.doPost();
		expect(requests.length).to.equal(1);
		console.log(requests[0]);
		body = JSON.parse(requests[0].requestBody);
	});

	afterEach('Clear Store and Form', function() {
		dialog.getStore().myJson = {};
		dialog.disableAndClearCheckboxes();
		dialog.cleanFields({ 'projectId' : "unsetProject", 'webhookId' : 'unsetWebhookId'});
		body = {};
		xhr.restore();
	});

	it('URL is set correctly', function() {
		expectEqual('#webHookUrl','http://localhost:8111/webhooks/endpoint.html');
	})
	it('href is set correctly', function() {
		expectEqual('#webHookUrl','http://localhost:8111/webhooks/endpoint.html');
		expect(dialog.getStore().myJson.href).to.equal('/app/rest/webhooks/configurations/TcPlugins/id:500198784');
	})
	it('POST Payload contains webhook href.', function() {
		expect(requests[0].url).to.contain("/app/rest/webhooks/configurations/TcPlugins/id:500198784");
	})
	it('POST Payload contains webhook buildTypes.', function() {
		expect(body.buildTypes.allEnabled).to.equal(true); 
		expect(body.buildTypes.subProjectsEnabled).to.equal(true); 
		expect(body.buildTypes.id.length).to.equal(0); 
	})
	it('POST Payload contains webhook enabled.', function() {
		console.log(body);
		expect(body.enabled).to.equal(true); 
	})
	it('POST Payload contains webhook template.', function() {
		console.log(body);
		console.log(dialog.getStore().myJson);
		expect(body.template).to.equal("test01");
	})
})