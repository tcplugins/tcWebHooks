package webhook.teamcity.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.jdom.JDOMException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import webhook.WebHookExecutionStats;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

public abstract class BaseStatisticsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private String projectId = "project01";
	private String projectName = "My Project Name";
	private String buildTypeId = "bt01";
	private String buildTypeName = "My Build Type";
	private String buildTypeExternalId = "MyProjectName_MyBuildType";
	private Long buildId = 10000L;

	protected static class MockingStatisticsJaxHelper extends StatisticsJaxHelper {
			
			int writeCount = 0;
			StatisticsEntity lastBean;
			
			@Override
			public void writeFile(StatisticsEntity bean, Class<StatisticsEntity> clazz, String configFilePath)
					throws JAXBException {
				super.writeFile(bean, clazz, configFilePath);
				this.lastBean = bean;
				writeCount++;
			}
			
			public int getWriteCount() {
				return writeCount;
			}
			
			public StatisticsEntity getLastBean() {
				return lastBean;
			}
		}

	protected Map<LocalDate, List<WebHookHistoryItem>> buildStats(LocalDateTime timestamp) throws JDOMException, IOException {
		WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();
		
		WebHookConfig webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled.xml"));
		WebHookExecutionStats webHookExecutionStats = new WebHookExecutionStats();
		webHookExecutionStats.setStatusCode(200);
		webHookExecutionStats.setStatusReason("OK");
		WebHookErrorStatus webhookErrorStatus = null;
		GeneralisedWebAddress generalisedWebAddress = webAddressTransformer.getGeneralisedHostName(webHookConfig.getUrl());
		WebHookHistoryItem item = new WebHookHistoryItem(projectId , projectName , buildTypeId , buildTypeName, buildTypeExternalId, buildId, webHookConfig, webHookExecutionStats, webhookErrorStatus, timestamp, generalisedWebAddress, false);
		Map<LocalDate, List<WebHookHistoryItem>> items = new HashMap<>();
		items.put(timestamp.toLocalDate(), Collections.singletonList(item));
		return items;
	}

	public BaseStatisticsTest() {
		super();
	}

}