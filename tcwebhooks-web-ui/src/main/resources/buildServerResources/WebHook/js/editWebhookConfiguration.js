WebHooksPlugin.Configurations = OO.extend(WebHooksPlugin, {
    showAddDialog: function (data, tab) {
        this.EditDialog.showDialog("Add Web Hook", 'addWebHook', 'new', data, tab);
    },
    showEditDialog: function (data, tab) {
        this.EditDialog.showDialog("Edit Web Hook", 'updateWebHook', data, tab);
    },
    showDeleteDialog: function (data) {
        this.DeleteDialog.showDialog("Delete Web Hook", 'deleteWebHook', data);
    },
    showAddParameterDialog: function (data) {
        this.EditParameterDialog.showDialog("Add Web Hook Parameter", 'addWebhookParameter', data);
    },
    showEditParameterDialog: function (data) {
        this.EditParameterDialog.showDialog("Edit Web Hook Parameter", 'updateWebhookParameter', data);
    },

    DeleteDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('deleteWebHookDialog');
        },

        formElement: function () {
            return $('deleteWebHookForm');
        },

        showDialog: function (title, action, data) {
            $j("#editWebHookForm input[id='webHookId']").val("none"); // Unset the edit id, so that it doesn't get animated by delete.
            $j("input[id='webHookaction']").val(action);
            $j(".dialogTitle").text(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
            $j("#viewRow_" + data.webhookId).animate({
                backgroundColor: "#ffffcc"
            }, 1000);
        },

        cancelDialog: function () {
            this.close();
            $j('li.tab').removeAttr('style');
            $j("#viewRow_" + $j("#deleteWebHookForm input[id='webHookId']").val()).animate({
                backgroundColor: "#ffffff"
            }, 500);
        },

        cleanFields: function (data) {
            $j("#deleteWebHookForm input[id='webHookId']").val(data.webhookId);
            $j("#deleteWebHookForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#deleteWebHookForm .error").remove();
        },

        error: function ($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },

        ajaxError: function (message) {
            var next = $j("#ajaxWebHookDeleteResult").next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $j("#ajaxWebHookDeleteResult").after("<p class='error'>" + message + "</p>");
            }
        },

        doPost: function () {
            this.cleanErrors();

            var dialog = this;
            var webhookId = $j("#deleteWebHookForm input[id='webHookId']").val()
            var projectId = $j("#deleteWebHookForm input[id='projectId']").val()

            $j.ajax({
                url: window['base_uri'] + '/app/rest/webhooks/configurations/' + projectId + '/id:' + webhookId,
                type: "DELETE",
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                success: function (response) {
                    let refreshDone = false;
                    dialog.close();
                    // Animate the removal of the webhook table row.
                    // Then do the div refresh after the row is gone.
                    $j("#viewRow_" + response.id)
                        .children('td, th')
                        .animate({ backgroundColor: "#ffffff", colour: "#ffffff", paddingTop: 0, paddingBottom: 0 })
                        .wrapInner('<div />')	// Wrap the content in a div, so that the height can be animated.
                        .children()
                        .slideUp(function () {
                            if (!refreshDone) {
                                $j(this).closest('tr').remove();
                                $("projectWebhooksContainer").refresh();
                                refreshDone = true;
                            }
                        });
                },
                error: function (response) {
                    WebHooksPlugin.handleAjaxError(dialog, response);
                }
            });

            return false;
        }
    })),

    EditDialog: OO.extend(WebHooksPlugin.EditDialog, {
        getContainer: function () {
            return $('editWebHookDialog');
        },

        formElement: function () {
            return $('editWebHookForm');
        },

        getRefreshContainer: function () {
            return $("projectWebhooksContainer");
        },

        getStore: function () {
            log("getStore: Getting WebHooksPlugin.Configurations.localStore");
            log(WebHooksPlugin.Configurations.localStore);
            return WebHooksPlugin.Configurations.localStore;
        },

        showDialog: function (title, action, data) {

            $j("input[id='webhookProjectId']").val(data.projectId);
            $j("input[id='WebHookaction']").val(action);
            $j(".dialogTitle").text(title);
            $j("#editWebHookDialogSubmit").val(action === "addWebhook" ? "Add WebHook" : "Edit WebHook");
            this.resetAndShow(data);
            this.getWebHookData(data.projectId, data.webhookId, action);
            this.highlightRow($j("#viewRow_" + data.webhookId), this);
        },

        cancelDialog: function () {
            var row = $j("#viewRow_" + $j("#editWebHookForm input[id='webhookId']").val());
            this.closeCancel(row, this);
        },

        resetAndShow: function (data) {
            this.disableAndClearCheckboxes();
            this.cleanFields(data);
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("#editWebHookForm input[class='editWebHookFormField']").val("");
            $j("#editWebHookForm")[0].reset();
            $j("#editWebHookForm #webhookId").val(data.webhookId);
            $j("#editWebHookForm #webhookProjectId").val(data.projectId);
            //this.toggleHidden();
            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#editWebHookForm .error").remove();
        },

        error: function ($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },

        ajaxError: function (message) {
            var next = $j("#ajaxWebHookEditResult").next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $j("#ajaxWebHookEditResult").after("<p class='error'>" + message + "</p>");
            }
        },

        getWebHookData: function (projectId, webhookId, action) {
            if (action === 'addWebHook') {
                this.getStore().myJson = this.createEmptyWebhook(projectId);
                this.handleGetSuccess(action)
            } else {
                this.getData(projectId, webhookId, action);
            }
        },
        putWebHookData: function () {
            this.updateJsonDataFromForm();
            this.putData();
        },
        postWebHookData: function () {
            this.updateJsonDataFromForm();
            this.postData();
        },
        disableAndClearCheckboxes: function () {
            $j("#editWebHookForm input.buildState").prop("disabled", true).prop("checked", false);
            $j("#editWebHookForm label").addClass("checkboxLooksDisabled");
        },
        disableCheckboxes: function () {
            $j("#editWebHookForm input.buildState").prop("disabled", true);
            $j("#editWebHookForm label").addClass("checkboxLooksDisabled");
        },
        enableCheckboxes: function () {
            $j("#editWebHookForm input.buildState").prop("disabled", false);
        },
        updateJsonDataFromForm: function () {
            var myJson = {};
            myJson.id = this.getStore().myJson.id;
            myJson.projectId = this.getStore().myJson.projectId;
            myJson.href = this.getStore().myJson.href;
            this.getStore().myJson = convertFormToWebHook(myJson);
        },
        getData: function (projectId, webhookId, action) {
            var dialog = this;
            $j.ajax({
                url: window['base_uri'] + '/app/rest/webhooks/configurations/' + projectId + '/id:' + webhookId + '?fields=**',
                type: "GET",
                headers: {
                    'Accept': 'application/json'
                },
                success: function (response) {
                    dialog.getStore().myJson = response;
                    dialog.handleGetSuccess(action);
                },
                error: function (response) {
                    console.log(response);
                    WebHooksPlugin.Configurations.handleAjaxError(dialog, response);
                }
            });
        },

        handleGetSuccess: function (action) {
            var myJson = this.getStore().myJson;
            log(myJson);
            log(this.getStore().myJson);
            //$j('#editWebHookForm #parameterId').val(myJson.id);
            //$j('#editWebHookForm #parameterHref').val(myJson.href);
            populateWebHookDialog(this.getStore().myJson);
        },
        putData: function () {
            var dialog = this;
            log(this.getStore().myJson.href);
            log(this.getStore().myJson);
            $j.ajax({
                url: window['base_uri'] + dialog.getStore().myJson.href,
                type: "PUT",
                data: JSON.stringify(dialog.getStore().myJson),
                dataType: 'json',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                success: function (response) {
                    this.closeSuccess($j("#viewRow_" + response.id), dialog);
                },
                error: function (response) {
                    console.log(response);
                    WebHooksPlugin.Configurations.handleAjaxError(dialog, response);
                }
            });
        },
        postData: function () {
            var dialog = this;
            // For creating, the ID must be empty
            this.getStore().myJson.id = "";
            $j.ajax({
                url: window['base_uri'] + '/app/rest/webhooks/configurations/' + dialog.getStore().myJson.projectId,
                type: "POST",
                data: JSON.stringify(dialog.getStore().myJson),
                dataType: 'json',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                success: function (response) {
                    this.closeSuccess($j("#viewRow_" + response.id), dialog);
                },
                error: function (response) {
                    console.log(response);
                    this.handleAjaxError(dialog, response);
                }
            });
        },
        handlePutSuccess: function () {
            $j("#templateHeading").text(this.getStore().myJson.parentTemplateDescription);
            this.updateEditor();
        },
        doPost: function () {
            if (this.getStore().myJson.id == '_new' || this.getStore().myJson.id == '_copy') {
                this.postWebHookData();
            } else {
                this.putWebHookData();
            }
            return false;
        },

        createEmptyWebhook: function (projectId) {
            return {
                "id": "_new",
                "projectId": projectId,
                "enabled": true,
                "buildTypes": {
                    "allEnabled": true,
                    "subProjectsEnabled": true,
                },
                "buildState": [
                    {
                        "type": "buildAddedToQueue",
                        "enabled": true
                    },
                    {
                        "type": "buildRemovedFromQueue",
                        "enabled": true
                    },
                    {
                        "type": "buildStarted",
                        "enabled": true
                    },
                    {
                        "type": "changesLoaded",
                        "enabled": true
                    },
                    {
                        "type": "buildInterrupted",
                        "enabled": true
                    },
                    {
                        "type": "beforeBuildFinish",
                        "enabled": true
                    },
                    {
                        "type": "buildFinished",
                        "enabled": true
                    },
                    {
                        "type": "buildSuccessful",
                        "enabled": true
                    },
                    {
                        "type": "buildFailed",
                        "enabled": true
                    },
                    {
                        "type": "responsibilityChanged",
                        "enabled": true
                    },
                    {
                        "type": "buildPinned",
                        "enabled": true
                    },
                    {
                        "type": "buildUnpinned",
                        "enabled": true
                    },
                    {
                        "type": "serviceMessageReceived",
                        "enabled": true
                    }
                ],
                "parameters": { "count": 0, "parameter": [] },
                "headers": { "count": 0, "header": [] },
                "filters": { "count": 0, "filter": [] },
            };
        }

    }),

    EditParameterDialog: OO.extend(WebHooksPlugin.Parameters.EditDialog, {

        getStore: function () {
            WebHooksPlugin.isDebug() && console.debug("getStore: Getting WebHooksPlugin.Configurations.localStore");
            return WebHooksPlugin.Configurations.localStore;
        },

        getContainer: function () {
            return $('editWebHookParameterDialog');
        },

        formElement: function () {
            return $('editWebHookParameterForm');
        },
        afterShow: function () {
            this.formElement().setAttribute("onsubmit", "return WebHooksPlugin.Configurations.EditParameterDialog.doPost()");
        },
        doPost: function () {
            if ($j("input[id='parameterAction']").val() == 'addWebhookParameter') {
                WebHooksPlugin.isDebug() && console.debug("doPost: Add new parameter");
                this.addWebHookParameterDataToWebHook();
            } else {
                WebHooksPlugin.isDebug() && console.debug("doPost: Save exisitng parameter");
                this.saveWebHookParameterDataToWebHook();
            }
            return false;
        },
        addWebHookParameterDataToWebHook: function () {
            var myJson = this.populateJsonDataFromForm();
            this.getStore().parameters.add(myJson);
        }

    })

});

