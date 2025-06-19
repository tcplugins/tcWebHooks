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
import webhook.teamcity.BuildTypeIdResolver;
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
            MockSProject myProject = new MockSProject("My Project", "My Example Project", "project01", "MyProjectId", sBuildType);
            myProject.addANewBuildTypeToTheMock(sBuildType2);
            sBuildType.setProject(myProject);
            sBuildType2.setProject(myProject);
            when(projectManager.findBuildTypeByExternalId("MyProjectId_TcDummyDeb")).thenReturn(sBuildType);
            when(projectManager.findBuildTypeByExternalId("MyProjectId_TcWebHooks")).thenReturn(sBuildType2);
            when(projectManager.findBuildTypeById("bt1")).thenReturn(sBuildType);
            when(projectManager.findBuildTypeById("bt2")).thenReturn(sBuildType2);
            
            buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
            authenticatorProvider = new WebHookAuthenticatorProvider();
            authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
            authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
            webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testProjectConfig/projects/Root/project-config.xml"));
            converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
        }
    
    /*
     
project {
    description = "Small Kotlin based project from VCS"
    features {

        webHookConfiguration {
            webHookId = "MyProjectId_WebHook_02"
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
            webHookId = "MyProjectId_WebHook_03"
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
            webHookId = "MyProjectId_WebHook_04"
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
            webHookId = "MyProjectId_WebHook_05"
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
            webHookId = "MyProjectId_WebHook_06"
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
            webHookId = "MyProjectId_WebHook_07"
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
            webHookId = "MyProjectId_WebHook_08"
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
    public void testRenderAsKotlinDsl00() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(0));
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"MyProjectId_WebHook_01\"\n"
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
                + "        buildTypeId{\"MyProjectId_TcDummyDeb\"}\n"
                + "        buildTypeId{\"MyProjectId_TcWebHooks\"}\n"
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
    public void testRenderAsKotlinDsl02() throws JDOMException, IOException {
        WebHookConfig config = converter.convert(webhooksAsProjectFeatures.get(2)); // Index 1 is not a webhook
        String expectedResult = 
                  "webHookConfiguration {\n"
                + "    webHookId = \"MyProjectId_WebHook_02\"\n"
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
                + "        header(name = \"foo1\", value = \"bar1\")\n"
                + "        header(name = \"foo2\", value = \"bar2\")\n"
                + "        header(\"foo3\", \"bar3\")\n"
                + "    }\n"
                + "    parameters {\n"
                + "        parameter(name=\"colour\", value=\"blue\")\n"
                + "    }\n"
                + "}";

        String actualResult = new WebHookConfigToKotlinDslRenderer(authenticatorProvider, buildTypeIdResolver).renderAsKotlinDsl(config);
        assertEquals(expectedResult, actualResult);
    }

}
