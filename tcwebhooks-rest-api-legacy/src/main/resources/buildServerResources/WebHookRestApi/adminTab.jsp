<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ include file="/include-internal.jsp" %>

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <h3>Health Report</h3>
	        
	        <bs:messages key="apiFixResult"/>
	        
	        <div>
	       	<c:choose>  
	    		<c:when test="${hasFoundIssues}"> 
					<div class="icon_before icon16 attentionRed">Health Status: WARNING. TeamCity REST API plugin files contain JAXB jars.</div>
	         	</c:when>  
	    		<c:when test="${restartRequired}"> 
					<div class="icon_before icon16 attentionRed">Health Status: WARNING. TeamCity </a> required after Plugin file cleaning.</div>
	         	</c:when>  
	         	<c:otherwise>  
					<div>Health Status: PASSED. No problematic TeamCity REST API plugin files have been found.</div>
	         	</c:otherwise>  
			</c:choose> 
	        
	        </div>
	        
	        <p>The WebHook REST API extends the TeamCity REST API plugin and uses the JAXB library to read and write to the webhooks templates configuration file. </p>
	        <p>Unfortunately, the  TeamCity REST API comes bundled with an old version of the JAXB libraries. This means that when the WebHooks API tries to write to the <i>webhooks-templates.xml</i> configuration file, it fails with a ClassCastException. See <a href="https://github.com/tcplugins/tcWebHooks/issues/43">issue 43 on github for more details</a>.<p/>
	        <p>This page gives you an indication on whether this problem is present on your TeamCity installation. It also has a tool which attempts to fix any problematic REST API installations.</p>
	        
	        <table class="settings">
	        <c:forEach items="${fileResults.values()}" var="foundJar">
	        		<tr><td colspan="4" style="text-align: left;padding:0.5em;"><strong>Plugin file found in <em>${foundJar.path.toString()}</em></strong></td></tr>
	        		<tr><td><strong>Plugin ZIP file</strong></td>
	        			<td colspan=2>${foundJar.path.toString()}</td>
	        			<c:set var="colspan" value="${foundJar.fileListSize + 2}"/>
	        			<td rowspan="${colspan}">
	        				<c:choose>
			        			<c:when test="${foundJar.jarReport.jarFileFound}">
									<a href="#" onclick="WebHookRestApiHealthStatus.fixPluginFile('${util:forJS(foundJar.path.toString(), false, false)}')">Fix the ZIP<br>file and cleanup<br>the unpacked jars</a>
								</c:when>
			        			<c:when test="${foundJar.jarReport.rebootRequired}">
									Please <a href="#" onclick="BS.ServerRestarter.restartServer(); return false;">restart</a> TeamCity 
								</c:when>
								<c:otherwise>
									No action required
								</c:otherwise>
							</c:choose>
	        			</td>
	        		</tr>
	        			
	        		<c:forEach items="${foundJar.jarReport.filesInZip}" var="file">
	        			<tr><td>Jar File</td>	
			        	<c:choose>
			        		<c:when test="${file.errored}">
	        					<td>${file.filename} was NOT removed from API zip file. The following error occurred:
	        					<p>${file.failureMessage}</p>
	        					</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;<a href="https://github.com/tcplugins/tcWebHooks/wiki/Fixing-the-REST-API-Jar-Conflict#manual-intervention" target="_blank">Manual intervention required</a></td>
							</c:when>
			        		<c:when test="${file.removed}">
	        					<td>${file.filename} successfully removed from API zip file. TeamCity <a href="#" onclick="BS.ServerRestarter.restartServer(); return false;">restart</a> required</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;<a href="#" onclick="BS.ServerRestarter.restartServer(); return false;">Restart Required</a></td>
							</c:when>
			        		<c:when test="${file.found}">
	        					<td>${file.filename} found inside API zip file</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;Found in ZIP</td>
							</c:when>
							<c:otherwise>
	        					<td>${file.filename} is not present in API zip file</td>
								<td colspan="1" class="icon_before icon16" style="border-color: #ccc; background-color:white;">&nbsp;Cool</td>
							</c:otherwise>
						</c:choose>
						</tr>
	        		</c:forEach>
	        		
	        		<tr>
	        		<td><strong>Unpacked Location</strong></td><td colspan=2>${foundJar.jarReport.apiZipFileUnpackedLocation}</td>
	        		</tr>
	        		
	        		<c:forEach items="${foundJar.jarReport.filesInUnpackedLocation}" var="file">
	        			<tr><td>Jar File</td>	
			        	<c:choose>
			        		<c:when test="${file.errored}">
	        					<td>${file.filename} was NOT removed from unpacked location. The following error occurred:
	        					<p>${file.failureMessage}</p>
	        					</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;<a href="https://github.com/tcplugins/tcWebHooks/wiki/Fixing-the-REST-API-Jar-Conflict#manual-intervention" target="_blank">Manual intervention required</a></td>
							</c:when>
			        		<c:when test="${file.removed}">
	        					<td>${file.filename} successfully removed from unpacked location.</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;<a href="#" onclick="BS.ServerRestarter.restartServer(); return false;">Restart Required</a></td>
							</c:when>
			        		<c:when test="${file.found}">
	        					<td>${file.filename} found in unpacked location</td>
								<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;Found in unpacked location</td>
							</c:when>
							<c:otherwise>
	        					<td>${file.filename} is not present in unpacked location</td>
								<td colspan="1" class="icon_before icon16" style="border-color: #ccc; background-color:white;">&nbsp;Cool</td>
							</c:otherwise>
						</c:choose>
						</tr>
	        		</c:forEach>
	        </c:forEach>
	        
		    </table>
	        
</bs:refreshable>	        
	        </div>

    <bs:dialog dialogId="fixPluginDialog"
               dialogClass="fixPluginDialog"
               title="Fix REST API Plugin"
               closeCommand="WebHookRestApiHealthStatus.FixPluginDialog.close()">
        <forms:multipartForm id="fixPluginForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHookRestApiHealthStatus.FixPluginDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>The API ZIP file will be modified and the conflicting jars will be removed from both the ZIP file and the unpacked directory.
                 		This will make changes to the files on your TeamCity server.
                        <div id="apiFixResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="filePath" name="filePath"/>
            <input type="hidden" name="action" id="FixPluginaction" value="fixPluginFile"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="fixPluginDialogSubmit" label="Fix Plugin File"/>
                <forms:cancel onclick="WebHookRestApiHealthStatus.FixPluginDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>	        
	        