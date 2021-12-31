    <bs:dialog dialogId="editWebHookFilterDialog"
               dialogClass="editWebHookFilterDialog"
               title="Edit WebHook Filter"
               closeCommand="WebHooksPlugin.Filters.EditDialog.cancelDialog()">
        <forms:multipartForm id="editWebHookFilterForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.Configurations.EditFilterDialog.doPost();">

            <table class="runnerFormTable">
            	
                <tr><td colspan="3"><div id="ajaxWebHookFilterEditResult"></div></td></tr>
            	<tr><td>Value:</td><td><input type=text length="256" size=40 class="editWebHookFilterFormField" id="filterDialogValue" name="filterDialogValue"></td></tr>
            	<tr><td>Regex:</td><td><input type=text length="256" size=40 class="editWebHookFilterFormField" id="filterDialogRegex" name="filterDialogRegex"></td></tr>
            	<tr><td>Enabled:</td><td><input type=checkbox class="editWebHookFilterFormField" id="filterDialogEnabled" name="filterDialogEnabled"></td></tr>
            </table>
            <input type="hidden" class="editWebHookFilterFormField" id="filterAction" name="filterAction"/>
            <input type="hidden" class="editWebHookFilterFormField" id="filterId" name="filterId"/>
            <input type="hidden" class="editWebHookFilterFormField" id="filterHref" name="filterHref"/>
            <input type="hidden" class="editWebHookFilterFormField" id="filterProjectId" name="filterProjectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editWebHookFilterDialogSubmit" label="Edit Filter"/>
                <forms:cancel onclick="WebHooksPlugin.Configurations.EditFilterDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
