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
    for (const oldBuildState of oldBuildStateList) {
        for (const newBuildState of newBuildStateList) {
            console.log(oldBuildState, newBuildState);
            if (oldBuildState.type == newBuildState.type) {
                oldStateCount++;
                expect(oldBuildState.enabled).to.equal(newBuildState.enabled);
            }
        }
    }
    expect(oldStateCount).to.equal(expectedEnabledCount);

    let newStateCount = 0;
    for (const newBuildState of newBuildStateList) {
        for (const oldBuildState of oldBuildStateList) {
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
    $j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function (templateKey, template) {
        console.log(template.templateId + ' :: ' + template.description);
        $j('#webHookFormContents select#payloadFormatHolder').append($j('<option>', {
            value: template.templateId,
            text: template.description
        }));
    });
}

describe('Populate WebHook Form when Adding an new Webhook', function () {

    beforeEach('Load Store with empty webhook data', function () {
        var dialog = WebHooksPlugin.Configurations.EditDialog;
        dialog.getStore().myJson = dialog.createEmptyWebhook('myProjectId');
        dialog.handleGetSuccess('add');
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', '');
    })

    it('buildSuccessful is checked', function () {
        expectIsChecked('#buildSuccessful');
    })
    it('buildRemovedFromQueue is checked', function () {
        expectIsChecked('#buildRemovedFromQueue');
    })
    it('buildStarted is checked', function () {
        expectIsChecked('#buildStarted');
    })
    it('changesLoaded is checked', function () {
        expectIsChecked('#changesLoaded');
    })
    it('buildInterrupted is checked', function () {
        expectIsChecked('#buildInterrupted');
    })
    it('beforeBuildFinish is checked', function () {
        expectIsChecked('#beforeBuildFinish');
    })
    it('buildSuccessful is checked', function () {
        expectIsChecked('#buildSuccessful');
    })
    it('buildFailed is checked', function () {
        expectIsChecked('#buildFailed');
    })
    it('responsibilityChanged is checked', function () {
        expectIsChecked('#responsibilityChanged');
    })
    it('buildPinned is checked', function () {
        expectIsChecked('#buildPinned');
    })
    it('buildUnpinned is checked', function () {
        expectIsChecked('#buildUnpinned');
    })
    it('serviceMessageReceived is checked', function () {
        expectIsChecked('#serviceMessageReceived');
    })
    it('buildFixed is unchecked', function () {
        expectIsUnChecked('#buildFixed');
    })
    it('buildBroken is unchecked', function () {
        expectIsUnChecked('#buildBroken');
    })
})

describe('Populate WebHook Form when Editing an existing Webhook (01)', function () {

    beforeEach('Load Store with mocked data from webHook01', function () {
        var dialog = WebHooksPlugin.Configurations.EditDialog;
        dialog.getStore().myJson = webHook01;
        dialog.handleGetSuccess('edit');
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })

    it('buildSuccessful is checked', function () {
        expectIsChecked('#buildSuccessful');
    })
    it('buildRemovedFromQueue is checked', function () {
        expectIsChecked('#buildRemovedFromQueue');
    })
    it('buildStarted is checked', function () {
        expectIsChecked('#buildStarted');
    })
    it('changesLoaded is checked', function () {
        expectIsChecked('#changesLoaded');
    })
    it('buildInterrupted is checked', function () {
        expectIsChecked('#buildInterrupted');
    })
    it('beforeBuildFinish is checked', function () {
        expectIsChecked('#beforeBuildFinish');
    })
    it('buildSuccessful is checked', function () {
        expectIsChecked('#buildSuccessful');
    })
    it('buildFailed is checked', function () {
        expectIsChecked('#buildFailed');
    })
    it('responsibilityChanged is checked', function () {
        expectIsChecked('#responsibilityChanged');
    })
    it('buildPinned is checked', function () {
        expectIsChecked('#buildPinned');
    })
    it('buildUnpinned is checked', function () {
        expectIsChecked('#buildUnpinned');
    })
    it('serviceMessageReceived is checked', function () {
        expectIsChecked('#serviceMessageReceived');
    })
    it('buildFixed is unchecked', function () {
        expectIsUnChecked('#buildFixed');
    })
    it('buildBroken is unchecked', function () {
        expectIsUnChecked('#buildBroken');
    })
})

