package webhook.teamcity.settings;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom.JDOMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.testframework.util.ConfigLoaderUtil;

@RunWith(MockitoJUnitRunner.class)
public class WebHookProjectFeaturesStoreTest {
    
    @Mock
    SProject sProject;
    
    ProjectFeatureToWebHookConfigConverter converter;
    
    @Mock
    BuildTypeIdResolver buildTypeIdResolver;
    

    @Test
    public void testGetWebHookConfigs() throws JDOMException, IOException {
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        List<SProjectFeatureDescriptor> webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testMigrationConfigurations/projects/FirstProject/project-config.xml"));
        when(sProject.getOwnFeaturesOfType("tcWebHooks")).thenReturn(webhooksAsProjectFeatures.stream().filter(f -> f.getType().equals("tcWebHooks")).collect(Collectors.toList()));
        
        // Firstly check that the order is incorrect from the file.
        assertTrue(webhooksAsProjectFeatures.get(0).getParameters().get("webHookId").endsWith("_01"));
        assertTrue(webhooksAsProjectFeatures.get(1).getParameters().get("webHookId").endsWith("_10"));
        assertTrue(webhooksAsProjectFeatures.get(2).getParameters().get("webHookId").endsWith("_11"));
        assertTrue(webhooksAsProjectFeatures.get(10).getParameters().get("webHookId").endsWith("_06"));
        
        assertTrue(webhooksAsProjectFeatures.get(0).getParameters().get("url").endsWith("=1"));
        assertTrue(webhooksAsProjectFeatures.get(1).getParameters().get("url").endsWith("=10"));
        assertTrue(webhooksAsProjectFeatures.get(2).getParameters().get("url").endsWith("=11"));
        assertTrue(webhooksAsProjectFeatures.get(10).getParameters().get("url").endsWith("=6"));
        
        // Now load them from the store. They should be sorted correctly.
        WebHookProjectFeaturesStore store = new WebHookProjectFeaturesStore(converter);
        List<WebHookConfig> configs = store.getWebHookConfigs(sProject).getWebHooksConfigs();
        
        // Now check that the order is correct from the store.
        assertTrue(configs.get(0).getUniqueKey().endsWith("_01"));
        assertTrue(configs.get(1).getUniqueKey().endsWith("_02"));
        assertTrue(configs.get(2).getUniqueKey().endsWith("_03"));
        assertTrue(configs.get(10).getUniqueKey().endsWith("_11"));
        assertTrue(configs.get(0).getUrl().endsWith("=1"));
        assertTrue(configs.get(1).getUrl().endsWith("=2"));
        assertTrue(configs.get(2).getUrl().endsWith("=3"));
        assertTrue(configs.get(10).getUrl().endsWith("=11"));
    }

}
