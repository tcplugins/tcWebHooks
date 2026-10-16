package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.web.context.ContextLoader;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.settings.ProjectFeatureToWebHookConfigConverter;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHooksRequestLoadedConfigReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testProjectWebHooksEndpointWithLoadedConfigResponds() throws JAXBException, IOException, JDOMException {
        SortedMap<String, String> map = new TreeMap<>();
        ExtraParameters extraParameters = new ExtraParameters(map);

        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
        framework.loadWebHookProjectSettingsFromConfigXml(
                new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));

        Element webhooksParent = new Element("webhooks");
        framework.getWebHookProjectSettings().writeTo(webhooksParent);

        ProjectSettingsManager projectSettingsManager = ContextLoader.getCurrentWebApplicationContext()
                .getBean(ProjectSettingsManager.class);
        projectSettingsManager.readFrom(webhooksParent, "testProject");

        ProjectManager projectManager = ContextLoader.getCurrentWebApplicationContext()
                .getBean(ProjectManager.class);
        WebHookAuthenticatorProvider authenticatorProvider = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookAuthenticatorProvider.class);
        registerTestAuthFactories(authenticatorProvider);
        SProject project = projectManager.findProjectByExternalId("testProject");
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter(
                authenticatorProvider,
                new IdentityBuildTypeIdResolver());
        for (WebHookConfig config : framework.getWebHookProjectSettings().getWebHooksConfigs()) {
            config.setProjectInternalId(project.getProjectId());
            project.addFeature(converter.convert(config).getType(), converter.convert(config).getParameters());
        }

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());
    }

        private static void registerTestAuthFactories(WebHookAuthenticatorProvider authenticatorProvider) {
                new UsernamePasswordAuthenticatorFactory(authenticatorProvider).register();
                new BearerAuthenticatorFactory(authenticatorProvider).register();
        }

        private static class IdentityBuildTypeIdResolver implements BuildTypeIdResolver {
                @Override
                public String getExternalBuildTypeId(String internalBuildTypeId) {
                        return internalBuildTypeId;
                }

                @Override
                public java.util.Set<String> getExternalBuildTypeIds(java.util.Collection<String> internalBuildTypeIds) {
                        return new java.util.LinkedHashSet<>(internalBuildTypeIds);
                }

                @Override
                public String getInternalBuildTypeId(String externalBuildTypeId) {
                        return externalBuildTypeId;
                }

                @Override
                public java.util.Set<String> getInternalBuildTypeIds(java.util.Collection<String> externalBuildTypeIds) {
                        return new java.util.LinkedHashSet<>(externalBuildTypeIds);
                }
        }
}

