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

        postData: function () {
            var dialog = this;
            // make a copy and send it 
            let webhook = JSON.parse(JSON.stringify(this.getStore().myJson));
            $j.ajax({
                url: window['base_uri'] + '/webhooks/save.html',
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