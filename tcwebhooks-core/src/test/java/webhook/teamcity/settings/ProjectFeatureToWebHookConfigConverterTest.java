package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.ProjectAndBuildTypeResolverImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticator;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.testframework.util.ConfigLoaderUtil;

@RunWith(MockitoJUnitRunner.class)
public class ProjectFeatureToWebHookConfigConverterTest {

    @Mock
    ProjectManager projectManager;
    
    @Before
    public void setup() {
        MockSBuildType sBuildType = new MockSBuildType("TcDummyDeb", "TcDummyDeb build", "bt1");
        MockSBuildType sBuildType2 = new MockSBuildType("TcWebHooks", "TcWebHooks build", "bt2");
        MockSProject myProject = new MockSProject("Root Project", "The Root Project", "project01", "RootProjectId", sBuildType);
        myProject.addANewBuildTypeToTheMock(sBuildType2);
        sBuildType.setProject(myProject);
        sBuildType2.setProject(myProject);
        when(projectManager.findBuildTypeByExternalId("RootProjectId_TcDummyDeb")).thenReturn(sBuildType);
        when(projectManager.findBuildTypeByExternalId("RootProjectId_TcWebHooks")).thenReturn(sBuildType2);
        when(projectManager.findBuildTypeById("bt1")).thenReturn(sBuildType);
        when(projectManager.findBuildTypeById("bt2")).thenReturn(sBuildType2);
    }
    
    @Test
    public void testConvert() throws JDOMException, IOException {
        //String featureId = "PROJECT_EXT_30";
        BuildTypeIdResolver buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        WebHookConfig webhook = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/plugin-settings-with-lots-of-examples.xml"));
        webhook.setProjectInternalId("project02");
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        SProjectFeatureDescriptor features = converter.convert(webhook);
        WebHookConfig convertedWebHook = converter.convert(features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //System.out.print(gson.toJson(webhook));
        System.out.print(gson.toJson(convertedWebHook));
        assertEquals(gson.toJson(webhook), gson.toJson(convertedWebHook));
        //assertTrue(EqualsBuilder.reflectionEquals(webhook.getAsElement() ,convertedWebHook.getAsElement()));
    }
    
    @Test
    public void testAndValidateConvert() throws JDOMException, IOException {
        //String featureId = "PROJECT_EXT_30";
        BuildTypeIdResolver buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        WebHookConfig webhook = ConfigLoaderUtil.getSpecificWebHookInConfig(2, new File("src/test/resources/plugin-settings-with-lots-of-examples.xml"));
        webhook.setProjectInternalId("project02");
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        SProjectFeatureDescriptor features = converter.convert(webhook);
        WebHookConfig convertedWebHook = converter.convert(features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //System.out.print(gson.toJson(webhook));
        System.out.print(gson.toJson(features.getParameters()));
        
        assertContainsKeyAndValue(features.getParameters(), "webHookId", "id_725393956");
        assertContainsKeyAndValue(features.getParameters(), "url", "http://localhost:8111/webhooks/endpoint.html?exmaple_config=all");
        assertContainsKeyAndValue(features.getParameters(), "buildAddedToQueue", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildRemovedFromQueue", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildStarted", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "changesLoaded", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildInterrupted", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "beforeBuildFinish", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildSuccessful", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildFailed", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildFixed", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildBroken", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "responsibilityChanged", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildPinned", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "buildUnpinned", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "testsMuted", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "testsUnmuted", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "serviceMessageReceived", "enabled");
        assertContainsKeyAndValue(features.getParameters(), "reportStatistics", "disabled");
        
        
        assertContainsKeyAndValue(features.getParameters(), "authentication", "basicAuth");
        assertContainsKeyAndValue(features.getParameters(), "basicAuthUsername", "my_username");
        assertContainsKeyAndValue(features.getParameters(), "basicAuthPassword", "my_password");
        assertContainsKeyAndValue(features.getParameters(), "basicAuthRealm", "my_realm");
        
        assertContainsKeyAndValue(features.getParameters(), "header_0_name", "x-testing-header");
        assertContainsKeyAndValue(features.getParameters(), "header_0_value", "oooh, a header!");
        
        assertContainsKeyAndValue(features.getParameters(), "triggerFilter_0_value", "${buildName}");
        assertContainsKeyAndValue(features.getParameters(), "triggerFilter_0_regex", ".+");
        assertContainsKeyAndValue(features.getParameters(), "triggerFilter_0_enabled", "true");
        
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_name", "my_variable");
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_value", "my_variable_value");
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_secure", "false");
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_includedInLegacyPayloads", "true");
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_forceResolveTeamCityVariable", "true");
        assertContainsKeyAndValue(features.getParameters(), "parameter_0_templateEngine", "VELOCITY");
        
        
        assertContainsKeyAndValue(features.getParameters(), "parameter_1_name", "my_parameter");
        assertContainsKeyAndValue(features.getParameters(), "parameter_1_value", "my_param_value");
        
        
//        System.out.print(gson.toJson(convertedWebHook));
//        assertEquals(gson.toJson(webhook), gson.toJson(convertedWebHook));
        
        //assertTrue(EqualsBuilder.reflectionEquals(webhook.getAsElement() ,convertedWebHook.getAsElement()));
    }
    
    private static void assertContainsKeyAndValue(Map<String,String> parameters, String key, String value) {
        assertTrue("Parameters Map does not contain key '" + key + "'", parameters.containsKey(key));
        assertTrue("Parameters Map with key '" + key + "' does not contain value '" + value + "'", parameters.get(key).equals(value));
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
        BuildTypeIdResolver buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        List<SProjectFeatureDescriptor> webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testProjectConfig/projects/Root/project-config.xml"));
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        WebHookConfig webHookConfig = converter.convert(webhooksAsProjectFeatures.get(0));
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
        assertEquals(2, webHookConfig.getEnabledBuildTypesSet().size());
    }
	
	@Test
	public void testConvertWhenAllBuildsEnabled() throws JDOMException, IOException {
        BuildTypeIdResolver buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
        WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        List<SProjectFeatureDescriptor> webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testProjectConfig/projects/Root/project-config.xml"));
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        WebHookConfig webHookConfig = converter.convert(webhooksAsProjectFeatures.get(3));
        assertEquals("http://localhost:8111/webhooks/endpoint.html?vcs_test=2", webHookConfig.getUrl());
        assertTrue(webHookConfig.isEnabledForAllBuildsInProject());
	}

}
