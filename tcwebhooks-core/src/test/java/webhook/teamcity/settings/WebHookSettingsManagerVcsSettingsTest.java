package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEvent;
import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEventImpl;
import webhook.testframework.util.ConfigLoaderUtil;
import webhook.teamcity.WebHookSettingsEventType;

public class WebHookSettingsManagerVcsSettingsTest extends WebHookSettingsManagerTestBase {
	
	private static final String PROJECT01 = "project01";

	@Test
	public void Testing() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
		config.setUniqueKey("my_unique_id");

		webHookSettingsManager.addNewWebHook(PROJECT01, config);
		
		WebHookSettingsEvent changeEvent = new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_PERSISTED, PROJECT01, null, null);
		webHookSettingsManager.handleProjectChangedEvent(changeEvent );
		
		assertEquals(3, webHookSettingsManager.getWebHooksConfigs(PROJECT01).size());
	}

}
