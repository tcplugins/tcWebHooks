    <bs:dialog dialogId="deleteWebHookFilterDialog"
               dialogClass="deleteWebHookFilterDialog"
               title="Confirm Filter deletion"
               closeCommand="${parameterConfigDialogScope}.DeleteDialog.cancelDialog()">
        <forms:multipartForm id="deleteWebHookFilterForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return ${webhookConfigDialogScope}.DeleteFilterDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteWebHookFilterWarningMessage">Are you sure you want to delete Webhook Filter '<span id="confirmationWebHookFilterValue"></span>'?
                        <div id="ajaxWebHookFilterDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="filterId" name="filterId"/>
            <input type="hidden" id="projectId" name="projectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteWebHookFilterDialogSubmit" label="Delete Filter"/>
                <forms:cancel onclick="${webhookConfigDialogScope}.DeleteFilterDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
