    <bs:dialog dialogId="deleteWebHookDialog"
               dialogClass="deleteWebHookDialog"
               title="Confirm Webhook deletion"
               closeCommand="${webhookConfigDialogScope}.DeleteDialog.cancelDialog()">
        <forms:multipartForm id="deleteWebHookForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return ${webhookConfigDialogScope}.DeleteDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteWebHookWarningMessage">Are you sure you want to delete this Webhook?
                        <div id="ajaxWebHookDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="webHookId" name="webHookId"/>
            <input type="hidden" id="projectId" name="projectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteWebHookDialogSubmit" label="Delete Web Hook"/>
                <forms:cancel onclick="${webhookConfigDialogScope}.DeleteDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
