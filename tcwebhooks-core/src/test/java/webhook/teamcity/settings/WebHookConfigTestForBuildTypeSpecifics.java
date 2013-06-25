package webhook.teamcity.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import jetbrains.buildServer.serverSide.SBuildType;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.MockSBuildType;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookConfigTestForBuildTypeSpecifics {

	WebHookConfig webhookAllBuilds;
	WebHookConfig webhookSpecificBuilds;
	SBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	SBuildType sBuildType02 = new MockSBuildType("Test Build", "A Test Build", "bt2");
	SBuildType sBuildType03 = new MockSBuildType("Test Build", "A Test Build", "bt3");
	
	
	@Before
	public void setup() throws JDOMException, IOException{
		webhookSpecificBuilds  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		webhookAllBuilds  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled.xml"));
	}

	@Test
	public void testGetBuildTypeEnabled() {
		assertTrue(webhookSpecificBuilds.isEnabledForBuildType(sBuildType));
		assertTrue(webhookSpecificBuilds.isEnabledForBuildType(sBuildType02));
		assertFalse(webhookSpecificBuilds.isEnabledForBuildType(sBuildType03));
		
		assertTrue(webhookAllBuilds.isEnabledForBuildType(sBuildType));
		assertTrue(webhookAllBuilds.isEnabledForBuildType(sBuildType02));
		assertTrue(webhookAllBuilds.isEnabledForBuildType(sBuildType03));
	}
	
	@Test
	public void testGetAsElementSpecific() {
		Element e = webhookSpecificBuilds.getAsElement();
		WebHookConfig whc = new WebHookConfig(e);
		assertTrue(whc.isEnabledForBuildType(sBuildType));
		assertTrue(whc.isEnabledForBuildType(sBuildType02));
		assertFalse(whc.isEnabledForBuildType(sBuildType03));
	}

	@Test
	public void testGetAsElementAll() {

		Element e = webhookAllBuilds.getAsElement();
		WebHookConfig whc = new WebHookConfig(e);
		assertTrue(whc.isEnabledForBuildType(sBuildType));
		assertTrue(whc.isEnabledForBuildType(sBuildType02));
		assertTrue(whc.isEnabledForBuildType(sBuildType03));
	}
}
