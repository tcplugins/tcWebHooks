package netwolfuk.teamcity.plugins.tcwebhooks.template.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.FlowdockWebHookTemplate;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;



public class BuildFlowDockTemplateFiles extends TemplateGenerator {
	
	private static final String XML_TEMPLATES_FILE = "../tcwebhooks-core/src/test/resources/webhook-templates-flowdock-example.xml";
	
	/**
	 * This Test just builds template files from the contents of an XML template config.
	 * Note, the output from this test is not automatically validated in the next test.
	 * The resulting files output in this test must be copied to the correct path before they are tested.
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void BuildFlowDockTemplates() throws JAXBException, IOException {
		TemplateGenerator generator = new TemplateGenerator();
		generator.generate("flowdock", XML_TEMPLATES_FILE, "target");
		
	}
	
	/**
	 * Validates that the json files in the classpath have the same contents as the XML configuration version of the template.
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	@Test
	public void CompareXmlAndSpringTemplates() throws FileNotFoundException, JAXBException{
		SBuildServer sBuildServer = mock(SBuildServer.class);
		WebHookPayloadManager webHookPayloadManager = new WebHookPayloadManager(sBuildServer);
		WebHookTemplateManager springManager = new WebHookTemplateManager(null, webHookPayloadManager );
		FlowdockWebHookTemplate springTemplate =  new FlowdockWebHookTemplate(springManager);
		springTemplate.register();
		
		WebHookTemplateManager xmlManager = new WebHookTemplateManager(null, webHookPayloadManager );
		
		
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read(XML_TEMPLATES_FILE);
		for (webhook.teamcity.settings.entity.WebHookTemplate template : templatesList.getWebHookTemplateList()){
			xmlManager.registerTemplateFormatFromXmlConfig(WebHookTemplateFromXml.build(template, webHookPayloadManager));
		}
		
		WebHookTemplate springFlowTemplate = springManager.getTemplate("flowdock");
		WebHookTemplate xmlFlowTemplate = xmlManager.getTemplate("flowdock");
		
		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
			if (springFlowTemplate.getSupportedBuildStates().contains(state) || xmlFlowTemplate.getSupportedBuildStates().contains(state)){
				System.out.println(state.getShortName());
				assertEquals(state.getShortName() + " template should match", springFlowTemplate.getTemplateForState(state).getTemplateText().trim(), xmlFlowTemplate.getTemplateForState(state).getTemplateText().trim());
				assertEquals(state.getShortName() + " branch template should match", springFlowTemplate.getBranchTemplateForState(state).getTemplateText().trim(), xmlFlowTemplate.getBranchTemplateForState(state).getTemplateText().trim());
			}
		}
		
	}

	

	
}
