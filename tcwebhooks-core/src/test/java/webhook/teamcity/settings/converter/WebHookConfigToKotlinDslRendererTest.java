package webhook.teamcity.settings.converter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.ProjectAndBuildTypeResolverImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.teamcity.settings.ProjectFeatureToWebHookConfigConverter;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

@RunWith(MockitoJUnitRunner.class)
public class WebHookConfigToKotlinDslRendererTest {
    
        @Mock
        ProjectManager projectManager;
        private ProjectAndBuildTypeResolverImpl buildTypeIdResolver;
        private WebHookAuthenticatorProvider authenticatorProvider;
        private List<SProjectFeatureDescriptor> webhooksAsProjectFeatures;
        private ProjectFeatureToWebHookConfigConverter converter;
        
        @Before
        public void setup() throws JDOMException, IOException {
            MockSBuildType sBuildType = new MockSBuildType("TcDummyDeb", "TcDummyDeb build", "bt1");
            MockSBuildType sBuildType2 = new MockSBuildType("TcWebHooks", "TcWebHooks build", "bt2");
            MockSBuildType sBuildType3 = new MockSBuildType("TcChatBot", "TcChatBot build", "bt3");
            MockSProject myProject = new MockSProject("My Project", "My Example Project", "project01", "RootProjectId", sBuildType);
            myProject.addANewBuildTypeToTheMock(sBuildType2);
            sBuildType.setProject(myProject);
            sBuildType2.setProject(myProject);
            sBuildType3.setProject(myProject);
            when(projectManager.findBuildTypeByExternalId("RootProjectId_TcDummyDeb")).thenReturn(sBuildType);
            when(projectManager.findBuildTypeByExternalId("RootProjectId_TcWebHooks")).thenReturn(sBuildType2);
            when(projectManager.findBuildTypeByExternalId("RootProjectId_TcChatBot")).thenReturn(sBuildType3);
            when(projectManager.findBuildTypeById("bt1")).thenReturn(sBuildType);
            when(projectManager.findBuildTypeById("bt2")).thenReturn(sBuildType2);
            when(projectManager.findBuildTypeById("bt3")).thenReturn(sBuildType3);
            
            buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
            authenticatorProvider = new WebHookAuthenticatorProvider();
            authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
            authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
            webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testMigrationConfigurations/projects/FirstProject/project-config.xml"));
            converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        }
    
    /*
     
project {
    description = "Small Kotlin based project from VCS"
    features {

        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_02"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=2"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = basic {
                username = "myUserName"
                password = "myPassword"
                realm = "myRealm"
                preemptive = true
            }
            headers {
                header(name = "foo1", value = "bar1")
                header(name = "foo2", value = "bar2")
                header("foo3", "bar3")
            }
            parameters {
                parameter(name="colour", value="blue")
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_03"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=3"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_04"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=4"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates  {
                buildAddedToQueue = true
            }
            authentication = bearer {
                token = "new-bearer-toke"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_05"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=5"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_06"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=6"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_07"
            template = "slack.com-compact"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=7"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "this-is-my-token"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_08"
            template = "slack.com-compact"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=8"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "this-is-my-updated-token"
                preemptive = true
            }

            headers {
                header(name = "foo1", value = "bar1")
                header(name = "foo2", value = "bar2")
                header("foo3", "bar3")
            }
            parameters {
                parameter(name="colour", value="blue")
            }
        }
    }
      
     */

