package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticator;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.testframework.util.ConfigLoaderUtil;

public class ProjectFeatureToWebHookConfigConverterTest {

    @Test
    public void testConvert() throws JDOMException, IOException {
        //String featureId = "PROJECT_EXT_30";
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        WebHookConfig webhook = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/plugin-settings-with-lots-of-examples.xml"));
        webhook.setProjectInternalId("project02");
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider);
        SProjectFeatureDescriptor features = converter.convert(webhook);
        WebHookConfig convertedWebHook = converter.convert(features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //System.out.print(gson.toJson(webhook));
        System.out.print(gson.toJson(convertedWebHook));
        assertEquals(gson.toJson(webhook), gson.toJson(convertedWebHook));
        //assertTrue(EqualsBuilder.reflectionEquals(webhook.getAsElement() ,convertedWebHook.getAsElement()));
    }
    
    @Test
    public void testConvertFromRealProjectConfig() throws JDOMException, IOException {
        /*
               <extension id="PROJECT_EXT_1" type="tcWebHooks">
                  <parameters>
                    <param name="authentication" value="basicAuth" />
                    <param name="basicAuthPassword" value="myPassword" />
                    <param name="basicAuthPreemptive" value="true" />
                    <param name="basicAuthRealm" value="myRealm" />
                    <param name="basicAuthUsername" value="myUserName" />
                    <param name="buildAddedToQueue" value="enabled" />
                    <param name="buildRemovedFromQueue" value="enabled" />
                    <param name="buildStates" value="enabledBuildStates" />
                    <param name="buildTypeIds" value="RootProjectId_TcDummyDeb, RootProjectId_TcWebHooks" />
                    <param name="buildTypes" value="selectedProjectBuilds" />
                    <param name="subProjectBuilds" value="true" />
                    <param name="template" value="legacy-json" />
                    <param name="url" value="http://localhost:8111/webhooks/endpoint.html?vcs_test=1" />
                    <param name="webHookId" value="SmallKotlinProject_WebHook_01" />
                  </parameters>
                </extension>
         */
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        List<SProjectFeatureDescriptor> webhook = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testProjectConfig/projects/Root/project-config.xml"));
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider);
        WebHookConfig webHookConfig = converter.convert(webhook.get(0));
        SProjectFeatureDescriptor features = converter.convert(webHookConfig);
        WebHookConfig convertedWebHook = converter.convert(features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //System.out.print(gson.toJson(webhook));
        System.out.print(gson.toJson(convertedWebHook));
        assertEquals(gson.toJson(webHookConfig), gson.toJson(convertedWebHook));
        //assertTrue(EqualsBuilder.reflectionEquals(webhook.getAsElement() ,convertedWebHook.getAsElement()));
        UsernamePasswordAuthenticatorFactory usernamePasswordAuthenticatorFactory = new UsernamePasswordAuthenticatorFactory(authenticatorProvider);
        assertTrue(webHookConfig.getAuthEnabled());
        assertEquals(usernamePasswordAuthenticatorFactory.getName(), webHookConfig.getAuthenticationConfig().getType());
        assertEquals("myUserName", webHookConfig.getAuthenticationConfig().getParameters().get(UsernamePasswordAuthenticator.KEY_USERNAME));
        assertEquals("myPassword", webHookConfig.getAuthenticationConfig().getParameters().get(UsernamePasswordAuthenticator.KEY_PASS));
        assertTrue(webHookConfig.isEnabledForSubProjects());
        assertFalse(webHookConfig.isEnabledForAllBuildsInProject());
        
    }

}
