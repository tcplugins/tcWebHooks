    <bs:dialog dialogId="editWebHookParameterDialog"
               dialogClass="editWebHookParameterDialog"
               title="Edit WebHook Parameter"
               closeCommand="${parameterConfigDialogScope}.EditDialog.cancelDialog()">
        <forms:multipartForm id="editWebHookParameterForm"
                             action="ajaxEdit.html?projectId=${projectId}"
                             targetIframe="hidden-iframe"
                             onsubmit="return ${parameterConfigDialogScope}.EditDialog.doPost();">

            <table class="runnerFormTable">
            	
                <tr><td colspan="3"><div id="ajaxWebHookParameterEditResult"></div></td></tr>
                <tr><td>Type:</td><td><select id="parameterDialogType" class="editWebHookParameterFormField" onchange="WebHooksPlugin.Parameters.EditDialog.toggleHidden();"><option value="text">Text</option><option value="password">Password</option></select></td></tr>
            	<tr><td>Name:</td><td><input type=text length="256" size=40 class="editWebHookParameterFormField" id="parameterDialogName" name="parameterDialogName"></td></tr>
            	<tr><td>Value:</td><td><input type=text length="256" size=40 class="editWebHookParameterFormField" id="parameterDialogValue" name="parameterDialogValue"></td></tr>
            	<tr><td>Visibility:</td><td><select id="parameterDialogVisibility" class="editWebHookParameterFormField" width=40><option value="template">Available as Template variable</option><option value="legacy">Include in legacy payloads and template variables</option></select></td></tr>
                <tr><td>Forced Resolve:</td><td><select id="parameterDialogResolve" class="editWebHookParameterFormField" width=40><option value="unforced">Don't force TeamCity to resolve variable early</option><option value="forced">Attempt to resolve variable contents early via TeamCity</option></select></td></tr>
                <tr><td>Resolve with:</td><td><select id="parameterDialogTemplateEngine" class="editWebHookParameterFormField" width=40><option value="STANDARD">Standard Template Engine</option><option value="VELOCITY">Velocity Template Engine</option></select></td></tr>
            </table>
            <input type="hidden" class="editWebHookParameterFormField" id="parameterAction" name="parameterAction"/>
            <input type="hidden" class="editWebHookParameterFormField" id="parameterId" name="parameterId"/>
            <input type="hidden" class="editWebHookParameterFormField" id="parameterHref" name="parameterHref"/>
            <input type="hidden" class="editWebHookParameterFormField" id="parameterProjectId" name="parameterProjectId"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editWebHookParameterDialogSubmit" label="Edit Parameter"/>
                <forms:cancel onclick="${parameterConfigDialogScope}.EditDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
