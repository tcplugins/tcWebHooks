    <bs:dialog dialogId="noRestApiDialog"
               dialogClass="noRestApiDialog"
               title="No WebHoooks REST API Plugin detected"
               closeCommand="WebHooksPlugin.NoRestApiDialog.close()">
        <forms:multipartForm id="noRestApiForm"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHooksPlugin.NoRestApi.NoRestApiDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>The WebHoooks REST API Plugin was not detected. This page requires
                		the WebHooks REST API to provide editing of WebHook Parameters.<p>
                		Please install the <a href="https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API">WebHooks REST API plugin</a> to use this page.
                </td></tr>
            </table>
            <div class="popupSaveButtonsBlock">
                <forms:cancel onclick="WebHooksPlugin.NoRestApi.NoRestApiDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>