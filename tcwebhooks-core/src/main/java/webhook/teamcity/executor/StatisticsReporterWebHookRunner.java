package webhook.teamcity.executor;

import java.util.Collections;

import org.apache.commons.codec.digest.HmacUtils;
import org.joda.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.SProject;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.statistics.LocalDateTypeAdaptor;
import webhook.teamcity.statistics.StatisticsReport;

public class StatisticsReporterWebHookRunner extends AbstractWebHookExecutor {

	private static final String HMAC_HEADER_NAME = "x-tcwebhooks-hmac";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private StatisticsReport myStatisticsReport;
    private VariableMessageBuilder simpleVariableMessageBuilder = new SimpleCopyingVariableMessageBuilder();
	private SProject rootProject;

	public StatisticsReporterWebHookRunner(WebHookContentBuilder webHookContentBuilder,
			WebHookHistoryRepository webHookHistoryRepository, WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookConfig whc, BuildStateEnum state, boolean overrideIsEnabled, WebHook webhook, boolean isTest,
			SProject rootProject,
			StatisticsReport report) {
		super(webHookContentBuilder, webHookHistoryRepository, webHookHistoryItemFactory, whc, state, overrideIsEnabled,
				webhook, isTest);
		this.myStatisticsReport = report;
		this.rootProject = rootProject;
	}

	@Override
	protected WebHook getWebHookContent() {
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdaptor()).setPrettyPrinting().create();
		webhook.setContentType("application/json");
		webhook.setCharset("UTF-8");
		webhook.setPayload(gson.toJson(myStatisticsReport));
		webhook.setUrl(whc.getUrl());
		webhook.addHeaders(Collections.singletonList(
		        new WebHookHeaderConfig(HMAC_HEADER_NAME, 
		                new HmacUtils(HMAC_ALGORITHM, myStatisticsReport.getPluginInfo().getTcWehooksVersion())
		                .hmacHex(webhook.getPayload()))
		        ));
		webhook.resolveHeaders(this.simpleVariableMessageBuilder); // The webhook only sends resolved headers 
		return webhook;
	}

	@Override
	public WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
		if (this.isTest) {
			return webHookHistoryItemFactory.getWebHookHistoryTestItem(
					whc,
					webhook.getExecutionStats(), 
					this.rootProject,
					errorStatus
				);			
		} else {
			return webHookHistoryItemFactory.getWebHookHistoryItem(
					whc,
					webhook.getExecutionStats(), 
					this.rootProject,
					errorStatus
				);
		}
	}
	
	@Override
	protected void errorCallback(RuntimeException exception) {
		throw exception;
	}
	
	protected static class SimpleCopyingVariableMessageBuilder implements VariableMessageBuilder {

        @Override
        public String build(String template) {
            return template; // Just return template without doing any substitution
        }

        @Override
        public void addWebHookPayload(String webHookContent) {
            // NOOP
        }
	}
	
}
