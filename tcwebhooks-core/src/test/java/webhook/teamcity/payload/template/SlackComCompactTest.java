package webhook.teamcity.payload.template;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.entity.WebHookTemplates;
import webhook.testframework.WebHookSemiMockingFrameworkImpl;

public class SlackComCompactTest {
	
	protected SortedMap<String, String> map = new TreeMap<>();
	protected ExtraParameters  extraParameters  = new ExtraParameters(map); 
	protected WebHookSemiMockingFrameworkImpl framework;
	
	@Test
	public void testBuildStarted() throws IOException, JDOMException, InterruptedException, JAXBException {
		
		framework = WebHookSemiMockingFrameworkImpl.create(BuildStateEnum.BUILD_STARTED, extraParameters);
		
		WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(
														framework.getWebHookTemplateManager(), 
														framework.getWebHookPayloadManager(), 
														framework.getWebHookTemplateJaxHelper(),
														framework.getProjectIdResolver(),
														null);
		slackCompact.register();
		framework.getWebHookTemplateManager().registerTemplateFormatFromSpring(slackCompact);
		
		WebHookTemplates templatesList =  framework.getWebHookTemplateJaxHelper().readTemplates("src/test/resources/testSlackCompactOverriden/webhook-templates.xml");
		framework.getWebHookTemplateManager().registerAllXmlTemplates(templatesList);		
		
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-slackcompact-jsonTemplate-AllEnabled.xml"));
		framework.getWebHookListener().buildStarted(framework.getRunningBuild());
		assertEquals("Post should have been executed", 1, framework.getWebHookFactory().getMostRecentMock().getInvocationCount());
		
	}
	
	@Test
	public void testBuildSuccessful() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookSemiMockingFrameworkImpl.create(BuildStateEnum.BUILD_SUCCESSFUL, extraParameters);
		
		WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(
				framework.getWebHookTemplateManager(), 
				framework.getWebHookPayloadManager(), 
				framework.getWebHookTemplateJaxHelper(),
				framework.getProjectIdResolver(),
				null);
		slackCompact.register();
		framework.getWebHookTemplateManager().registerTemplateFormatFromSpring(slackCompact);
		
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-slackcompact-jsonTemplate-AllEnabled.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		assertEquals("Post should have been executed", 1, framework.getWebHookFactory().getMostRecentMock().getInvocationCount());
		
	}

}
