    <bs:dialog dialogId="deleteWebHookParameterDialog"
               dialogClass="deleteWebHookParameterDialog"
               title="Confirm Parameter deletion"
               closeCommand="WebHooksPlugin.Parameters.DeleteDialog.cancelDialog()">
        <forms:multipartForm id="deleteWebHookParameterForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.Parameters.DeleteDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td id="deleteWebHookParameterWarningMessage">Are you sure you want to delete Webhook Parameter '<span id="confirmationWebHookParameterName"></span>'?
                        <div id="ajaxWebHookParameterDeleteResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="parameterId" name="parameterId"/>
            <input type="hidden" id="projectId" name="projectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="deleteWebHookParameterDialogSubmit" label="Delete Parameter"/>
                <forms:cancel onclick="WebHooksPlugin.Parameters.DeleteDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