function populateBuildHistoryAjax(locator) {
    $j('#webhookRendered').empty();

    if (!locator) { // We got a null or undefined locator. Empty the build list and return
        $j("#webhookPreviewBuildId").empty();
        console.log("populateBuildHistoryAjax() : locator is null. This should not happen");
        return;
    }

    if (locator === 'project:_Root,') {
        locator = '';
    }

    $j("#webhookPreviewBuildId").empty().append($j('<option></option>').val(null).text("Loading build history..."))
    $j.ajax({
        url: window['base_uri'] + '/app/rest/builds?locator=' + locator
            + "state:finished&fields=build(id,number,status,finishDate,buildType(id,name))",
        type: "GET",
        headers: {
            'Accept': 'application/json'
        },
        success: function (response) {
            var myselect = $j("#webhookPreviewBuildId");
            myselect.empty().append($j('<option></option>').val(null).text("Choose a Build..."));
            $j(response.build).each(function (index, build) {
                var desc = build.buildType.name
                    + "#" + build.number
                    + " - " + build.status + " ("
                    + moment(build.finishDate, moment.ISO_8601).fromNow()
                    + ")";
                myselect.append($j('<option></option>').val(build.id).text(desc));
            });
        },
        error: function (response) {
            if (response.status == 404) {
                $j("#webhookPreviewBuildId").empty().append(
                    $j('<option></option>').val(null).text("No builds found. Choose a different project.")
                );
            } else {
                WebHooksPlugin.handleAjaxError(dialog, response);
            }
        }
    });

}


