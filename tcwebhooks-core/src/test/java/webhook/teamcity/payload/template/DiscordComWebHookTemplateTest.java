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

public class DiscordComWebHookTemplateTest extends AbstractSpringTemplateTest {

	@Test
	public void test() throws JDOMException, IOException {

		WebHookConfig whc  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-discordcom.xml"));
		WebHook wh = webHookFactory.getWebHook(whc,null);
		
		wh = webHookContentBuilder.buildWebHookContent(wh, whc, sRunningBuild, BuildStateEnum.BUILD_STARTED, null, null, true);
		System.out.println(wh.getPayload());
		assertTrue(wh.getPayload().contains("{ \"name\" : \"Project Name\", \"value\" : \"[Test Project](http://my-server//project.html?projectId=ATestProject)\", \"inline\": true },"));

	}
	
	@Override
	public WebHookPayloadTemplate getTemplateInstance() {
		return new DiscordComXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper);
	}
	

	@Override
	public String getUrl() {
		return "http://some.test.exmaple.com/test/test";
	}

}
