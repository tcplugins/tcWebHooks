package webhook.teamcity.payload.template;

import static org.junit.Assert.assertFalse;
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

public class StatisticsWebHookTemplateTest extends AbstractSpringTemplateTest {

	@Test
	public void test() throws JDOMException, IOException {
		
		WebHookConfig webhookSlackCompact  = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-before-statistics.xml"));
		WebHook wh = webHookFactory.getWebHook(webhookSlackCompact,null);
		assertTrue(wh.isEnabled());
		assertFalse(wh.getBuildStates().enabled(BuildStateEnum.REPORT_STATISTICS));
	}

	@Override
	public String getUrl() {
		return "http://some.test.exmaple.com/test/test";
	}

	@Override
	public WebHookPayloadTemplate getTemplateInstance() {
		return new StatisticsReportWebHookTemplate(templateManager);
	}

}
