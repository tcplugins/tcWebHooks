    <bs:dialog dialogId="editWebHookDialog"
               dialogClass="editWebHookDialog"
               title="Edit Web Hook"
               closeCommand="${webhookConfigDialogScope}.EditDialog.cancelDialog()">
		  <forms:multipartForm id="editWebHookForm"
                             action="save.html"
                             targetIframe="hidden-iframe"
          					 onsubmit="return ${webhookConfigDialogScope}.EditDialog.doPost();">
            <div id='webHookFormContents'>

            		<div id="tab-container" class="tab-container">
								  <ul class='etabs'>
												   <li class='tab' id="hookPaneTab"><a href="#hookPane" class="active">WebHook Config</a></li>
												   <li class='tab' id="buildPaneTab"><a href="#buildPane">Builds (<span id="selectedBuildCount">all</span>)</a></li>
												   <li class='tab' id="extrasPaneTab"><a href="#extrasPane">Extra Config</a></li>
												   <li class='tab' id="templatePaneTab"><a href="#templatePane">Preview &amp; Test</a></li>
								  </ul>
						 <div class='panel-container'>
									<div id='hookPane' class="tabPane">
											<table style="border:none;">

												<tr style="border:none;">
													<td>URL:</td>
													<td colspan=2 style="padding-left:0.5em;"><input autocomplete="on" id="webHookUrl" name="URL" type=text maxlength=512 style="margin: 0pt; padding: 0pt; width: 36em;" autofocus></td>
												</tr>
												<tr>
													<td></td>
													<td colspan=2><span class="error" id="error_webHookUrl" style="margin-left: 0.5em;"></span></td>
												</tr>
												<tr style="border:none;">
													<td><label class="webhookEnabled" for="webHooksEnabled">Enabled:</label></td>
													<td style="padding-left:2px;" colspan=2><input id="webHooksEnabled" type=checkbox name="webHooksEnabled" style="margin-left:0.5em; padding-left:0.5em;"></td>
												</tr>

												<tr style="border:none;"><td style="vertical-align:text-top; padding-top:0.33em;">Payload Format:</td>
													<td colspan=2>
														<table style="padding:0; margin:0; left: 0px;" id="payloadFormatTable"><tbody style="padding:0; margin:0; left: 0px;">
																<tr style="padding:0; margin:0; left: 0px;">
																	<td style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
																		<select id="payloadFormatHolder" name="payloadFormatHolder" class="templateAjaxRefresh">
																		    <c:forEach items="${formatList}" var="template">
																				<option value="${template.templateId}"><c:out value="${template.description}" /></option>
																			</c:forEach>
																		</select>
																	</td></tr>
														</tbody></table>
													</td>
												</tr>
												<tr>
													<td></td>
													<td colspan=2><span class="error" id="error_payloadFormat" style="margin-left: 0.5em;"></span></td>
												</tr>

												<tr style="border:none;" class="onDuring">
													<td>Trigger on Events:</td>
													<td  class="buildAddedToQueue" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildAddedToQueue" name="BuildAddedToQueue"  type=checkbox />
														 Build Added to Queue</label>
													</td>
													<td class="buildRemovedFromQueue"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildRemovedFromQueue" name="BuildRemovedFromQueue" type=checkbox />
														 Build Removed from Queue by User</label>
													</td>
												</tr>
												<tr style="border:none;"><td>&nbsp;</td>
													<td  class="buildStarted" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildStarted" name="BuildStarted"  type=checkbox />
														 Build Started</label>
													</td>
													<td class="changesLoaded"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="changesLoaded" name="ChangesLoaded" type=checkbox />
														 Changes Loaded</label>
													</td>
												</tr>
												<tr style="border:none;"><td>&nbsp;</td>
													<td class="buildInterrupted" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildInterrupted" name="BuildInterrupted" type=checkbox />
														 Build Interrupted</label>
													</td>
													<td class="beforeBuildFinish"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="beforeBuildFinish" name="BeforeFinished" type=checkbox />
														 Build Almost Completed</label>
													</td>
												</tr>
												<tr style="border:none;"><td>&nbsp;</td>
													<td class="responsibilityChanged" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="responsibilityChanged" name="ResponsibilityChanged" type=checkbox />
														 Build Responsibility Changed</label>
													</td>
													<td class="serviceMessageReceived"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="serviceMessageReceived" name="ServiceMessageReceived" type=checkbox />
														 Service Message Received</label>
													</td>
													<td></td>
												</tr>
												<tr style="border:none;"><td>&nbsp;</td>
													<td class="buildPinned" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildPinned" name="BuildPinned" type=checkbox />
														 Build Pinned</label>
													</td>
													<td class="buildUnpinned"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildUnpinned" name="BuildUnpinned" type=checkbox />
														 Build Unpinned</label>
													</td>
												</tr>
												<tr style="border:none;" class="onCompletion"><td style="vertical-align:text-top;">On Completion:</td>
													<td colspan=2 >
														<table style="padding:0; margin:0; left: 0px;"><tbody style="padding:0; margin:0; left: 0px;">
																<tr style="padding:0; margin:0; left: 0px;"><td class="buildSuccessful" style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
																	<input onclick='doExtraCompleted();' class="buildState" id="buildSuccessful" name="BuildSuccessful" type=checkbox />
																	 Trigger when build is Successful</label>
																	</td></tr>
																<tr class="onBuildFixed" style="padding:0; margin:0; left: 0px;"><td class="buildFixed" style="padding:0; margin:0; padding-left: 2em; left: 0px;"><label style='white-space:nowrap;'>
																	<input class="buildStateFixed buildState" id="buildFixed" name="BuildFixed" type=checkbox />
																	 Only trigger when build changes from Failure to Success</label>
																	</td></tr>
																<tr style="padding:0; margin:0; left: 0px;"><td class="buildFailed" style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
																	<input onclick='doExtraCompleted();' class="buildState" id="buildFailed" name="BuildFailed" type=checkbox />
																	 Trigger when build Fails</label>
																	</td></tr>
																<tr class="onBuildFailed" style="padding:0; margin:0; left: 0px;"><td class="buildBroken" style="padding:0; margin:0; padding-left: 2em; left: 0px;"><label style='white-space:nowrap;'>
																	<input class="buildStateBroken buildState" id="buildBroken" name="BuildBroken" type=checkbox />
																	 Only trigger when build changes from Success to Failure</label>
																	</td></tr>
														</tbody></table>
													</td>
												</tr>
					    					</table>

					    			</div><!--hookPane -->

					    			<div id='buildPane' class="tabPane">
					    				<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input id="buildTypeAll" name="buildTypeAll" onclick="toggleAllBuildTypesSelected();" type=checkbox style="padding-right: 1em;" class="buildType_all"><strong>All Project Builds</strong></label></p>
					    				<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input id="buildTypeSubProjects" name="buildTypeSubProjects" onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" class="buildType_subprojects"><strong>All Sub-Project Builds</strong></label></p>
					            		<div id='buildList' style="padding:0;">
										<c:forEach items="${buildList}" var="build">
											<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;">
											<label>
											<input onclick="updateSelectedBuildTypes();" 
												type=checkbox style="padding-right: 1em;" 
												name="buildTypeId" 
												value="${build.externalId}" class="buildType_single">
												<c:out value="${build.name}" />
											</label>
											</p>
										</c:forEach>
										</div>
									</div><!--buildPane -->
									<div id='extrasPane' class="tabPane">
										<div id='extrasAuthPanel'>
											<h3>Authentication</h3>
											<div class="extraAuthParametersWrapper">
												<div class="error" id="error_authParameter"></div>
												<table style="border:none;" id='extraAuthParameters'>
													<thead>
													<tr><th class="authParameterName">Type</th><th class="authParameterValueWrapper">
									    				<select name="extraAuthType" id="extraAuthType" class="extraAuthRefresh authParameterValue"></select>
									    			</th></tr>
									    			</thead>
									    			<tbody></tbody>
												</table>
											</div>
											<h3>Parameters</h3>
											<div class="extraParametersWrapper">
												<div class="error" id="error_parameter"></div>
												<div id=""></div>
												<table class="parametersTable settings webhooktable" id="webhookParameters">
												<thead>
													<tr><th>Parameter Name</th><th colspan=3 style="width:60%;">Parameter Value</th></tr>
												</thead>
												<tbody>
												</tbody>
												<tfoot>
													<tr class="webHookRow"><td class="highlight newWebHookRow" colspan="4"><p onclick="${webhookConfigDialogScope}.showAddParameterDialog({'projectId':'${projectExternalId}', 'webhookId':'${hook.uniqueKey}', 'enableSecure': false});">Click to create new Parameter for this WebHook</p></td></tr>
												</tfoot>
												</table>
											</div>
											<h3>Headers</h3>
											<div class="extraHeadersWrapper">
												<div class="error" id="error_header"></div>
												<table class="parametersTable settings webhooktable" id="webhookHeaders">
												<thead>
													<tr class="webHookRow"><th>Header Name</th><th colspan=3 style="width:60%;">Header Value</th></tr>
												</thead>
												<tbody>
												</tbody>
												<tfoot>
													<tr class="webHookRow"><td class="highlight newWebHookRow" colspan="4"><p onclick="${webhookConfigDialogScope}.showAddHeaderDialog({'projectId':'${projectExternalId}', 'webhookId':'${hook.uniqueKey}'});">Click to create new Header for this WebHook</p></td></tr>
												</tfoot>
												</table>
											</div>
											<div style="float:right"><a href="#" data-dropdown="#dropdownQuickFilters">Quick Filters</a></div>
											<h3>Filters</h3>
											<div class="dropdown-menu dropdown-anchor-top-left" data-add-y="-5" id="dropdownQuickFilters">
												<ul>
													<li><a href="#" onclick="WebHooksPlugin.Configurations.addFilter('\$\{branchIsDefault\}', 'true');">Default VCS Branch only</a></li>
													<li><a href="#" onclick="WebHooksPlugin.Configurations.addFilter('\$\{buildIsPersonal\}', 'false');">Non-Personal Builds only</a></li>
												</ul>
											</div>
											<div class="extraFiltersWrapper">
												<div class="error" id="error_filter"></div>
												<table class="parametersTable settings webhooktable" id="webhookFilters">
												<thead>
													<tr><th>Filter Value</th><th colspan=3 style="width:60%;">Filter Regex</th></tr>
												</thead>
												<tbody>
												</tbody>
												<tfoot>
													<tr class="webHookRow"><td class="highlight newWebHookRow" colspan="4"><p onclick="${webhookConfigDialogScope}.showAddFilterDialog({'projectId':'${projectExternalId}', 'webhookId':'${hook.uniqueKey}'});">Click to create new Filter for this WebHook</p></td></tr>
												</tfoot>
												</table>
											</div>
						            	</div>
						            </div><!--extrasPane -->
						            <div id='templatePane' class="tabPane">
						            	<div id='templateLeftPanel'>
						            		<div class="webHookPreviewHeader">
							            		<table>
							            		<tr><td colspan=2>Select a build to use as example data for a webhook test execution:</td></tr>
							            		<tr><td>Template:</td><td><span id="currentTemplateName"></span></td></tr>
							            		<tr><td>Build:</td>
							            		<td> <select name="webhookPreviewBuildId" id="webhookPreviewBuildId" class="templateAjaxRefresh"></select></td></tr>
							            		<tr><td>Build Event:</td>
							            		<td> <select name="webhookPreviewBuildEvent" id="webhookPreviewBuildEvent" class="templateAjaxRefresh">
								            			<option value="buildAddedToQueue">Build Added to Queue</option>
								            			<option value="buildRemovedFromQueue">Build Removed from Queue by User</option>
								            			<option value="buildStarted">Build Started</option>
								            			<option value="changesLoaded">Changes Loaded</option>
								            			<option value="serviceMessageReceived">Service Message Received</option>
								            			<option value="buildInterrupted">Build Interrupted</option>
								            			<option value="beforeBuildFinish">Build Almost Completed</option>
								            			<option value="responsibilityChanged">Build Responsibility Changed</option>
								            			<option value="buildPinned">Build Pinned</option>
								            			<option value="buildUnpinned">Build Unpinned</option>
								            			<option value="buildSuccessful">Build Successful</option>
								            			<option value="buildFixed"> - Build changes from Failure to Success</option>
								            			<option value="buildFailed">Build Failed</option>
								            			<option value="buildBroken"> - Build changes from Success to Failure</option>
								            		</select>
								            	</td></tr></table>
											</div>
						            		<div id="webhookPreviewRendered"></div>
						            		<button id="webhookDialogPreview" class="btn btn_primary" onclick="return ${webhookConfigDialogScope}.EditDialog.executeWebHook();">Send Test WebHook for Build Event</button>
						            		<span class="testingLimitations"><a rel="noopener noreferrer" target="_blank" href="https://github.com/tcplugins/tcWebHooks/wiki/Testing-a-WebHook#limitations-with-webhook-testing">Testing limitations (opens in new window)</a></span>
											<div id="webhookTestProgress">
												<forms:progressRing progressTitle="Sending test webhook..."/>
												<span class="stage-status__description">Sending test webhook...</span>
											</div>
											<div id="webhookDialogAjaxResult"></div>
						            	</div>
						            </div><!--templatePane -->
					    	</div><!-- panel-container  -->
					</div>    <!-- tab-container -->

			</div> <!-- webHookFormContents -->

            <input type="hidden" id="projectExternalId" name="projectExternalId" value="<c:out value="${projectExternalId}"/>"/>
            <input type="hidden" id="webhookId" name="webhookId" value=""/>
            <input type="hidden" id="payloadFormat" name="payloadFormat" value=""/>
            <input type="hidden" id="payloadTemplate" name="payloadTemplate" value=""/>
            <input type="hidden" id="submitAction" name="submitAction" value=""/>

            <!-- input type="hidden" name="action" id="WebHookTemplateAction" value="editTemplateItem"/-->
            <div id="ajaxWebHookEditResult"></div>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="editTemplateItemDialogSubmit" label="Save Web Hook"/>
                <forms:cancel onclick="${webhookConfigDialogScope}.EditDialog.cancelDialog()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