function selectBuildState() {
    doExtraCompleted();
}

function doExtraCompleted() {
    if ($j('#buildSuccessful').is(':checked')) {
        $j('.onBuildFixed').removeClass('onCompletionDisabled');
        $j('tr.onBuildFixed td input').prop('disabled', false);
    } else {
        $j('.onBuildFixed').addClass('onCompletionDisabled');
        $j('tr.onBuildFixed td input').prop('disabled', true);
    }
    if ($j('#buildFailed').is(':checked')) {
        $j('.onBuildFailed').removeClass('onCompletionDisabled');
        $j('tr.onBuildFailed td input').prop('disabled', false);
    } else {
        $j('.onBuildFailed').addClass('onCompletionDisabled');
        $j('tr.onBuildFailed td input').prop('disabled', true);
    }
}

function toggleAllBuildTypesSelected() {
    $j.each($j('.buildType_single'), function () {
        $j(this).prop('checked', $j('input.buildType_all').is(':checked'))
    });
    updateSelectedBuildTypes();
}

function updateSelectedBuildTypes() {
    var subText = "";
    if ($j('#buildTypeSubProjects').is(':checked')) {
        subText = " & sub-projects";
    }

    if ($j('#webHookFormContents input.buildType_single:checked').length == $j('#webHookFormContents input.buildType_single').length) {
        $j('input.buildType_all').prop('checked', true);
        $j('span#selectedBuildCount').text("all" + subText);
    } else {
        $j('input.buildType_all').prop('checked', false);
        $j('span#selectedBuildCount').text($j('#webHookFormContents input.buildType_single:checked').length + subText);
    }

}

