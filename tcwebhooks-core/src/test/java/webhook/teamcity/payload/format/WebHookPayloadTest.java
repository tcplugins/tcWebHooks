package webhook.teamcity.payload.format;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.UnsupportedWebHookFormatException;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsLegacyVariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;

public class WebHookPayloadTest {
	
	WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
	
	@Before
	public void setup() {
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsLegacyVariableResolverFactory());
	}
	
	@Test
	public void test_Xml() throws WebHookPayloadContentAssemblyException{
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadXml whp = new WebHookPayloadXml(wpm, variableResolverManager);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("xml").getContentType().equals("text/xml"));
		assertTrue(wpm.getFormat("xml").getFormatDescription().equals("XML"));
		System.out.println(wpm.getFormat("xml").buildFinished(sRunningBuild, previousBuild, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null));
	}
	
	
	@Test
	public void test_Json() throws WebHookPayloadContentAssemblyException{
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadJson whp = new WebHookPayloadJson(wpm, variableResolverManager);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("json").getContentType().equals("application/json"));
		assertTrue(wpm.getFormat("json").getFormatDescription().equals("JSON"));
		System.out.println(wpm.getFormat("json").buildStarted(sRunningBuild, previousBuild, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null));
	}
	
	@Test
	public void test_NvPairs() throws WebHookPayloadContentAssemblyException{
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadNameValuePairs whp = new WebHookPayloadNameValuePairs(wpm, variableResolverManager);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		
		extraParameters.put(ExtraParameters.WEBHOOK, "item1", "content1");
		extraParameters.put(ExtraParameters.WEBHOOK, "item2", "content2");
		extraParameters.put(ExtraParameters.WEBHOOK, "item3", "content3");
		extraParameters.put(ExtraParameters.WEBHOOK, "item4", "content4");
		extraParameters.put(ExtraParameters.WEBHOOK, "item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("nvpairs").getContentType().equals("application/x-www-form-urlencoded"));
		assertTrue(wpm.getFormat("nvpairs").getFormatDescription().equals("Name Value Pairs"));
		System.out.println(wpm.getFormat("nvpairs").buildStarted(sRunningBuild, previousBuild, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void test_Empty() throws WebHookPayloadContentAssemblyException{
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running","TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadEmpty whp = new WebHookPayloadEmpty(wpm);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		wpm.getFormat("empty").setRank(5000);
		
		assertTrue(wpm.getFormat("empty").getRank().equals(5000));
		assertTrue(wpm.getFormat("empty").getContentType().equals("text/plain"));
		assertTrue(wpm.getFormat("empty").getFormatDescription().equals("None"));
		assertTrue(wpm.getFormat("empty").getCharset().equals("UTF-8"));
		assertTrue(wpm.getFormat("empty").buildStarted(sRunningBuild, previousBuild, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null).equals(""));
		assertTrue(wpm.getFormat("empty").beforeBuildFinish(sRunningBuild, previousBuild, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null).equals(""));
		assertTrue(wpm.getFormat("empty").buildChangedStatus(sRunningBuild, previousBuild, Status.NORMAL, Status.ERROR, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null).equals(""));
		assertTrue(wpm.getFormat("empty").buildFinished(sRunningBuild, previousBuild, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null).equals(""));
		assertTrue(wpm.getFormat("empty").buildInterrupted(sRunningBuild, previousBuild, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), null).equals(""));
		
	}
	
	@Test(expected=UnsupportedWebHookFormatException.class)
	public void test_NotFound(){
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		assertTrue(wpm.getRegisteredFormats().isEmpty());
		assertTrue(wpm.getRegisteredFormatsAsCollection().isEmpty());
		assertNull(wpm.getFormat("SomethingThatDoesNotExist"));
	}
	
	
}
