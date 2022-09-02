    <bs:dialog dialogId="deleteWebHookHeaderDialog"
               dialogClass="deleteWebHookHeaderDialog"
               title="Confirm Header deletion"
               closeCommand="${parameterConfigDialogScope}.DeleteDialog.cancelDialog()">
        <forms:multipartForm id="deleteWebHookHeaderForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return ${webhookConfigDialogScope}.DeleteHeaderDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteWebHookHeaderWarningMessage">Are you sure you want to delete Webhook Header '<span id="confirmationWebHookHeaderName"></span>'?
                        <div id="ajaxWebHookHeaderDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="headerId" name="headerId"/>
            <input type="hidden" id="projectId" name="projectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteWebHookHeaderDialogSubmit" label="Delete Header"/>
                <forms:cancel onclick="${webhookConfigDialogScope}.DeleteHeaderDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