var preemptiveToolTip = "Preemptively sends credentials on first request. " +
    "Without preemption, the webhook will only send credentials when a 401 UNAUTHORIZED response is received " +
    "and the Server\'s Realm (if any) matches, which would require two requests for each webhook event.";

function populateWebHookAuthExtrasPane(webhookObj) {
    if (webhookObj.hasOwnProperty("authentication") && ProjectBuilds.templatesAndWebhooks.registeredAuthTypes.hasOwnProperty(webhookObj.authentication.type)) {
        $j('#extraAuthType').val(webhookObj.authentication.type);
        $j('#extraAuthParameters > tbody').empty();
        $j('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip + '">Preemptive</label></td>' +
            '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip + '" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
        $j('#extraAuthPreemptive').prop('checked', webhookObj.authentication.preemptive);
        $j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[webhookObj.authentication.type].parameters, function (index, paramObj) {
            $j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
        });
    } else {
        $j('#extraAuthType').val("");
        $j('#extraAuthParameters > tbody').empty();
    }
}

function populateWebHookParametersExtrasPane(webhook) {
    $j('#webhookParameters > tbody').empty();
    if (webhook.parameters.count == 0) {
        $j('#webhookParameters > thead').hide();
    } else {
        webhook.parameters.parameter.each(function (parameter) {
            $j('#webhookParameters > tbody').append('<tr><td>' + parameter.name + '</td><td style="width:40%;">' + parameter.value + '</td><td class="actionCell">edit</td><td class="actionCell">delete</td></tr>');
        });
        $j('#webhookParameters > thead').show();
    }
}