describe('Populate WebHook Form when Editing an existing Webhook (02)', function () {
    var dialog = WebHooksPlugin.Configurations.EditDialog;

    beforeEach('Load Store with mocked data from webHook02', function () {
        dialog.getStore().myJson = webHook02;
        dialog.handleGetSuccess('edit');
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })

    it('buildSuccessful is unchecked', function () {
        expectIsUnChecked('#buildSuccessful');
    })
    it('buildRemovedFromQueue is unchecked', function () {
        expectIsUnChecked('#buildRemovedFromQueue');
    })
    it('buildStarted is unchecked', function () {
        expectIsUnChecked('#buildStarted');
    })
    it('changesLoaded is unchecked', function () {
        expectIsUnChecked('#changesLoaded');
    })
    it('buildInterrupted is unchecked', function () {
        expectIsUnChecked('#buildInterrupted');
    })
    it('beforeBuildFinish is unchecked', function () {
        expectIsUnChecked('#beforeBuildFinish');
    })
    it('buildSuccessful is unchecked', function () {
        expectIsUnChecked('#buildSuccessful');
    })
    it('buildFailed is unchecked', function () {
        expectIsUnChecked('#buildFailed');
    })
    it('responsibilityChanged is unchecked', function () {
        expectIsUnChecked('#responsibilityChanged');
    })
    it('buildPinned is unchecked', function () {
        expectIsUnChecked('#buildPinned');
    })
    it('buildUnpinned is unchecked', function () {
        expectIsUnChecked('#buildUnpinned');
    })
    it('serviceMessageReceived is unchecked', function () {
        expectIsUnChecked('#serviceMessageReceived');
    })
    it('buildFixed is checked', function () {
        expectIsChecked('#buildFixed');
    })
    it('buildBroken is checked', function () {
        expectIsChecked('#buildBroken');
    })
})

describe('Populate WebHook Form when Editing an existing Webhook (03)', function () {
    var dialog = WebHooksPlugin.Configurations.EditDialog;

    beforeEach('Load Store with mocked data from webHook03', function () {
        dialog.getStore().myJson = webHook03;
        dialog.handleGetSuccess('edit');
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })
})

describe('Build WebHook response object from Form when Editing an existing Webhook (03)', function () {
    var dialog = WebHooksPlugin.Configurations.EditDialog;
    var json, webhook;

    beforeEach('Load Store with mocked data from webHook03', function () {
        dialog.getStore().myJson = webHook03;
        dialog.handleGetSuccess('edit');
        json = dialog.getStore().myJson;
        webhook = convertFormToWebHook(json);
    });

    afterEach('Clear Store and Form', function () {
        json = {};
        webhook = {};
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
    });

    it('convertFormToWebHook matches config', function () {
        expect(json.url).to.equal(webhook.url);
        expect(json.id).to.equal(webhook.id);
        expect(json.enabled).to.equal(webhook.enabled);
        expect(json.buildTypes.allEnabled).to.equal(webhook.buildTypes.allEnabled);
        expect(json.buildTypes.subProjectsEnabled).to.equal(webhook.buildTypes.subProjectsEnabled);
        expect(json.projectId).to.equal(webhook.projectId);
    })
});

describe('Populate from Form and then POST WebHook - Webhook (03)', function () {

    var dialog = WebHooksPlugin.Configurations.EditDialog;
    loadTemplatesIntoFormatHolder();

    var xhr, requests, body;
    //dialog.showCentered();

    beforeEach('Load Store with mocked data from webHook03', function () {
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

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
        body = {};
        xhr.restore();
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })
    it('href is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
        expect(dialog.getStore().myJson.href).to.equal('/app/rest/webhooks/configurations/TcPlugins/id:500198784');
    })
    it('POST Payload contains webhook href.', function () {
        expect(requests[0].url).to.contain("/app/rest/webhooks/configurations/TcPlugins/id:500198784");
    })
    it('POST Payload contains webhook buildTypes.', function () {
        expect(body.buildTypes.allEnabled).to.equal(true);
        expect(body.buildTypes.subProjectsEnabled).to.equal(true);
        expect(body.buildTypes.id.length).to.equal(0);
    })
    it('POST Payload contains webhook enabled.', function () {
        expect(body.enabled).to.equal(true);
    })
    it('POST Payload contains webhook template.', function () {
        expect(body.template).to.equal("test01");
    })
})

