package webhook.teamcity.payload.format;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Test;

import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.WebHookPayloadManager;

public class WebHookPayloadTest {
	
	@Test
	public void test_Xml(){
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadXml whp = new WebHookPayloadXml(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<String, String>();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("xml").getContentType().equals("text/xml"));
		assertTrue(wpm.getFormat("xml").getFormatDescription().equals("XML"));
		System.out.println(wpm.getFormat("xml").buildFinished(sRunningBuild, previousBuild, extraParameters));
	}
	
	
	@Test
	public void test_Json(){
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadJson whp = new WebHookPayloadJson(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<String, String>();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("json").getContentType().equals("application/json"));
		assertTrue(wpm.getFormat("json").getFormatDescription().equals("JSON (beta)"));
		System.out.println(wpm.getFormat("json").buildStarted(sRunningBuild, previousBuild, extraParameters));
	}
	
	@Test
	public void test_NvPairs(){
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
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("nvpairs").getContentType().equals("application/x-www-form-urlencoded"));
		assertTrue(wpm.getFormat("nvpairs").getFormatDescription().equals("Name Value Pairs"));
		System.out.println(wpm.getFormat("nvpairs").buildStarted(sRunningBuild, previousBuild, extraParameters));
	}

	@Test
	public void test_Empty(){
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadEmpty whp = new WebHookPayloadEmpty(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<String, String>();
		
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
		assertTrue(wpm.getFormat("empty").buildStarted(sRunningBuild, previousBuild, extraParameters).equals(""));
		assertTrue(wpm.getFormat("empty").beforeBuildFinish(sRunningBuild, previousBuild, extraParameters).equals(""));
		assertTrue(wpm.getFormat("empty").buildChangedStatus(sRunningBuild, previousBuild, Status.NORMAL, Status.ERROR, extraParameters).equals(""));
		assertTrue(wpm.getFormat("empty").buildFinished(sRunningBuild, previousBuild, extraParameters).equals(""));
		assertTrue(wpm.getFormat("empty").buildInterrupted(sRunningBuild, previousBuild, extraParameters).equals(""));
		
	}
	
	@Test
	public void test_Null(){
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		assertTrue(wpm.getRegisteredFormats().isEmpty());
		assertTrue(wpm.getRegisteredFormatsAsCollection().isEmpty());
		assertNull(wpm.getFormat("SomethingThatDoesNotExist"));
	}
	
	
}