function populateWebHookHeadersExtrasPane(webhook) {
    $j('#webhookHeaders > tbody').empty();
    if (webhook.headers.count == 0) {
        $j('#webhookHeaders > thead').hide();
    } else {
        webhook.headers.header.each(function (header) {
            $j('#webhookHeaders > tbody').append('<tr><td>' + header.name + '</td><td style="width:40%;">' + header.value + '</td><td class="actionCell">edit</td><td class="actionCell">delete</td></tr>');
        });
        $j('#webhookHeaders > thead').show();
    }
}

function populateWebHookFiltersExtrasPane(webhook) {
    $j('#webhookFilters > tbody').empty();
    if (webhook.filters.count == 0) {
        $j('#webhookFilters > thead').hide();
    } else {
        webhook.filters.filter.each(function (filter) {
            $j('#webhookFilters > tbody').append('<tr><td>' + filter.value + '</td><td style="width:40%;">' + filter.regex + '</td><td class="actionCell">edit</td><td class="actionCell">delete</td></tr>');
        });
        $j('#webhookFilters > thead').show();
    }
}

function populateWebHookAuthExtrasPaneFromChange(webhookObj) {
    var authType = $j('#extraAuthType').val();
    if (authType === '') {
        $j('#extraAuthParameters > tbody').empty();
    } else {
        $j('#extraAuthParameters > tbody').empty();
        $j('#extraAuthParameters > tbody').append('<tr><td class="authParameterName"><label for="extraAuthPreemptive" title="' + preemptiveToolTip + '">Preemptive</label></td>' +
            '<td class="authParameterValueWrapper"><input title="' + preemptiveToolTip + '" type=checkbox name="extraAuthPreemptive" id="extraAuthPreemptive"></td></tr>');
        if (webhookObj.hasOwnProperty("authentication") && webhookObj.authentication.type == authType) {
            $j('#extraAuthPreemptive').prop('checked', webhookObj.authentication.preemptive);
            $j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function (index, paramObj) {
                $j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, true));
            });
        } else {
            $j('#extraAuthPreemptive').prop('checked', webhookObj, true);
            $j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes[authType].parameters, function (index, paramObj) {
                $j('#extraAuthParameters > tbody').append(buildWebHookAuthParameterHtml(paramObj, webhookObj, false));
            });
        }

    }
}

function buildWebHookAuthParameterHtml(paramObj, webhookObj, setValue) {
    var value = '';
    var requireText = '';
    if (setValue) {
        value = 'value="' + webhookObj.authentication.parameters[paramObj.key] + '" ';
    }

    if (paramObj.required) {
        requireText = '<span class="mandatoryAsterix" title="Mandatory field">*</span>';
    }
    return '<tr><td class="authParameterName"><label for="extraAuthParam_'
        + paramObj.key + '" title="' + paramObj.toolTip + '">'
        + paramObj.name + requireText + '</label></td><td class="authParameterValueWrapper">'
        + '<input title="' + paramObj.toolTip + '" type=text name="extraAuthParam_'
        + paramObj.key + '" ' + value + 'class="authParameterValue authParameterItemValue"></td></tr>';

}

