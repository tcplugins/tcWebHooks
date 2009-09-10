package webhook.teamcity.payload.format;

import static org.junit.Assert.*;

import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;

import org.junit.Test;

import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.format.WebHookPayloadJson;


public class WebHookPayloadJsonTest {
	
	@Test
	public void test_Json(){
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadManager wpm = new WebHookPayloadManager();
		WebHookPayloadJson whp = new WebHookPayloadJson(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<String, String>();
		extraParameters.put("something", "somewhere");
		//String content = wpm.getFormat("nvpairs").buildStarted(sRunningBuild, extraParameters);
		System.out.println(sRunningBuild.getBuildDescription());
		assertTrue(wpm.getFormat("json").getContentType().equals("application/json"));
		assertTrue(wpm.getFormat("json").getFormatDescription().equals("JSON"));
		System.out.println(wpm.getFormat("json").buildStarted(sRunningBuild, extraParameters));
	}
}
