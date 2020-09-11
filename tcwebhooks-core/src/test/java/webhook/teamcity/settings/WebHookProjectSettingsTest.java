package webhook.teamcity.settings;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHookProjectSettingsTest {
	
	protected SortedMap<String, String> map = new TreeMap<>();
	protected ExtraParameters  extraParameters  = new ExtraParameters(map); 
	protected WebHookMockingFramework framework;
	
	@Test
	public void TestUpdateToWebhookConfigToRemoveAuthenicationUpdatesCorrectlyWhenNullWebHookAuthConfigPassedIn() throws IOException, JDOMException{
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
		
		WebHookProjectSettings settings = framework.getWebHookProjectSettings(); 
		WebHookConfig config = settings.getWebHooksConfigs().get(0);
		assertTrue("Auth should be enabled", config.getAuthEnabled());
		
		settings.updateWebHook("project01", config.getUniqueKey(), config.getUrl(), config.getEnabled(), new BuildState(), config.getPayloadTemplate(), config.isEnabledForAllBuildsInProject(), config.isEnabledForSubProjects(), null, null);
		
		WebHookConfig config2 = settings.getWebHooksConfigs().get(0);
		assertFalse("Auth should now be disabled", config2.getAuthEnabled());
		
	}
	@Test
	public void TestUpdateToWebhookConfigToAddAuthenicationUpdatesCorrectlyWhenValidWebHookAuthConfigPassedIn() throws IOException, JDOMException{
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		
		WebHookProjectSettings settings = framework.getWebHookProjectSettings(); 
		WebHookConfig config = settings.getWebHooksConfigs().get(0);
		assertFalse("Auth should be disabled", config.getAuthEnabled());
		
		WebHookAuthConfig authConfig = new WebHookAuthConfig();
		authConfig.setType("userpass");
		authConfig.setPreemptive(true);
		authConfig.getParameters().put("username", "usernamey");
		
		settings.updateWebHook("project01", config.getUniqueKey(), config.getUrl(), config.getEnabled(), new BuildState(), config.getPayloadTemplate(), config.isEnabledForAllBuildsInProject(), config.isEnabledForSubProjects(), null, authConfig);
		
		WebHookConfig config2 = settings.getWebHooksConfigs().get(0);
		assertTrue("Auth should now be enabled", config2.getAuthEnabled());
		
	}
	
}
