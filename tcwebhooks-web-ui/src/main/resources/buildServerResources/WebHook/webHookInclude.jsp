		<script>
        var ProjectBuilds = ${projectWebHooksAsJson};
        </script>

	    <!-- <p><label for="webHookEnabled" style="width:30em;"><input id="webHookEnabled" type="checkbox" ${webHooksEnabledAsChecked}/> Process WebHooks for this project</label></p>-->
	    <br/>
	    <table id="webHookTable" class="settings">
	   		<thead>
		   		<tr style="background-color: rgb(245, 245, 245);">
					<th class="name">URL</th>
					<th class="name">Format</th>
					<th class="name">Build Events</th>
					<th class="value" style="width:20%;" colspan="3">Enabled Builds</th>
			</tr>
			</thead>
			<tbody>
				<tr id="viewRow_template" class="webHookRowTemplate">
					<td class="name highlight webHookRowItemUrl">URL</td>
					<td class="value highlight webHookRowItemFormat" style="width:15%;">Format</td>
					<td class="value highlight webHookRowItemEvents" style="width:15%;">Events</td>
					<td class="value highlight webHookRowItemBuilds" style="width:15%;">Builds</td>
					<td class="edit highlight webHookRowItemEdit"><a href="javascript://">edit</a></td>
					<td class="edit highlight webHookRowItemDelete"><a ref="javascript://">delete</a></td>
				</tr> 	
	
			<c:forEach items="${webHookList.webHookList}" var="hook">
				
				<tr id="viewRow_${hook.uniqueKey}" class="webHookRow">
					<td class="name highlight" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}','#hookPane');"><c:out value="${hook.url}" /></td>
					<td class="value highlight" style="width:15%;" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}','#hookPane');"><c:out value="${hook.payloadFormatForWeb}" /></td>
					<td class="value highlight" style="width:15%;" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}','#hookPane');"><c:out value="${hook.enabledEventsListForWeb}" /></td>
					<td class="value highlight" style="width:15%;" onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}','#buildPane');"><c:out value="${hook.enabledBuildsListForWeb}" /></td>
					<td class="edit highlight"><a onclick="BS.EditWebHookDialog.showDialog('${hook.uniqueKey}','#hookPane');" href="javascript://">edit</a></td>
					<td class="edit highlight"><a onclick="BS.WebHookForm.removeWebHook('${hook.uniqueKey}','#hookPane');" href="javascript://">delete</a></td>
				</tr> 
			</c:forEach>
			</tbody>
			<tfoot>
				<tr>
		<c:choose>  
    		<c:when test="${haveBuild}"> 
					<td colspan="6" class="highlight newWebHookRow"><p onclick="BS.EditWebHookDialog.showDialog('new');" class="addNew">Click to create new WebHook for this build</p></td>
         	</c:when>  
         	<c:otherwise>  
					<td colspan="6" class="highlight newWebHookRow"><p onclick="BS.EditWebHookDialog.showDialog('new');" class="addNew">Click to create new WebHook for this project</p></td>
         	</c:otherwise>  
		</c:choose> 
				</tr>
			</tfoot>
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
            
            		<div id="tab-container" class="tab-container">
								  <ul class='etabs'>
												   <li class='tab'><a href="#hookPane" class="active">WebHook Config</a></li>
												   <li class='tab'><a href="#buildPane">Builds (<span id="selectedBuildCount">all</span>)</a></li>
												   <li class='tab'><a href="#templatePane">Payload Content</a></li>
								  </ul>
						 <div class='panel-container'>
									<div id='hookPane'>
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
												
												<tr style="border:none;"><td style="vertical-align:text-top; padding-top:0.33em;">Payload Format:</td>
													<td colspan=2>
														<table style="padding:0; margin:0; left: 0px;" id="payloadFormatTable"><tbody style="padding:0; margin:0; left: 0px;">
																<tr style="padding:0; margin:0; left: 0px;">
																	<td style="padding:0; margin:0; left: 0px;"><label style='white-space:nowrap;'>
																		<select id="payloadFormatHolder" name="payloadFormatHolder">
																		    <c:forEach items="${formatList}" var="template">
																				<option value="${template.templateFormatCombinationKey}">${template.description}</option>
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
												
												<tr style="border:none;">
													<td>Trigger on Events:</td>
													<td  class="buildStarted" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildStarted" name="BuildStarted"  type=checkbox />
														 Build Started</label>
													</td>
													<td class="buildInterrupted"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="buildInterrupted" name="BuildInterrupted" type=checkbox />
														 Build Interrupted</label>
													</td>
												</tr>
												<tr style="border:none;"><td>&nbsp;</td>
													<td class="beforeBuildFinish" style="padding-left:3px;"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="beforeBuildFinish" name="BeforeFinished" type=checkbox />
														 Build Almost Completed</label>
													</td>
													<td class="responsibilityChanged"><label style='white-space:nowrap;'>
														<input onclick='selectBuildState();' class="buildState" id="responsibilityChanged" name="ResponsibilityChanged" type=checkbox />
														 Build Responsibility Changed</label>
													</td>
												</tr>
					
												<tr style="border:none;" class="onCompletion"><td style="vertical-align:text-top; padding-top:0.33em;">On Completion:</td>
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
					    			
					    			<div id='buildPane'>
					    				<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input name="buildTypeAll" onclick="toggleAllBuildTypesSelected();" type=checkbox style="padding-right: 1em;" class="buildType_all"><strong>All Project Builds</strong></label></p>
					    				<p style="border-bottom:solid 1px #cccccc; margin:0; padding:0.5em;"><label><input id="buildTypeSubProjects" name="buildTypeSubProjects" onclick="updateSelectedBuildTypes();" type=checkbox style="padding-right: 1em;" class="buildType_subprojects"><strong>All Sub-Project Builds</strong></label></p>
					            		<div id='buildList' style="overflow:auto; padding:0;">
						            	</div>
						            </div><!--buildPane -->
						            <div id='templatePane'>
						            	<div id='templateLeftPanel'>
						            		<div id="currentTemplateName"></div>
						            		<div>Build History: <select name="currentTemplateBuildId" id="currentTemplateBuildId" class="templateAjaxRefresh"></select></div>
						            		<div>
							            		Build Event: <select name="currentTemplateBuildEvent" id="currentTemplateBuildEvent" class="templateAjaxRefresh">
							            			<option value="buildStarted">Build Started</option>
							            			<option value="buildInterrupted">Build Interrupted</option>
							            			<option value="beforeBuildFinish">Build Almost Completed</option>
							            			<option value="responsibilityChanged">Build Responsibility Changed</option>
							            			<option value="buildSuccessful">Build Successful</option>
							            			<option value="buildFixed"> - Build changes from Failure to Success</option>
							            			<option value="buildFailed">Build Failed</option>
							            			<option value="buildBroken"> - Build changes from Success to Failure</option>
							            		</select> 
							            		<!--label><checkbox id="currentTemplateCustomise" disabled>&nbsp;Customise Template</label-->
											</div>
						            		<div id="currentTemplateRaw"></div>
						            		<div id="currentTemplateRendered"></div>
																	            		
						            		
						            	</div>
						            
						            
						            </div><!--templatePane -->
					    	</div><!-- panel-container  -->
					</div>    <!-- tab-container -->   
		            
		            <!--
		            <label class="editParameterLabel" for="parameterName">Name: <span class="mandatoryAsterix" title="Mandatory field">*</span></label>
					<input type="text" name="parameterName" id="parameterName" size="" maxlength="512" value="" class="textfield" style="margin:0; padding:0; width:22em;"   >
		
		            <span class="error" id="error_parameterName" style="margin-left: 5.5em;"></span>
		
		            <div class="clr" style="height:3px;"></div>
		            <label class="editParameterLabel" for="parameterValue">Value:</label>
					<input type="text" name="parameterValue" id="parameterValue" size="" maxlength="512" value="" class="textfield" style="margin:0; padding:0; width:22em;"   >
					-->
			</div> <!-- webHookFormContents -->

            <div class="popupSaveButtonsBlock">
              <a href="javascript://" showdiscardchangesmessage="false" onclick="BS.EditWebHookDialog.cancelDialog()"
                 class="cancel">Cancel</a>
              <input class="submitButton" type="submit" value="Save"/>
			<img id="webHookSaving" style="display: none; padding-top: 0.1em; float: right;" src="../img/ajax-loader.gif" width="16" height="16" alt="Please wait..." title="Please wait..."/>

              <br clear="all"/>
            </div>

            <input type="hidden" id="webHookId" name="webHookId" value=""/>
            <input type="hidden" id="payloadFormat" name="payloadFormat" value=""/>
            <input type="hidden" id="payloadTemplate" name="payloadTemplate" value=""/>
            <input type="hidden" id="submitAction" name="submitAction" value=""/>


          </form>
	    </div>
    </div>
          
