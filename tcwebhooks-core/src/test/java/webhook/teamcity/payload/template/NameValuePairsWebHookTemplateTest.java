package webhook.teamcity.payload.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;

public class NameValuePairsWebHookTemplateTest {

	private WebHookTemplateManager wtm;
	private SBuildServer mockServer = mock(SBuildServer.class);
	private AbstractPropertiesBasedWebHookTemplate wht;
	
	@Before
	public void setup(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(mockServer, null, null);
		wht = new NameValuePairsWebHookTemplate(wtm);
		wht.register();
	}
	
	@Test
	public void testGetTemplateRegister() {
		assertTrue(wtm.getRegisteredTemplates().contains(wht));
	}	

	@Test
	public void testGetTemplateForState() {
		Loggers.SERVER.info(wht.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText());
		assertTrue("BuildBroken template must be greater than zero length", wht.getTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText().length() > 0);
		assertFalse("Branch and Non-Branch template content should be different", wht.getTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText().equals(wht.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText()));
	}

	@Test
	public void testGetBranchTemplateForState() {
		Loggers.SERVER.info(wht.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText());
		assertTrue("BuildBroken branch template must be greater than zero length", wht.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText().length() > 0);
		assertFalse("Branch and Non-Branch template content should be different", wht.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText().equals(wht.getTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText()));
	}

	@Test
	public void testGetSupportedBuildStates() {
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_STARTED));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_INTERRUPTED));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BEFORE_BUILD_FINISHED));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_FAILED));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_BROKEN));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_SUCCESSFUL));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_FIXED));
		assertTrue(wht.getSupportedBuildStates().contains(BuildStateEnum.RESPONSIBILITY_CHANGED));
	}

	@Test
	public void testGetSupportedBranchBuildStates() {
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_STARTED));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_INTERRUPTED));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BEFORE_BUILD_FINISHED));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_FAILED));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_BROKEN));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_SUCCESSFUL));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_FIXED));
		assertTrue(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.RESPONSIBILITY_CHANGED));
	}
	
	@Test
	public void testNonValidNotifyState(){
		assertFalse(wht.getSupportedBuildStates().contains(BuildStateEnum.BUILD_FINISHED));
		assertFalse(wht.getSupportedBranchBuildStates().contains(BuildStateEnum.BUILD_FINISHED));
		assertNull(wht.getTemplateForState(BuildStateEnum.BUILD_FINISHED));
		assertNull(wht.getBranchTemplateForState(BuildStateEnum.BUILD_FINISHED));
	}

}
