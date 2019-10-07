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

public class FlowdockWebHookTemplateTest extends AbstractSpringTemplateTest {
	
	@Test
	public void test() throws JDOMException, IOException {

		WebHookConfig webhookFlowDock  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-flowdock.xml"));
		WebHook wh = webHookFactory.getWebHook(webhookFlowDock,null);
		
		wh = webHookContentBuilder.buildWebHookContent(wh, webhookFlowDock, sRunningBuild, BuildStateEnum.BUILD_STARTED, null, null, true);
		System.out.println(wh.getPayload());
		assertTrue(wh.getPayload().contains("\"tags\": [ \"#TestBuild\", \"#ATestProject\", \"#buildStarted\", \"#master\", \"#teamcity\" ],"));
	}

	@Override
	public WebHookPayloadTemplate getTemplateInstance() {
		return new FlowdockXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
	}

	@Override
	public String getUrl() {
		return "http://some.test.exmaple.com/test/test";
	}

}
