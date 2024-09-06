package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Ignore;
import org.junit.Test;

import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEvent;
import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEventImpl;
import webhook.testframework.util.ConfigLoaderUtil;
import webhook.teamcity.WebHookSettingsEventType;

public class WebHookSettingsManagerVcsSettingsTest extends WebHookSettingsManagerTestBase {
	
	private static final String PROJECT01 = "project01";

	@Test @Ignore
	public void TestThatAddingWebhookToExistingWebHooksViaProjectChangedResultsInWebHookAddedToCache() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
		config.setUniqueKey("my_unique_id");
		projectSettings.addNewWebHook(config);
		when(webhookFeaturesStore.addWebHookConfig(sProject, config)).thenReturn(new WebHookUpdateResult(true, config));

		//webHookSettingsManager.addNewWebHook(PROJECT01, config);
		
		WebHookSettingsEvent changeEvent = new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_PERSISTED, PROJECT01, null, null);
		webHookSettingsManager.handleProjectChangedEvent(changeEvent );
		
		assertEquals(3, webHookSettingsManager.getWebHooksForProject(sProject).size());
	}
	
	@Test
	public void TestThatAddingWebHookToProjectWithoutWebhooksViaProjectChangedResultsInWebHookAddedToCache() throws JDOMException, IOException {
	WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
	    config.setUniqueKey("my_unique_id");
	
	    // Put the webhook directly into the project settings object (not via WebHookSettingsManager). 
	    project02Settings.addNewWebHook(config);
	    
	    // We can assume that "readFrom" will have been called before our event, so the plugin-settings.xml will have been re-read.
	    // We have simulated that by putting the webhook directly into the projectSettings above.
	    // Note: WebHookSettingsManager does not know about our webhook yet
        WebHookSettingsEvent changeEvent = new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_PERSISTED, "project02", null, null);
        webHookSettingsManager.handleProjectChangedEvent(changeEvent );
        
        assertEquals(1, webHookSettingsManager.getWebHooksForProject(sProject02).size());
	}
	
	@Test
	public void TestThatAddingSecondWebHookToProjectWithoutWebhooksViaProjectChangedResultsInWebHookAddedToCache() throws JDOMException, IOException {
	    WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
	    config.setUniqueKey("my_unique_id");
	    
	    // Put the webhook directly into the project settings object (not via WebHookSettingsManager). 
	    project02Settings.addNewWebHook(config);
	    
	    // We can assume that "readFrom" will have been called before our event, so the plugin-settings.xml will have been re-read.
	    // We have simulated that by putting the webhook directly into the projectSettings above.
	    // Note: WebHookSettingsManager does not know about our webhook yet
	    WebHookSettingsEvent changeEvent = new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, "project02", null, null);
	    webHookSettingsManager.handleProjectChangedEvent(changeEvent );
	    
	    assertEquals(1, webHookSettingsManager.getWebHooksForProject(sProject02).size());
	    
	    config = ConfigLoaderUtil.getSpecificWebHookInConfig(2, new File("src/test/resources/project-settings-test-with-build-types.xml"));
        config.setUniqueKey("my_unique_id_2");
        
        // Put the webhook directly into the project settings object (not via WebHookSettingsManager). 
        project02Settings.addNewWebHook(config);
        
        webHookSettingsManager.handleProjectChangedEvent(changeEvent);
        
        assertEquals(2, webHookSettingsManager.getWebHooksForProject(sProject02).size());

	}
	
	@Test
	public void TestThatOldWebHooksAreRemovedFromTheCacheWhenTheyDontHaveIdDefined() throws JDOMException, IOException {
        
        // Put two webhooks directly into the project settings object (not via WebHookSettingsManager).
        WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
        config.setUniqueKey(null);
        project02Settings.addNewWebHook(config);
        
        WebHookConfig config2 = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
        config.setUniqueKey(null);
        project02Settings.addNewWebHook(config2);
        
        // We can assume that "readFrom" will have been called before our event, so the plugin-settings.xml will have been re-read.
        // We have simulated that by putting the webhook directly into the projectSettings above.
        // Note: WebHookSettingsManager does not know about our webhook yet
        WebHookSettingsEvent changeEvent = new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, "project02", null, null);
        webHookSettingsManager.handleProjectChangedEvent(changeEvent );
        List<WebHookConfigEnhanced> webHooksEnhnaced = webHookSettingsManager.getWebHooksForProject(sProject02);
        assertEquals(2, webHooksEnhnaced.size());
        
        project02Settings.deleteWebHook(webHooksEnhnaced.get(0).getWebHookConfig().getUniqueKey(), "project02");
        project02Settings.deleteWebHook(webHooksEnhnaced.get(1).getWebHookConfig().getUniqueKey(), "project02");

        WebHookConfig config3 = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
        config.setUniqueKey(null);
        project02Settings.addNewWebHook(config3);
        
        WebHookConfig config4 = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-with-build-types.xml"));
        config.setUniqueKey(null);
        project02Settings.addNewWebHook(config4);
        // Trigger update again
        webHookSettingsManager.handleProjectChangedEvent(changeEvent );
        
        assertEquals(2, webHookSettingsManager.getWebHooksForProject(sProject02).size());
	}

}
