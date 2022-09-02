// A version of WebHooksPlugin.Configurations that talks to webhooks/save.html
WebHooksPlugin.Configurations.WithoutRestApi = OO.extend(WebHooksPlugin.Configurations, {
    showAddDialog: function (data, tab) {
        this.EditDialog.showDialog("Add Web Hook", 'addWebHook', data, tab);
    },
    showEditDialog: function (data, tab) {
        this.EditDialog.showDialog("Edit Web Hook", 'updateWebHook', data, tab);
    },
    showDeleteDialog: function (data) {
        this.DeleteDialog.showDialog("Delete Web Hook", 'deleteWebHook', data);
    },
    DeleteDialog: OO.extend(WebHooksPlugin.Configurations.DeleteDialog, {

        doPost: function () {
            this.cleanErrors();

            var dialog = this;
            var webhookId = $j("#deleteWebHookForm input[id='webHookId']").val()
            var projectId = $j("#deleteWebHookForm input[id='projectId']").val()

            $j.ajax({
                url: window['base_uri'] + '/webhooks/edit.html?action=delete&projectId=' + projectId + '&webHookId=' + webhookId,
                type: "POST",
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
    }),
    EditDialog: OO.extend(WebHooksPlugin.Configurations.EditDialog, {
        afterShow: function () {
            // Update the form, so that the submit button calls 
            // the doPost method on this dialog, not the base dialog.
            $(this.formElement()).setAttribute("onsubmit", "return WebHooksPlugin.Configurations.WithoutRestApi.EditDialog.doPost()");
        },

        doPost: function () {
            this.updateJsonDataFromForm();
            this.postData();
            return false;
        },

        getData: function (projectId, webhookId, action) { 
            var dialog = this;
            $j.ajax({
                url: window['base_uri'] + '/webhooks/edit.html?projectId=' + projectId + '&webHookId=' + webhookId,
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
                    dialog.handleAjaxError(dialog, response);
                }
            });
        }, 

        postData: function () {
            var dialog = this;
            // make a copy and send it 
            let webhook = JSON.parse(JSON.stringify(this.getStore().myJson));
            let url = webhook.id === "_new" ? window['base_uri'] + '/webhooks/edit.html?action=add' : window['base_uri'] + '/webhooks/edit.html'
            $j.ajax({
                url: url,
                type: "POST",
                data: JSON.stringify(webhook),
                dataType: 'json',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                success: function (response) {
                    if (response.errors) {
                        $j.each(response.errors, function(index, errorMsg){
                            dialog.ajaxError(errorMsg)
                        });
                    } else {
                        dialog.closeSuccess([{ id: 'viewRow_' + response.id }], dialog);
                    }
                        
                },
                error: function (response) {
                    console.log(response);
                    dialog.handleAjaxError(dialog, response);
                }
            });
        },
    }
)});