describe('Populate WebHook Form when Editing an existing Webhook (04)', function () {
    var dialog = WebHooksPlugin.Configurations.EditDialog;

    beforeEach('Load Store with mocked data from webHook04', function () {
        dialog.getStore().myJson = webHook04;
        dialog.handleGetSuccess('edit');
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })
    
    it('All Build Types is unchecked', function () {
        expectIsUnChecked('#buildTypeAll');
    })
    
    it('Sub-Project Build Types is unchecked', function () {
        expectIsUnChecked('#buildTypeSubProjects');
    })

    it('Specific build is checked', function () {
        expectIsChecked('input[type=checkbox][value=Project_MyBuildId02]');
    })

    it('Three specific builds are checked when "All Builds" checked', function () {
        $j('#buildTypeAll').prop('checked', true);
        toggleAllBuildTypesSelected();
        expectIsChecked('input[type=checkbox][value=Project_MyBuildId01]');
        expectIsChecked('input[type=checkbox][value=Project_MyBuildId02]');
        expectIsChecked('input[type=checkbox][value=Project_MyBuildId03]');
    })

    it('No specific builds are checked when "All Builds" is unchecked', function () {
        $j('#buildTypeAll').prop('checked', false);
        toggleAllBuildTypesSelected();
        expectIsUnChecked('input[type=checkbox][value=Project_MyBuildId01]');
        expectIsUnChecked('input[type=checkbox][value=Project_MyBuildId02]');
        expectIsUnChecked('input[type=checkbox][value=Project_MyBuildId03]');
    })

})

describe('Populate from Form and then POST WebHook - Webhook (04)', function () {

    var dialog = WebHooksPlugin.Configurations.EditDialog;
    loadTemplatesIntoFormatHolder();

    var xhr, requests, body;

    beforeEach('Load Store with mocked data from webHook04', function () {
        dialog.getStore().myJson = webHook04;
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

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
        body = {};
        xhr.restore();
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })
    it('href is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
        expect(dialog.getStore().myJson.href).to.equal('/app/rest/webhooks/configurations/TcPlugins/id:306243139');
    })
    it('POST Payload contains webhook href.', function () {
        expect(requests[0].url).to.contain("/app/rest/webhooks/configurations/TcPlugins/id:306243139");
    })
    it('POST Payload contains webhook buildTypes.', function () {
        expect(body.buildTypes.allEnabled).to.equal(false);
        expect(body.buildTypes.subProjectsEnabled).to.equal(false);
        expect(body.buildTypes.id.length).to.equal(1);
        expect(body.buildTypes.id).to.contain("Project_MyBuildId02");
    })
    it('POST Payload contains webhook enabled.', function () {
        expect(body.enabled).to.equal(true);
    })
    it('POST Payload contains webhook template.', function () {
        expect(body.template).to.equal("legacy-json");
    })
    it('POST Payload contains Parameters.', function () {
        expect(body.parameters.parameter.length).to.equal(2);
        expect(body.parameters.count).to.equal(2);
    })
    it('POST Payload contains Filters.', function () {
        expect(body.filters.filter.length).to.equal(3);
        expect(body.filters.count).to.equal(3);
    })
    it('POST Payload contains Headers.', function () {
        expect(body.headers.header.length).to.equal(1);
        expect(body.headers.count).to.equal(1);
    })
})

describe('Populate from Form and then POST WebHook - Webhook (03)', function () {

    var dialog = WebHooksPlugin.Configurations.EditDialog;
    loadTemplatesIntoFormatHolder();

    var xhr, requests, body;

    beforeEach('Load Store with mocked data from webHook03', function () {
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

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
        dialog.disableAndClearCheckboxes();
        dialog.cleanFields({ 'projectId': "unsetProject", 'webhookId': 'unsetWebhookId' });
        body = {};
        xhr.restore();
    });

    it('URL is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
    })
    it('href is set correctly', function () {
        expectEqual('#webHookUrl', 'http://localhost:8111/webhooks/endpoint.html');
        expect(dialog.getStore().myJson.href).to.equal('/app/rest/webhooks/configurations/TcPlugins/id:500198784');
    })
    it('POST Payload contains webhook href.', function () {
        expect(requests[0].url).to.contain("/app/rest/webhooks/configurations/TcPlugins/id:500198784");
    })
    it('POST Payload contains webhook buildTypes.', function () {
        expect(body.buildTypes.allEnabled).to.equal(true);
        expect(body.buildTypes.subProjectsEnabled).to.equal(true);
        expect(body.buildTypes.id.length).to.equal(0);
    })
    it('POST Payload contains webhook enabled.', function () {
        expect(body.enabled).to.equal(true);
    })
    it('POST Payload contains webhook template.', function () {
        expect(body.template).to.equal("test01");
    })
})

