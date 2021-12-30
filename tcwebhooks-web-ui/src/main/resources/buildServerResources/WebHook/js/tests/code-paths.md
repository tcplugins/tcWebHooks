
# Javascript Code Paths for Editing WebHooks

### Load WebHook Edit Dialog

- WebHooksPlugin.Configurations.showEditDialog
  - Configurations.EditDialog.showDialog
    - EditDialog.resetAndShow(data);
      - EditDialog.disableAndClearCheckboxes();
      - EditDialog.cleanFields(data);
        - EditDialog.cleanErrors();
      - EditDialog.showCentered();
    - EditDialog.getWebHookData(data.projectId, data.webhookId, action);
      - EditDialog.getData(projectId, webhookId, action);
        - EditDialog.handleGetSuccess(action);
          - WebHooksPlugin.populateWebHookDialog(this.getStore().myJson);
            - WebHooksPlugin.populateBuildHistoryAjax
            - WebHooksPlugin.toggleAllBuildTypesSelected();
              - WebHooksPlugin.updateSelectedBuildTypes();
            - WebHooksPlugin.populateWebHookAuthExtrasPane(webhook);
            - WebHooksPlugin.populateWebHookParametersExtrasPane(webhook);
            - WebHooksPlugin.populateWebHookHeadersExtrasPane(webhook);
            - WebHooksPlugin.populateWebHookFiltersExtrasPane(webhook);
            - WebHooksPlugin.updateSelectedBuildTypes();
            - WebHooksPlugin.renderPreviewOnChange(); // Empty the div if no build selected.
    - EditDialog.highlightRow($j("#viewRow_" + data.webhookId), this);

### Save New Webhook

- 

### Save Edited Webhook