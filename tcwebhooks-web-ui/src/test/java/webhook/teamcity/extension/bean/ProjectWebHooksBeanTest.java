package webhook.teamcity.extension.bean;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBeanJsonSerialiser;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class ProjectWebHooksBeanTest {

	SortedMap<String, String> map = new TreeMap<String, String>();
	ExtraParametersMap  extraParameters  = new ExtraParametersMap(map); 
	ExtraParametersMap  teamcityProperties  = new ExtraParametersMap(map); 
	WebHookMockingFramework framework;

	@Test
	public void JsonSerialisationTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats());
		System.out.println(ProjectWebHooksBeanJsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig)));
	}
	
	@Test
	public void JsonBuildSerialisationTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats());
		System.out.println(ProjectWebHooksBeanJsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig)));
	}
	
	@Test
	public void JsonBuildSerialisationWithTemplatesTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats());
		System.out.println(RegisteredWebHookTemplateBeanJsonSerialiser.serialise(template));
		System.out.println(ProjectWebHooksBeanJsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig)));
	}

}