describe('Edit Parameters on existing Webhook (04)', function () {
    var dialog = WebHooksPlugin.Configurations.EditParameterDialog;

    beforeEach('Load Store with mocked data from webHook04', function () {
        dialog.getStore().myJson = webHook04;
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
    });
    
    it('Populate Parameter 1 in Form from Webhook', function () {
        let parameter = dialog.getWebHookParameterData('TcPlugins', "1", 'updateWebhookParameter');
        expect(parameter.id).to.equal("1");
        dialog.populateForm('updateWebhookParameter', parameter);
        expectEqual('#editWebHookParameterForm #parameterId', "1");
        expectEqual('#editWebHookParameterForm #parameterDialogName', "color");
        expectEqual('#editWebHookParameterForm #parameterDialogValue', "red");
        expectEqual('#editWebHookParameterForm #parameterDialogVisibility', "legacy");
        expectEqual('#editWebHookParameterForm #parameterDialogTemplateEngine', "STANDARD");
        expectEqual('#editWebHookParameterForm #parameterDialogType', "text");
    })

    it('Populate Parameter 2 in Form from Webhook', function () {
        let parameter = dialog.getWebHookParameterData('TcPlugins', "2", 'updateWebhookParameter');
        expect(parameter.id).to.equal("2");
        dialog.populateForm('updateWebhookParameter', parameter);
        expectEqual('#editWebHookParameterForm #parameterId', "2");
        expectEqual('#editWebHookParameterForm #parameterDialogName', "notify");
        expectEqual('#editWebHookParameterForm #parameterDialogValue', "1");
        expectEqual('#editWebHookParameterForm #parameterDialogVisibility', "template");
        expectEqual('#editWebHookParameterForm #parameterDialogTemplateEngine', "VELOCITY");
        expectEqual('#editWebHookParameterForm #parameterDialogType', "password");
    })

    it('Populate Parameter 1 in Form and read back out', function () {
        let parameter = dialog.getWebHookParameterData('TcPlugins', "1", 'updateWebhookParameter');
        expect(parameter.id).to.equal("1");
        dialog.populateForm('updateWebhookParameter', parameter);
        let paramFromForm = dialog.populateJsonDataFromForm();
        expect(paramFromForm.id).to.equal("1");
    })

    it('Populate Parameter 1 in Form, change it, and read back out', function () {
        let parameter = dialog.getWebHookParameterData('TcPlugins', "1", 'updateWebhookParameter');
        expect(parameter.id).to.equal("1");
        dialog.populateForm('updateWebhookParameter', parameter);
        $j('#editWebHookParameterForm #parameterDialogName').val("test01")
        $j('#editWebHookParameterForm #parameterDialogValue').val("test02")
        $j('#editWebHookParameterForm #parameterDialogVisibility').val("template")
        $j('#editWebHookParameterForm #parameterDialogTemplateEngine').val("VELOCITY")
        $j('#editWebHookParameterForm #parameterDialogType').val("password")
        let paramFromForm = dialog.populateJsonDataFromForm();
        expect(paramFromForm.id).to.equal("1");
        expect(paramFromForm.name).to.equal("test01");
        expect(paramFromForm.value).to.equal("test02");
        expect(paramFromForm.secure).to.equal(true);
        expect(paramFromForm.includedInLegacyPayloads).to.equal(false);
        expect(paramFromForm.templateEngine).to.equal("VELOCITY");
    })

})

describe('Edit Headers on existing Webhook (04)', function () {
    var dialog = WebHooksPlugin.Configurations.EditHeaderDialog;

    beforeEach('Load Store with mocked data from webHook04', function () {
        dialog.getStore().myJson = webHook04;
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
    });
    
    it('Populate Header 1 in Form from Webhook', function () {
        let header = dialog.getWebHookHeaderData('TcPlugins', "1", 'updateWebhookHeader');
        expect(header.id).to.equal(1);
        dialog.populateForm('updateWebhookHeader', header);
        expectEqual('#editWebHookHeaderForm #headerId', "1");
        expectEqual('#editWebHookHeaderForm #headerDialogName', "my-header-showing-build-name");
        expectEqual('#editWebHookHeaderForm #headerDialogValue', "${buildName}");
    })

    it('Populate Header 1 in Form and read back out', function () {
        let header = dialog.getWebHookHeaderData('TcPlugins', "1", 'updateWebhookHeader');
        expect(header.id).to.equal(1);
        dialog.populateForm('updateWebhookHeader', header);
        let headerFromForm = dialog.populateJsonDataFromForm();
        expect(headerFromForm.id).to.equal("1");
    })

    it('Populate Header 1 in Form, change it, and read back out', function () {
        let header = dialog.getWebHookHeaderData('TcPlugins', "1", 'updateWebhookHeader');
        expect(header.id).to.equal(1);
        dialog.populateForm('updateWebhookHeader', header);
        $j('#editWebHookHeaderForm #headerDialogName').val("foo")
        $j('#editWebHookHeaderForm #headerDialogValue').val("${bar}")
 
        expectEqual('#editWebHookHeaderForm #headerId', "1");
        expectEqual('#editWebHookHeaderForm #headerDialogName', "foo");
        expectEqual('#editWebHookHeaderForm #headerDialogValue', "${bar}");
        let headerFromForm = dialog.populateJsonDataFromForm();
        expect(headerFromForm.id).to.equal("1");
        expect(headerFromForm.name).to.equal("foo");
        expect(headerFromForm.value).to.equal("${bar}");
    })

})