    @Test
    public void testRenderAsKotlinDsl_00() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(0));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_01\"\n"
                + "    template = \"legacy-json\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=1\"\n"
                + "    buildTypes = selectedProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "        /* Define the specific buildTypes that this webhook should execute for.\n"
                + "         * There are 3 ways to define a buildType.\n"
                + "         *\n"
                + "         *     This function takes an object of type \"jetbrains. buildServer. configs. kotlin.BuildType\".\n"
                + "         *     Typically, the buildType will already be defined in this file, and we can just reference it.\n"
                + "         * buildType(myBuildType)\n"
                + "         *\n"
                + "         *     This function takes the id of the BuildType. Again, we already know the buildType config, so\n"
                + "         *     we can use that by calling toString() on it.\n"
                + "         * buildTypeId(myBuildType.id.toString())\n"
                + "         *\n"
                + "         *     This example calls the same function, but we are hard coding the BuildType's ID string.\n"
                + "         *     This is the least preferred method as it would need to be updated if the ID changes.\n"
                + "         * buildTypeId(\"MyProjectId_MyBuildTypeId\")\n"
                + "         */\n"
                + "        \n"
                + "        buildTypeId{\"RootProjectId_TcDummyDeb\"}\n"
                + "        buildTypeId{\"RootProjectId_TcWebHooks\"}\n"
                + "        buildTypeId{\"RootProjectId_TcChatBot\"}\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "        buildRemovedFromQueue = true\n"
                + "    }\n"
                + "    authentication = basic {\n"
                + "        username = \"myUserName\"\n"
                + "        password = \"myPassword\"\n"
                + "        realm = \"myRealm\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "}";
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testRenderAsKotlinDsl_01() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(1));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_02\"\n"
                + "    template = \"legacy-json\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=2\"\n"
                + "    buildTypes = allProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "        buildRemovedFromQueue = true\n"
                + "    }\n"
                + "    authentication = basic {\n"
                + "        username = \"myUserName\"\n"
                + "        password = \"myPassword\"\n"
                + "        realm = \"myRealm\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "    headers {\n"
                + "        header(\"foo1\", \"bar1\")\n"
                + "        header(\"foo2\", \"bar2\")\n"
                + "        header(\"foo3\", \"bar3\")\n"
                + "    }\n"
                + "    parameters {\n"
                + "        parameter(\"colour\", \"blue\")\n"
                + "    }\n"
                + "}";

        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testRenderAsKotlinDsl_02() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(2));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_03\"\n"
                + "    template = \"legacy-json\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=3\"\n"
                + "    buildTypes = allProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "        buildRemovedFromQueue = true\n"
                + "    }\n"
                + "    authentication = bearer {\n"
                + "        token = \"dkfjsdlfjldfjk\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "}";
        
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testRenderAsKotlinDsl_03() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(3));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_04\"\n"
                + "    template = \"legacy-json\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=4\"\n"
                + "    buildTypes = allProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "    }\n"
                + "    authentication = bearer {\n"
                + "        token = \"new-bearer-toke\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "}";
        
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testRenderAsKotlinDsl_04() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(4));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_05\"\n"
                + "    template = \"legacy-json\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=5\"\n"
                + "    buildTypes = allProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "        buildRemovedFromQueue = true\n"
                + "    }\n"
                + "    authentication = bearer {\n"
                + "        token = \"dkfjsdlfjldfjk\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "}";
        
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testRenderAsKotlinDsl_07() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(7));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"RootProjectId_WebHook_08\"\n"
                + "    template = \"slack.com-compact\"\n"
                + "    url = \"http://localhost:8111/webhooks/endpoint.html?vcs_test=8\"\n"
                + "    buildTypes = allProjectBuilds {\n"
                + "        subProjectBuilds = true\n"
                + "    }\n"
                + "    buildStates {\n"
                + "        buildAddedToQueue = true\n"
                + "        buildRemovedFromQueue = true\n"
                + "    }\n"
                + "    authentication = bearer {\n"
                + "        token = \"this-is-my-updated-token\"\n"
                + "        preemptive = true\n"
                + "    }\n"
                + "    headers {\n"
                + "        header(\"foo1\", \"bar1\")\n"
                + "        header(\"foo2\", \"bar2\")\n"
                + "        header(\"foo3\", \"bar3\")\n"
                + "    }\n"
                + "    parameters {\n"
                + "        parameter(\n"
                + "            name = \"colour\",\n"
                + "            value = \"blue\",\n"
                + "            secure = true\n"
                + "        )\n"
                + "        parameter(\n"
                + "            name = \"fooParam2\",\n"
                + "            value = \"barParam2\",\n"
                + "            secure = true,\n"
                + "            forceResolveTeamCityVariable = true,\n"
                + "            includedInLegacyPayloads = true,\n"
                + "            templateEngine = \"VELOCITY\"\n"
                + "        )\n"
                + "        parameter(\n"
                + "            name = \"fooParam3\",\n"
                + "            value = \"barParam3\",\n"
                + "            secure = false,\n"
                + "            forceResolveTeamCityVariable = false,\n"
                + "            includedInLegacyPayloads = false,\n"
                + "            templateEngine = \"STANDARD\"\n"
                + "        )\n"
                + "    }\n"
                + "}";
        
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config, 0);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testConvertPluginSettingsToKotlonForDocs() throws JDOMException, IOException {
        WebHookConfig webhook = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/plugin-settings-with-lots-of-examples.xml"));
        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(webhook, 0);
        System.out.print(actualResult);
        
    }
}