function populateWebHookDialog(webhook) {
    populateBuildHistoryAjax('project:' + webhook.projectId + ',');
    //$j('#buildList').empty();
    // $j.each(ProjectBuilds.templatesAndWebhooks.projectWebhookConfig.webHookList, function(webHookKey, webhook){
    // 	if (id === webHookKey){

    $j("#viewRow_" + webhook.uniqueKey).animate({
        backgroundColor: "#ffffcc"
    }, 1000);

    $j("#editWebHookForm input[id='webHookId']").val(webhook.uniqueKey);
    $j('#webHookUrl').val(webhook.url);
    $j('#webHooksEnabled').prop('checked', webhook.enabled);
    $j.each(webhook.buildState, function (name, value) {
        $j('#' + value.type).prop('checked', value.enabled);
    });
    $j('#webHookFormContents select#payloadFormatHolder').val(webhook.template).change();

    $j('#buildTypeAll').prop('checked', webhook.buildTypes.allEnabled);
    $j('#buildTypeSubProjects').prop('checked', webhook.buildTypes.subProjectsEnabled);
    $j.each(webhook.builds, function () {
        var isChecked = '';
        if (this.enabled) {
            isChecked = ' checked';
        }
        var cbox = $j('<input' + isChecked + ' onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">');
        var label = $j('<label></label>');
        label.text(this.buildTypeName);
        label.prepend(cbox);
        var container = $j('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"></p>');
        container.append(label);
        $j('#buildList').append(container);
    });

    toggleAllBuildTypesSelected();
    populateWebHookAuthExtrasPane(webhook);
    populateWebHookParametersExtrasPane(webhook);
    populateWebHookHeadersExtrasPane(webhook);
    populateWebHookFiltersExtrasPane(webhook);
    $j('select#extraAuthType').change(function () {
        populateWebHookAuthExtrasPaneFromChange(webhook);
    });
    if ($j('#payloadFormatHolder').val()) {
        $j('#currentTemplateName').text(lookupTemplateName($j('#payloadFormatHolder').val()));
    } else {
        $j('#currentTemplateName').html("&nbsp;");
    }
    /*	}
    }); */
    updateSelectedBuildTypes();
    renderPreviewOnChange(); // Empty the div if no build selected.

}

function lookupTemplateName(templateId) {
    var name;
    $j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function (templateKey, template) {
        if (templateId === templateKey) {
            name = template.templateDescription;
            return false;
        }
    });
    return name;
}

function lookupTemplate(templateId) {
    var name;
    $j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function (templateKey, template) {
        if (templateId === templateKey) {
            name = template.templateId;
            return false;
        }
    });
    return name;
}

function lookupFormat(templateId) {
    var name;
    $j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function (templateKey, template) {
        if (templateId === templateKey) {
            name = template.formatShortName;
            return false;
        }
    });
    return name;
}

function lookupAuthType(authTypeValue) {
    if (authTypeValue) {
        return authTypeValue;
    }
    return null;
}

function lookupAuthEnabled(authTypeValue) {
    if (authTypeValue) {
        return true;
    }
    return false;
}

function lookupAuthParameters(authTypeValue, webHookForm) {
    var authParams = {};
    if (!lookupAuthEnabled(authTypeValue)) {
        return authParams;
    }
    $j.each(webHookForm, function (index, item) {
        var mySubStringOffSet = item.name.indexOf("extraAuthParam_");
        if (mySubStringOffSet != -1) {
            authParams[item.name.substring(15)] = item.value;
        }
    });
    return authParams;
}