describe('Edit Filters on existing Webhook (04)', function () {
    var dialog = WebHooksPlugin.Configurations.EditFilterDialog;

    beforeEach('Load Store with mocked data from webHook04', function () {
        dialog.getStore().myJson = webHook04;
    });

    afterEach('Clear Store and Form', function () {
        dialog.getStore().myJson = {};
    });
    
    it('Populate Filter 1 in Form from Webhook', function () {
        let filter = dialog.getWebHookFilterData('TcPlugins', "1", 'updateWebhookFilter');
        expect(filter.id).to.equal(1);
        dialog.populateForm('updateWebhookFilter', filter);
        expectEqual('#editWebHookFilterForm #filterId', "1");
        expectEqual('#editWebHookFilterForm #filterDialogValue', "${branchDisplayName}");
        expectEqual('#editWebHookFilterForm #filterDialogRegex', "^master$");
        expectIsChecked('#editWebHookFilterForm #filterDialogEnabled');
    })

    it('Populate Filter 2 in Form from Webhook', function () {
        let filter = dialog.getWebHookFilterData('TcPlugins', "2", 'updateWebhookParameter');
        expect(filter.id).to.equal(2);
        dialog.populateForm('updateWebhookFilter', filter);
        expectEqual('#editWebHookFilterForm #filterId', "2");
        expectEqual('#editWebHookFilterForm #filterDialogValue', "${buildInternalTypeId}");
        expectEqual('#editWebHookFilterForm #filterDialogRegex', "^bt\\d$");
        expectIsChecked('#editWebHookFilterForm #filterDialogEnabled');
    })

    it('Populate Filter 3 in Form from Webhook', function () {
        let filter = dialog.getWebHookFilterData('TcPlugins', "3", 'updateWebhookParameter');
        expect(filter.id).to.equal(3);
        dialog.populateForm('updateWebhookFilter', filter);
        expectEqual('#editWebHookFilterForm #filterId', "3");
        expectEqual('#editWebHookFilterForm #filterDialogValue', "${foo}");
        expectEqual('#editWebHookFilterForm #filterDialogRegex', "^bar$");
        expectIsUnChecked('#editWebHookFilterForm #filterDialogEnabled');
    })

    it('Populate Filter 1 in Form and read back out', function () {
        let filter = dialog.getWebHookFilterData('TcPlugins', "1", 'updateWebhookParameter');
        expect(filter.id).to.equal(1);
        dialog.populateForm('updateWebhookFilter', filter);
        let filterFromForm = dialog.populateJsonDataFromForm();
        expect(filterFromForm.id).to.equal("1");
    })

    it('Populate Filter 1 in Form, change it, and read back out', function () {
        let filter = dialog.getWebHookFilterData('TcPlugins', "1", 'updateWebhookParameter');
        console.log(filter);
        expect(filter.id).to.equal(1);
        dialog.populateForm('updateWebhookFilter', filter);
        $j('#editWebHookFilterForm #filterDialogValue').val("${test02}")
        $j('#editWebHookFilterForm #filterDialogRegex').val("^password$")
        $j('#editWebHookFilterForm #filterDialogEnabled').prop('checked', false);
 
        expectEqual('#editWebHookFilterForm #filterId', "1");
        expectEqual('#editWebHookFilterForm #filterDialogValue', "${test02}");
        expectEqual('#editWebHookFilterForm #filterDialogRegex', "^password$");
        expectIsUnChecked('#editWebHookFilterForm #filterDialogEnabled');
        let filterFromForm = dialog.populateJsonDataFromForm();
        expect(filterFromForm.id).to.equal("1");
        expect(filterFromForm.value).to.equal("${test02}");
        expect(filterFromForm.regex).to.equal("^password$");
        expect(filterFromForm.enabled).to.equal(false);
    })

})