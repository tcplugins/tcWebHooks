package webhook.teamcity.settings;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookConfigTest {
	
	private static final String EMPTY_STRING = "";
	private static final String CHECKED = "checked ";
	WebHookConfig webhookAllEnabled;
	WebHookConfig webhookAllDisabled;
	WebHookConfig webhookDisabled;
	WebHookConfig webhookMostEnabled;
	WebHookConfig webhookWithAuth;
	WebHookConfig webhookWithFilters;
	
	
	@Before
	public void setup() throws JDOMException, IOException{
		
		webhookAllEnabled  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled.xml"));
		webhookAllDisabled = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-disabled.xml")); 
		webhookDisabled    = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-webhook-disabled.xml"));
		webhookMostEnabled = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-but-respchange-states-enabled.xml"));
		webhookWithAuth    = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
		webhookWithFilters    = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
	}
	
//	private WebHookConfig getFirstWebHookInConfig(File f) throws JDOMException, IOException{
//		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
//		assertTrue("One and only one webhook expected when loading test config from file : " + f.getName(), fileAsElement.getChild("webhooks").getChildren("webhook").size() == 1);
//		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(0));
//	}

	@Test
	public void printDefaultHtmlMessage(){
		System.out.println(WebHookPayloadDefaultTemplates.DEFAULT_HTML_BUILDSTATUS_TEMPLATE);
	}
	
	@Test
	public void testGetAsElement() {
		Element e = webhookAllEnabled.getAsElement();
		WebHookConfig whc = new WebHookConfig(e);
		assertTrue(whc.getParams().containsKey("color"));
		assertTrue(whc.getParams().containsKey("notify"));
	}

	@Test
	public void testGetParams() {
		assertTrue(webhookAllEnabled.getParams().containsKey("color"));
		assertTrue(webhookAllEnabled.getParams().containsKey("notify"));
	}

	@Test
	public void testGetEnabled() {
		assertTrue(webhookAllEnabled.getEnabled());
		assertTrue(webhookAllDisabled.getEnabled());
		assertFalse(webhookDisabled.getEnabled());
	}

	@Test
	public void testSetEnabled() {
		assertTrue(webhookAllEnabled.getEnabled());
		webhookAllEnabled.setEnabled(false);
		assertFalse(webhookAllEnabled.getEnabled());
	}

	@Test
	public void testGetBuildStates() {
		assertTrue(webhookAllEnabled.getBuildStates().allEnabled());
		assertFalse(webhookAllDisabled.getBuildStates().allEnabled());
		assertFalse(webhookDisabled.getBuildStates().allEnabled());
	}

	@Test
	public void testGetUrl() {
		assertTrue(webhookAllEnabled.getUrl().equals("http://localhost/test"));
	}

	@Test
	public void testSetUrl() {
		assertTrue(webhookAllEnabled.getUrl().equals("http://localhost/test"));
		webhookAllEnabled.setUrl("a new url");
		assertFalse(webhookAllEnabled.getUrl().equals("http://localhost/test"));
		assertTrue(webhookAllEnabled.getUrl().equals("a new url"));
		
	}

	@Test
	public void testGetUniqueKey() {
		assertFalse(webhookAllEnabled.getUniqueKey().equals(EMPTY_STRING));
	}

	@Test
	public void testSetUniqueKey() {
		String s = webhookAllEnabled.getUniqueKey();
		webhookAllEnabled.setUniqueKey("SomethingElse");
		assertFalse(webhookAllEnabled.getUniqueKey().equals(s));
		assertTrue(webhookAllEnabled.getUniqueKey().equals("SomethingElse"));
	}

	@Test
	public void testGetEnabledListAsString() {
		assertTrue(webhookAllEnabled.getEnabledListAsString().equals("All Build Events"));
		assertTrue(webhookAllDisabled.getEnabledListAsString().equals("None"));
		assertEquals(webhookMostEnabled.getEnabledListAsString()," Build Added to Queue, Build Removed from Queue by User, Build Started, Changes Loaded, Build Interrupted, Build Almost Completed, Build Failed, Build Successful, Build Pinned, Build Unpinned");
	}

	@Test
	public void testGetWebHookEnabledAsChecked() {
		assertTrue(webhookAllEnabled.getWebHookEnabledAsChecked().equals(CHECKED));
		assertTrue(webhookAllDisabled.getWebHookEnabledAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateAllAsChecked() {
		assertTrue(webhookAllEnabled.getStateAllAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateAllAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildStartedAsChecked() {
		assertTrue(webhookAllEnabled.getStateBuildStartedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildStartedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildFinishedAsChecked() {
		assertTrue(webhookAllEnabled.getStateBeforeFinishedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBeforeFinishedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBeforeFinishedAsChecked() {
		assertTrue(webhookAllEnabled.getStateBeforeFinishedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBeforeFinishedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateResponsibilityChangedAsChecked() {
		assertTrue(webhookAllEnabled.getStateResponsibilityChangedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateResponsibilityChangedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildInterruptedAsChecked() {
		assertTrue(webhookAllEnabled.getStateBuildInterruptedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildInterruptedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildSuccessfulAsChecked() {
		assertTrue(webhookAllEnabled.getStateBuildInterruptedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildInterruptedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildFixedAsChecked() {
		assertFalse(webhookAllEnabled.getStateBuildFixedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildFixedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildFailedAsChecked() {
		assertTrue(webhookAllEnabled.getStateBuildFailedAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildFailedAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetStateBuildBrokenAsChecked() {
		assertFalse(webhookAllEnabled.getStateBuildBrokenAsChecked().equals(CHECKED));
		assertFalse(webhookAllDisabled.getStateBuildBrokenAsChecked().equals(CHECKED));
	}

	@Test
	public void testGetPayloadFormat() {
		assertTrue(webhookAllEnabled.getPayloadFormat().equals("nvpairs"));
	}

	@Test
	public void testSetPayloadFormatString() {
		assertTrue(webhookAllEnabled.getPayloadFormat().equals("nvpairs"));
		webhookAllEnabled.setPayloadFormat("XML");
		assertTrue(webhookAllEnabled.getPayloadFormat().equals("XML"));
		assertFalse(webhookAllEnabled.getAuthEnabled());
	}
	
	@Test
	public void testAuthParametersAreLoaded(){
		assertTrue(webhookWithAuth.getAuthEnabled());
		assertNotNull(webhookWithAuth.getAuthenticationConfig());
		assertTrue(webhookWithAuth.getAuthenticationConfig().getParameters().containsKey("username"));
		assertTrue(webhookWithAuth.getAuthenticationConfig().getParameters().containsKey("password"));
		assertTrue(webhookWithAuth.getAuthenticationConfig().getParameters().containsKey("realm"));
	}

	@Test
	public void testWebHookElementSerialisation(){
		assertTrue(webhookWithAuth.getAuthEnabled());
		WebHookConfig newTestAuthConfig = new WebHookConfig(webhookWithAuth.getAsElement());
		assertEquals(webhookWithAuth.getAuthEnabled(), newTestAuthConfig.getAuthEnabled());
	}
	
	@Test
	public void testWebHookElementFiltersSerialisation(){
		assertTrue(webhookWithFilters.getTriggerFilters().size() == 1);
		WebHookConfig newTestFilterConfig = new WebHookConfig(webhookWithFilters.getAsElement());
		assertEquals(webhookWithFilters.getTriggerFilters().get(0).regex, newTestFilterConfig.getTriggerFilters().get(0).regex);
	}
}
