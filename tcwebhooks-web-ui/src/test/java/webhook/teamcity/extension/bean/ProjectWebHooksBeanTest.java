package webhook.teamcity.extension.bean;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBeanGsonSerialiser;
import webhook.teamcity.extension.util.ProjectHistoryResolver;
import webhook.teamcity.extension.util.ProjectHistoryResolver.ProjectHistoryBean;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class ProjectWebHooksBeanTest {

	SortedMap<String, String> map = new TreeMap<String, String>();
	ExtraParameters  extraParameters  = new ExtraParameters(map); 
	WebHookMockingFramework framework;

	@Test
	public void JsonSerialisationTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats(), framework.getWebHookSettingsManager(), framework.getServer().getProjectManager());
		RegisteredWebhookAuthenticationTypesBean authBean = RegisteredWebhookAuthenticationTypesBean.build(framework.getWebHookAuthenticatorProvider());
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig, authBean)));
	}
	
	@Test
	public void JsonBuildSerialisationTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats(), framework.getWebHookSettingsManager(), framework.getServer().getProjectManager());
		ProjectHistoryBean history = ProjectHistoryResolver.getProjectHistory(framework.getServer().getProjectManager().findProjectById("project01"));
		RegisteredWebhookAuthenticationTypesBean authBean = RegisteredWebhookAuthenticationTypesBean.build(framework.getWebHookAuthenticatorProvider());
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(history));
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig, authBean)));
	}
	
	@Test
	public void JsonBuildSerialisationWithTemplatesTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats(), framework.getWebHookSettingsManager(), framework.getServer().getProjectManager());
		ProjectHistoryBean history = ProjectHistoryResolver.getProjectHistory(framework.getServer().getProjectManager().findProjectById("project01"));
		RegisteredWebhookAuthenticationTypesBean authBean = RegisteredWebhookAuthenticationTypesBean.build(framework.getWebHookAuthenticatorProvider());
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(history));
		System.out.println(RegisteredWebHookTemplateBeanGsonSerialiser.serialise(template));
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig, authBean)));
	}
	
	@Test
	public void JsonBuildSerialisationWithTemplatesAndAuthTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats(), framework.getWebHookSettingsManager(), framework.getServer().getProjectManager());
		ProjectHistoryBean history = ProjectHistoryResolver.getProjectHistory(framework.getServer().getProjectManager().findProjectById("project01"));
		RegisteredWebhookAuthenticationTypesBean authBean = RegisteredWebhookAuthenticationTypesBean.build(framework.getWebHookAuthenticatorProvider());
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(history));
		System.out.println(RegisteredWebHookTemplateBeanGsonSerialiser.serialise(template));
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig, authBean)));
	}
	
	@Test
	public void GsonBuildSerialisationWithTemplatesAndAuthTest() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
		ProjectWebHooksBean webhooksConfig = ProjectWebHooksBean.build(framework.getWebHookProjectSettings() ,framework.getServer().getProjectManager().findProjectById("project01"), framework.getWebHookPayloadManager().getRegisteredFormatsAsCollection(), framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")));
		RegisteredWebHookTemplateBean template = RegisteredWebHookTemplateBean.build(framework.getWebHookTemplateResolver().findWebHookTemplatesForProject(framework.getServer().getProjectManager().findProjectById("project01")), framework.getWebHookPayloadManager().getRegisteredFormats(), framework.getWebHookSettingsManager(), framework.getServer().getProjectManager());
		ProjectHistoryBean history = ProjectHistoryResolver.getProjectHistory(framework.getServer().getProjectManager().findProjectById("project01"));
		RegisteredWebhookAuthenticationTypesBean authBean = RegisteredWebhookAuthenticationTypesBean.build(framework.getWebHookAuthenticatorProvider());
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(history));
		System.out.println(RegisteredWebHookTemplateBeanGsonSerialiser.serialise(template));
		System.out.println(ProjectWebHooksBeanGsonSerialiser.serialise(TemplatesAndProjectWebHooksBean.build(template, webhooksConfig, authBean)));
	}

}
