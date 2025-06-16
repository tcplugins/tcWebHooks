package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

public class ElasticSearchWebHookTemplateTest extends AbstractSpringTemplateTest {


	@Test
	public void test() throws JDOMException, IOException {

		WebHookConfig webhookElastic  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-elastic.xml"));
		WebHook wh = webHookFactory.getWebHook(webhookElastic,null);
		
		wh = webHookContentBuilder.buildWebHookContent(wh, webhookElastic, sRunningBuild, BuildStateEnum.BUILD_STARTED, null, null, true, Collections.emptyMap());
		System.out.println(wh.getPayload());
		assertTrue(wh.getPayload().contains("\"build_status_url\": \"http://my-server/viewLog.html?buildTypeId=ATestProject_TestBuild&buildId=123456\""));
	}

	@Override
	public String getUrl() {
		return "http://some.test.exmaple.com/test/test";
	}

	@Override
	public WebHookPayloadTemplate getTemplateInstance() {
		return new ElasticSearchXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
	}
	
	

}
