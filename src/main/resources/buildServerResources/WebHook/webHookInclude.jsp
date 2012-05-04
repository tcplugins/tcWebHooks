
	    <!-- <p><label for="webHookEnabled" style="width:30em;"><input id="webHookEnabled" type="checkbox" ${webHooksEnabledAsChecked}/> Process WebHooks for this project</label></p>-->
	    <br/>
	    <table id="webHookTable" class="settings">
	   		<thead>
		   		<tr style="background-color: rgb(245, 245, 245);">
					<th class="name">URL</th>
					<th class="name">Format</th>
					<th class="value" style="width:20%;" colspan="3">Enabled</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${webHookList}" var="hook">
				<tr id="viewRow_${hook.uniqueKey}">
					<td class="name highlight" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}');"><c:out value="${hook.url}" /></td>
						<c:forEach items="${formatList}" var="format">
							<c:if test="${format.formatShortName == hook.payloadFormat}">
								<td class="value highlight" style="width:15%;" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}');"><c:out value="${format.formatDescription}" /></td>
							</c:if>
						</c:forEach>
					<td class="value highlight" style="width:15%;" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}');"><c:out value="${hook.enabledListAsString}" /></td>
					<td class="edit highlight"><a onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}');" href="javascript://">edit</a></td>
					<td class="edit highlight"><a onclick='BS.WebHookForm.removeWebHook("${hook.uniqueKey}");' href="javascript://">delete</a></td>
				</tr> 
				<tr style="display:none;" id="editRow_${hook.uniqueKey}">
					<td colspan="4" id="wrapper_${hook.uniqueKey}">
					<form id="form_${hook.uniqueKey}">
						<input id="url_${hook.uniqueKey}" name="URL" type=text size=64 maxlength=512 value="<c:out value="${hook.url}"/>" />
						<input id="webHooksEnabled_${hook.uniqueKey}" type=checkbox ${hook.webHookEnabledAsChecked}/>
						<!--input id="selectAll_${hook.uniqueKey}" type=checkbox ${hook.stateAllAsChecked}/-->
						<input id="BuildStarted_${hook.uniqueKey}" value="BuildStarted" name="BuildStarted"  type=checkbox ${hook.stateBuildStartedAsChecked}/>
						<input id="BuildFinished_${hook.uniqueKey}" value="BuildFinished" name="BuildFinished" type=checkbox ${hook.stateBuildFinishedAsChecked}/>
						<input id="BuildInterrupted_${hook.uniqueKey}" value="BuildInterrupted" name="BuildInterrupted" type=checkbox ${hook.stateBuildInterruptedAsChecked}/>
						<input id="BeforeFinished_${hook.uniqueKey}" value="BeforeFinished" name="BeforeFinished" type=checkbox ${hook.stateBeforeFinishedAsChecked}/>
						<input id="ResponsibilityChanged_${hook.uniqueKey}" value="ResponsibilityChanged" name="ResponsibilityChanged" type=checkbox ${hook.stateResponsibilityChangedAsChecked}/>
						<input id="BuildSuccessful_${hook.uniqueKey}" value="BuildSuccessful" name="BuildSuccessful" type=checkbox ${hook.stateBuildSuccessfulAsChecked}/>
						<input id="BuildFailed_${hook.uniqueKey}" value="BuildFailed" name="BuildFailed" type=checkbox ${hook.stateBuildFailedAsChecked}/>
						<input id="BuildFixed_${hook.uniqueKey}" value="BuildFixed" name="BuildFixed" type=checkbox ${hook.stateBuildFixedAsChecked}/>
						<input id="BuildBroken_${hook.uniqueKey}" value="BuildBroken" name="BuildBroken" type=checkbox ${hook.stateBuildBrokenAsChecked}/>
						<input id="payloadFormat_${hook.uniqueKey}" name="payloadFormat2" type="hidden" value="${hook.payloadFormat}" />
			<!-- ${ hook.payloadFormat} -->
						<c:forEach items="${formatList}" var="format">
							<c:if test="${format.formatShortName == hook.payloadFormat}">
								<input id="payloadFormat_${format.formatShortName}_${hook.uniqueKey}" name="payloadFormat" type="radio" value="${format.formatShortName}" checked />
							</c:if>
							<c:if test="${format.formatShortName != hook.payloadFormat}">
								<input id="payloadFormat_${format.formatShortName}_${hook.uniqueKey}" name="payloadFormat" type="radio" value="${format.formatShortName}" />
							</c:if>
						</c:forEach>
    				</form>
					</td>
				</tr>
			</c:forEach>
				<tr>
					<td colspan="5" class="highlight"><p onclick="BS.EditWebHookDialog.showDialog('new');" class="addNew">Click to create new WebHook for this project</p></td>
				</tr>
				<tr style="display:none;" id="editRow_new">
					<td colspan="4" id="wrapper_new">
					<form id="form_new">
						<input id="url_new" name="URL" type=text size=64 maxlength=512 />
						<input id="webHooksEnabled_new" type=checkbox checked />
						<!--input id="selectAll_new" type=checkbox checked /-->
						<input id="BuildStarted_new" value="BuildStarted" name="BuildStarted"  type=checkbox checked />
						<input id="BuildFinished_new" value="BuildFinished" name="BuildFinished" type=checkbox checked />
						<input id="BuildInterrupted_new" value="BuildInterrupted" name="BuildInterrupted" type=checkbox checked />
						<input id="BeforeFinished_new" value="BeforeFinished" name="BeforeFinished" type=checkbox checked />
						<input id="ResponsibilityChanged_new" value="ResponsibilityChanged" name="ResponsibilityChanged" type=checkbox checked />
						<input id="BuildSuccessful_new" value="BuildSuccessful" name="BuildSuccessful" type=checkbox checked />
						<input id="BuildFixed_new" value="BuildFixed" name="BuildFixed" type=checkbox />
						<input id="BuildFailed_new" value="BuildFailed" name="BuildFailed" type=checkbox checked />
						<input id="BuildBroken_new" value="BuildBroken" name="BuildBroken" type=checkbox />
						<input id="payloadFormat_new" name="payloadFormat" type="hidden" value="${format.formatShortName}" />
    				</form>
					</td>
				</tr>
			</tbody>
		</table>
      <div id="editWebHookDialog" class="editParameterDialog modalDialog"  style="width:50em;">
        <div class="dialogHeader">
          <div class="closeWindow">
            <a title="Close dialog window" href="javascript://" showdiscardchangesmessage="false"
               onclick="BS.EditWebHookDialog.cancelDialog()">close</a>
          </div>
          <h3 id="webHookDialogTitle" class="dialogTitle"></h3>

        </div>

        <div class="modalDialogBody">
          <form id='WebHookForm' action="ajaxEdit.html?projectId=${projectId}"
                method="post" onsubmit="return BS.WebHookForm.saveWebHook();">
            <div id='webHookFormContents'>
            

						<table style="border:none;">
							
							<tr style="border:none;">
								<td>URL:</td>
								<td colspan=2><input id="webHookUrl" name="URL" type=text maxlength=512 style="margin: 0pt; padding: 0pt; width: 36em;"/></td>
							</tr>
							<tr>
								<td></td>
								<td colspan=2><span class="error" id="error_webHookUrl" style="margin-left: 0.5em;"></span></td>
							</tr>
							<tr style="border:none;">
								<td><label for="webHooksEnabled">Enabled:</label></td>
								<td style="padding-left:3px;" colspan=2><input id="webHooksEnabled" type=checkbox name="webHooksEnabled"/></td>
							</tr>
							<tr style="border:none;">
								<td>Trigger on Events:</td>
								<td style="padding-left:3px;"><label style='white-space:nowrap;'>
									<input onclick='selectBuildState();' class="buildState" id="BuildStarted" name="BuildStarted"  type=checkbox />
									 Build Started</label>
								</td>
								<td><label style='white-space:nowrap;'>
									<input onclick='selectBuildState();' class="buildState" id="BuildInterrupted" name="BuildInterrupted" type=checkbox />
									 Build Interrupted</label>
								</td>
							</tr>
							<tr style="border:none;"><td>&nbsp;</td>
								<td style="padding-left:3px;"><label style='white-space:nowrap;'>
									<input onclick='selectBuildState();' class="buildState" id="BeforeFinished" name="BeforeFinished" type=checkbox />
									 Build Almost Completed</label>
								</td>
								<td><label style='white-space:nowrap;'>
									<input onclick='selectBuildState();' class="buildState" id="ResponsibilityChanged" name="ResponsibilityChanged" type=checkbox />
									 Build Responsibility Changed</label>
								</td>
							</tr>

							<tr style="border:none;" class="onCompletion"><td style="vertical-align:text-top; padding-top:0.33em;">On Completion:</td>
								<td colspan=2 >
									<table style="padding:0; margin:0; left: 0px;"><tbody style="padding:0; margin:0; left: 0px;">
											<tr style="padding:0; margin:0; left: 0px;"><td style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
												<input onclick='doExtraCompleted();' class="buildState" id="BuildSuccessful" name="BuildSuccessful" type=checkbox />
												 Trigger when build is Successful</label>
												</td></tr>
											<tr class="onBuildFixed" style="padding:0; margin:0; left: 0px;"><td style="padding:0; margin:0; padding-left: 2em; left: 0px;"><label style='white-space:nowrap;'>
												<input class="buildStateFixed" id="BuildFixed" name="BuildFixed" type=checkbox />
												 Only trigger when build changes from Failure to Success</label>
												</td></tr>
											<tr style="padding:0; margin:0; left: 0px;"><td style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
												<input onclick='doExtraCompleted();' class="buildState" id="BuildFailed" name="BuildFailed" type=checkbox />
												 Trigger when build Fails</label>
												</td></tr>
											<tr class="onBuildFailed" style="padding:0; margin:0; left: 0px;"><td style="padding:0; margin:0; padding-left: 2em; left: 0px;"><label style='white-space:nowrap;'>
												<input class="buildStateBroken" id="BuildBroken" name="BuildBroken" type=checkbox />
												 Only trigger when build changes from Success to Failure</label>
												</td></tr>
									</tbody></table>
								</td>
							</tr>

							<tr style="border:none;"><td style="vertical-align:text-top; padding-top:0.33em;">Payload Format:</td>
								<td colspan=2>
									<table style="padding:0; margin:0; left: 0px;" id="payloadFormatTable"><tbody style="padding:0; margin:0; left: 0px;">
										<c:forEach items="${formatList}" var="format">
											<tr style="padding:0; margin:0; left: 0px;"><td style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
												<input style="vertical-align:text-bottom;" class="payloadFormat" id="payloadFormat_${format.formatShortName}" name="payloadFormat" type="radio" value="${format.formatShortName}" />
												${format.formatDescription}</label>
												</td></tr>
										</c:forEach>
									</tbody></table>
								</td>
							</tr>
							<tr>
								<td></td>
								<td colspan=2><span class="error" id="error_payloadFormat" style="margin-left: 0.5em;"></span></td>
							</tr>
    					</table>            
            
            <!--
            <label class="editParameterLabel" for="parameterName">Name: <span class="mandatoryAsterix" title="Mandatory field">*</span></label>
			<input type="text" name="parameterName" id="parameterName" size="" maxlength="512" value="" class="textfield" style="margin:0; padding:0; width:22em;"   >

            <span class="error" id="error_parameterName" style="margin-left: 5.5em;"></span>

            <div class="clr" style="height:3px;"></div>
            <label class="editParameterLabel" for="parameterValue">Value:</label>
			<input type="text" name="parameterValue" id="parameterValue" size="" maxlength="512" value="" class="textfield" style="margin:0; padding:0; width:22em;"   >
			-->
			</div>

            <div class="popupSaveButtonsBlock">
              <a href="javascript://" showdiscardchangesmessage="false" onclick="BS.EditWebHookDialog.cancelDialog()"
                 class="cancel">Cancel</a>
              <input class="submitButton" type="submit" value="Save"/>
			<img id="webHookSaving" style="display: none; padding-top: 0.1em; float: right;" src="../img/ajax-loader.gif" width="16" height="16" alt="Please wait..." title="Please wait..."/>

              <br clear="all"/>
            </div>

            <input type="hidden" id="webHookId" name="webHookId" value=""/>
            <input type="hidden" id="submitAction" name="submitAction" value=""/>


          </form>
	    </div>
    </div>
          
