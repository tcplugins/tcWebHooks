    <bs:dialog dialogId="editWebHookHeaderDialog"
               dialogClass="editWebHookHeaderDialog"
               title="Edit WebHook Header"
               closeCommand="WebHooksPlugin.Headers.EditDialog.cancelDialog()">
        <forms:multipartForm id="editWebHookHeaderForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.Configurations.EditHeaderDialog.doPost();">

            <table class="runnerFormTable">
            	
                <tr><td colspan="3"><div id="ajaxWebHookHeaderEditResult"></div></td></tr>
            	<tr><td>Name:</td><td><input type=text length="256" size=40 class="editWebHookHeaderFormField" id="headerDialogName" name="headerDialogName"></td></tr>
            	<tr><td>Value:</td><td><input type=text length="256" size=40 class="editWebHookHeaderFormField" id="headerDialogValue" name="headerDialogValue"></td></tr>
            </table>
            <input type="hidden" class="editWebHookHeaderFormField" id="headerAction" name="headerAction"/>
            <input type="hidden" class="editWebHookHeaderFormField" id="headerId" name="headerId"/>
            <input type="hidden" class="editWebHookHeaderFormField" id="headerHref" name="headerHref"/>
            <input type="hidden" class="editWebHookHeaderFormField" id="headerProjectId" name="headerProjectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editWebHookHeaderDialogSubmit" label="Edit Header"/>
                <forms:cancel onclick="WebHooksPlugin.Configurations.EditHeaderDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