$j(document).ready(function () {
    $j('#tab-container').easytabs({
        animate: false,
        updateHash: false
    });
    $j('#payloadFormatHolder').change(function () {
        var templateId = $j(this).val();
        $j.each(ProjectBuilds.templatesAndWebhooks.registeredTemplates.templateList, function (templateKey, template) {
            if (templateId === templateKey) {
                $j("#hookPane .buildState").each(function (thing, state) {
                    if (($j.inArray(state.id, template.supportedStates) >= 0) &&
                        ($j.inArray(state.id, template.supportedBranchStates) >= 0)) {
                        $j("td." + state.id).removeClass('buildStateDisabled');
                        $j("input#" + state.id).prop('disabled', false);
                        $j("#webhookPreviewBuildEvent option[value=" + state.id + "]").prop('disabled', false);
                    } else {
                        $j("td." + state.id).addClass('buildStateDisabled');
                        $j("input#" + state.id).prop('disabled', 'disabled');
                        $j("#webhookPreviewBuildEvent option[value=" + state.id + "]").prop('disabled', 'disabled');
                    }
                });
                return false;
            }
        });
    });

    $j('#extraAuthType').empty();
    $j('#extraAuthType').append($j("<option />").val("").text("No Authentication"));
    $j.each(ProjectBuilds.templatesAndWebhooks.registeredAuthTypes, function (key, authType) {
        $j('#extraAuthType').append($j("<option />").val(key).text(authType.description));
    });

    $j('select.templateAjaxRefresh').change(function () {
        renderPreviewOnChange()
    });

});

var restApiDetected = true;
/*
function populateBuildHistory() {
    
    <c:if test="${not haveBuild && haveProject}"> 
        populateBuildHistoryAjax("project:${projectExternalId},");
    </c:if>
    <c:if test="${haveBuild}">				
        populateBuildHistoryAjax("buildType:${buildExternalId},");
    </c:if>
}*/

function convertFormToWebHook(myJson) {
    var template = $j('#editWebHookForm select#payloadFormatHolder').val();
    console.log(template);
    var webhook = {
        id: myJson.id,
        projectId: myJson.projectId,
        href: myJson.href,
        url: $j('#webHookUrl').val(),
        enabled: $j('#editWebHookForm :input#webHooksEnabled').is(':checked'),
        template: $j('#editWebHookForm select#payloadFormatHolder').val(),
        authType: lookupAuthType($j("#editWebHookForm :input#extraAuthType").val()),
        authEnabled: lookupAuthEnabled($j("#editWebHookForm :input#extraAuthType").val()),
        authPreemptive: $j("#editWebHookForm :input#extraAuthPreemptive").is(':checked'),
        authParameters: lookupAuthParameters($j("#editWebHookForm :input#extraAuthType").val(), $j("#editWebHookForm :input.authParameterItemValue")),
        buildTypes: {
            allEnabled: $j('input#buildTypeAll').is(':checked'),
            subProjectsEnabled: $j('input#buildTypeSubProjects').is(':checked'),
            id: []
        },
        buildState: [
            { type: "buildSuccessful", enabled: $j("#editWebHookForm :input#buildSuccessful").is(':checked') },
            { type: "changesLoaded", enabled: $j("#editWebHookForm :input#changesLoaded").is(':checked') },
            { type: "buildFailed", enabled: $j("#editWebHookForm :input#buildFailed").is(':checked') },
            { type: "buildBroken", enabled: $j("#editWebHookForm :input#buildBroken").is(':checked') },
            { type: "buildStarted", enabled: $j("#editWebHookForm :input#buildStarted").is(':checked') },
            { type: "buildAddedToQueue", enabled: $j("#editWebHookForm :input#buildAddedToQueue").is(':checked') },
            { type: "buildRemovedFromQueue", enabled: $j("#editWebHookForm :input#buildRemovedFromQueue").is(':checked') },
            { type: "beforeBuildFinish", enabled: $j("#editWebHookForm :input#beforeBuildFinish").is(':checked') },
            { type: "responsibilityChanged", enabled: $j("#editWebHookForm :input#responsibilityChanged").is(':checked') },
            { type: "buildFixed", enabled: $j("#editWebHookForm :input#buildFixed").is(':checked') },
            { type: "buildInterrupted", enabled: $j("#editWebHookForm :input#buildInterrupted").is(':checked') },
            { type: "buildPinned", enabled: $j("#editWebHookForm :input#buildPinned").is(':checked') },
            { type: "buildUnpinned", enabled: $j("#editWebHookForm :input#buildUnpinned").is(':checked') },
            { type: "serviceMessageReceived", enabled: $j("#editWebHookForm :input#serviceMessageReceived").is(':checked') }
        ]
    };

    if (!webhook.buildTypes.allEnabled) {
        $j.each($j('.buildType_single'), function () {
            if ($j(this).prop('checked')) {
                webhook.buildTypes.id.add(this.id);
            }
        });
    }

    $j.each(webhook.buildTypes, function () {
        var isChecked = '';
        if (this.enabled) {
            isChecked = ' checked';
        }
        var cbox = $j('<input' + isChecked + ' onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" name="buildTypeId" value="' + this.buildTypeId + '"class="buildType_single">');
        var label = $j('<label></label>');
        label.text(this.buildTypeName);
        label.prepend(cbox);
        var container = $j('<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"></p>');
        container.append(label);
        $j('#buildList').append(container);
    });
    return webhook;
};

