package webhook.teamcity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Test;

import webhook.WebHook;
import webhook.WebHookTest;
import webhook.WebHookTestServer;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookPayloadTest {

	@Test
	public void TestNVPairsPayloadContent(){
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class); 	
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadNameValuePairs whp = new WebHookPayloadNameValuePairs(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<String, String>();
		extraParameters.put("something", "somewhere");
		//String content = wpm.getFormat("nvpairs").buildStarted(sRunningBuild, extraParameters);
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("nvpairs").getContentType().equals("application/x-www-form-urlencoded"));
		assertTrue(wpm.getFormat("nvpairs").getFormatDescription().equals("Name Value Pairs"));
		System.out.println(wpm.getFormat("nvpairs").buildStarted(sRunningBuild, previousBuild, extraParameters));
		
	}
	
	@Test
	public void TestNVPairsPayloadWithPostToJetty(){
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookTest test = new WebHookTest();
		String url = "http://" + test.webserverHost + ":" + test.webserverPort + "/200";
		WebHookTestServer s = test.startWebServer();
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadNameValuePairs whp = new WebHookPayloadNameValuePairs(wpm);
		whp.register();
		WebHookProjectSettings whps = new WebHookProjectSettings();
		
		whps.addNewWebHook("project1", url, true, BuildState.ALL_ENABLED, "nvpairs");
		
    	for (WebHookConfig whc : whps.getWebHooksConfigs()){
			WebHook wh = new WebHook();
			wh.setUrl(whc.getUrl());
			wh.setEnabled(whc.getEnabled());
			//webHook.addParams(webHookConfig.getParams());
			wh.setTriggerStateBitMask(whc.getStatemask());
			//wh.setProxy(whps. getProxyConfigForUrl(whc.getUrl()));
			//this.getFromConfig(wh, whc);
			
			if (wpm.isRegisteredFormat(whc.getPayloadFormat())){
				//wh.addParam("notifyType", state);
				//addMessageParam(sRunningBuild, wh, stateShort);
				//wh.addParam("buildStatus", sRunningBuild.getStatusDescriptor().getText());
				//addCommonParams(sRunningBuild, wh);
				WebHookPayload payloadFormat = wpm.getFormat(whc.getPayloadFormat());
				wh.setContentType(payloadFormat.getContentType());
				wh.setCharset(payloadFormat.getCharset());
				wh.setPayload(payloadFormat.buildStarted(sRunningBuild, previousBuild, whc.getParams()));
				if (BuildState.enabled(wh.getEventListBitMask(), BuildState.BUILD_STARTED)){
					try {
						wh.post();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						test.stopWebServer(s);
					}
				}
		
			}
    	}
		
	}

}
