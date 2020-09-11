package webhook.teamcity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.TestingWebHookFactory;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsLegacyVariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;


public class WebHookPayloadTest {
	
	TestingWebHookFactory factory = new TestingWebHookFactory();
	WebHookVariableResolverManager resolverManager = new WebHookVariableResolverManagerImpl();
	VariableResolverFactory variableResolverFactory = new WebHooksBeanUtilsVariableResolverFactory();
	VariableResolverFactory legacyVariableResolverFactory = new WebHooksBeanUtilsLegacyVariableResolverFactory();
	@Test
	public void TestNVPairsPayloadContent() throws WebHookPayloadContentAssemblyException{
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class); 	
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		
		resolverManager.registerVariableResolverFactory(variableResolverFactory);
		resolverManager.registerVariableResolverFactory(legacyVariableResolverFactory);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadNameValuePairs whp = new WebHookPayloadNameValuePairs(wpm, resolverManager);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.put("something", "somewhere");
		//String content = wpm.getFormat("nvpairs").buildStarted(sRunningBuild, extraParameters);
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("nvpairs").getContentType().equals("application/x-www-form-urlencoded"));
		assertTrue(wpm.getFormat("nvpairs").getFormatDescription().equals("Name Value Pairs"));
		System.out.println(wpm.getFormat("nvpairs").buildStarted(sRunningBuild, previousBuild, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null));
		
	}
	
	// TODO: Get this working again once nvpairs is converted to template based payload 
	/*
	@Test
	public void TestNVPairsPayloadWithPostToJetty() throws InterruptedException, WebHookPayloadContentAssemblyException{
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		
		resolverManager.registerVariableResolverFactory(variableResolverFactory);
		resolverManager.registerVariableResolverFactory(legacyVariableResolverFactory);
		
		WebHookTest test = new WebHookTest();
		String url = "http://" + test.webserverHost + ":" + test.webserverPort + "/200";
		WebHookTestServer s = test.startWebServer();
		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadNameValuePairs whp = new WebHookPayloadNameValuePairs(wpm, resolverManager);
		whp.register();
		WebHookProjectSettings whps = new WebHookProjectSettings();
		
		BuildState state = new BuildState().setAllEnabled();
		whps.addNewWebHook("project1", "MyProject", url, true, state, "nvpairs", "originalNvpairsTemplate", true, true, new HashSet<String>());
		
    	for (WebHookConfig whc : whps.getWebHooksConfigs()){
			WebHook wh = factory.getWebHook(whc.getUrl());
			wh.setEnabled(whc.getEnabled());
			//webHook.addParams(webHookConfig.getParams());
			wh.setBuildStates(whc.getBuildStates());
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
				wh.setPayload(payloadFormat.buildStarted(sRunningBuild, previousBuild, whc.getParams(), whc.getEnabledTemplates(), null));
				if (wh.isEnabled()){
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
		
	}*/

}