function renderPreviewOnChange() {
    if ($j('#payloadFormatHolder').val()) {
        $j('#currentTemplateName').text(lookupTemplateName($j('#payloadFormatHolder').val()));
    } else {
        $j('#currentTemplateName').html("&nbsp;");
    }

    if (
        $j("#webhookPreviewBuildEvent").val() &&
        $j("#webhookPreviewBuildId").val() &&
        $j("#payloadFormatHolder").val()
    ) {

        var selectedBuildState = $j('#webhookPreviewBuildEvent').val();
        var selectedBuildId = $j('#webhookPreviewBuildId').val();
        if (selectedBuildId === "") {
            $j('#webhookPreviewRendered').html("");
        } else {
            $j.ajax({
                url: "testWebHook.html?action=preview",
                type: "POST",
                dataType: 'json',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'text/html'
                },
                data: JSON.stringify({
                    "url": $j('#webHookUrl').val(),
                    "projectExternalId": $j("#editWebHookForm input[id='projectExternalId']").val(),
                    "uniqueKey": $j("#editWebHookForm input[id='webHookId']").val(),
                    "testBuildState": selectedBuildState,
                    "buildId": selectedBuildId,
                    "templateId": lookupTemplate($j('#payloadFormatHolder').val()),
                    "payloadFormat": lookupFormat($j('#payloadFormatHolder').val()),
                    "authType": lookupAuthType($j("#editWebHookForm :input#extraAuthType").val()),
                    "authEnabled": lookupAuthEnabled($j("#editWebHookForm :input#extraAuthType").val()),
                    "authPreemptive": $j("#editWebHookForm :input#extraAuthPreemptive").is(':checked'),
                    "authParameters": lookupAuthParameters($j("#editWebHookForm :input#extraAuthType").val(), $j("#editWebHookForm :input.authParameterItemValue")),
                    "configBuildStates": {
                        "BUILD_SUCCESSFUL": true,
                        "CHANGES_LOADED": false,
                        "BUILD_FAILED": true,
                        "BUILD_BROKEN": true,
                        "BUILD_STARTED": false,
                        "BUILD_ADDED_TO_QUEUE": false,
                        "BUILD_REMOVED_FROM_QUEUE": false,
                        "BEFORE_BUILD_FINISHED": false,
                        "RESPONSIBILITY_CHANGED": false,
                        "BUILD_FIXED": true,
                        "BUILD_INTERRUPTED": false,
                        "BUILD_PINNED": false,
                        "BUILD_UNPINNED": false,
                        "SERVICE_MESSAGE_RECEIVED": false
                    }
                }),
                success: (function (data) {
                    if (data.errored) {
                        $j('#webhookPreviewRendered').html(
                            "<b>An error occured building the payload preview</b><br>").append(
                                $j("<div style='padding:1em;'></div>").text(data.exception.detailMessage));
                    } else {
                        $j('#webhookPreviewRendered').html(data.html);

                        $j('#webhookPreviewRendered pre code').each(function (i, block) {
                            hljs.highlightBlock(block);
                        });
                    }
                })
            });
        }
    } else {
        $j('#webhookPreviewRendered').html();
    }

}

