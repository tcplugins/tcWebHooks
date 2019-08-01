package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

public class SlackComCompactWebHookTemplateTest extends AbstractSpringTemplateTest {

	@Test
	public void test() throws JDOMException, IOException {

		WebHookConfig webhookSlackCompact  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-slack.xml"));
		WebHook wh = webHookFactory.getWebHook(webhookSlackCompact,null);
		
		wh = webHookContentBuilder.buildWebHookContent(wh, webhookSlackCompact, sRunningBuild, BuildStateEnum.BUILD_STARTED, null, null, true);
		System.out.println(wh.getPayload());
		assertTrue(wh.getPayload().contains("{ \"title\" : \"Project Name\", \"value\" : \"<http://my-server//project.html?projectId=ATestProject|Test Project>\", \"short\": true },"));
	}

	@Override
	public String getUrl() {
		return "http://some.test.exmaple.com/test/test";
	}

	@Override
	public WebHookPayloadTemplate getTemplateInstance() {
		return new SlackComXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper);
	}

}
