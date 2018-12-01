package webhook.teamcity.history;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.WebHook;
import webhook.WebHookExecutionStats;
import webhook.WebHookProxyConfig;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;

public class WebHookHistoryRepositoryImplTest {
	
	@Mock
	SBuild sBuild01;

	@Mock
	SBuild sBuild02;
	
	WebHookConfig whc1;
	WebHookConfig whc2;
	
	WebHookExecutionStats stats01 = new WebHookExecutionStats("url");
	WebHookExecutionStats stats02 = new WebHookExecutionStats("url");
	
	private final int pageNumber = 1;
	private final int pageSize = 20;
	
	@Test
	public void testAddHistoryItem() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(2, historyRepository.getTotalCount());
	}
	
	@Test
	public void testGetHistoryItem() {
		WebHookHistoryRepository historyRepository = setupMocks();
		UUID trackingId = stats01.getTrackingId();
		assertEquals(stats01.getStatusCode(), historyRepository.getHistoryItem(trackingId.toString()).getWebHookExecutionStats().getStatusCode());
	}
	
	@Test
	public void testGetHistoryItemForInvalidItemReturnsNull() {
		WebHookHistoryRepository historyRepository = setupMocks();
		UUID trackingId = stats01.getTrackingId();
		assertEquals(stats01.getStatusCode(), historyRepository.getHistoryItem(trackingId.toString()).getWebHookExecutionStats().getStatusCode());
	}

	@Test
	public void testFindHistoryItemsForProject01ShouldReturnOne() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(2, historyRepository.findHistoryItemsForProject("project01", pageNumber, pageSize).getItems().size());
	}
	
	@Test
	public void testFindHistoryItemsForProject02ShouldReturnZero() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(0, historyRepository.findHistoryItemsForProject("project02", pageNumber, pageSize).getItems().size());
	}
	
	@Test
	public void testFindHistoryItemsForBuildType() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(1, historyRepository.findHistoryItemsForBuildType("bt01", pageNumber, pageSize).getItems().size());
	}
	
	@Test
	public void testFindHistoryItemsForBuild() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(1, historyRepository.findHistoryItemsForBuild(01L, pageNumber, pageSize).getItems().size());
	}
	
	@Test
	public void testFindHistoryItemsInError() {
		WebHookHistoryRepository historyRepository = setupMocks();
		assertEquals(1, historyRepository.findHistoryErroredItems(pageNumber, pageSize).getItems().size());
	}
	
	@Test
	public void testFindHistoryErroredItemsGroupedByDay() {
		WebHookHistoryRepository historyRepository = setupMocks();
		Map<LocalDate, List<WebHookHistoryItem>> historyItems = historyRepository.findHistoryErroredItemsGroupedByDay(LocalDate.now(), 8);
		assertEquals(8, historyItems.entrySet().size());
		int count = 0;
		for (List<WebHookHistoryItem> items : historyItems.values()) {
			count += items.size(); 
		}
		assertEquals(1, count);
	}
	
	@Test
	public void testFindHistoryNonErroredItemsGroupedByDay() {
		WebHookHistoryRepository historyRepository = setupMocks();
		Map<LocalDate, List<WebHookHistoryItem>> historyItems = historyRepository.findHistoryOkItemsGroupedByDay(LocalDate.now(), 8);
		assertEquals(8, historyItems.entrySet().size());
		int count = 0;
		for (List<WebHookHistoryItem> items : historyItems.values()) {
			count += items.size(); 
		}
		assertEquals(1, count);
	}
	
	@Test
	public void testFindHistoryAllItemsGroupedByDay() {
		WebHookHistoryRepository historyRepository = setupMocks();
		Map<LocalDate, List<WebHookHistoryItem>> historyItems = historyRepository.findHistoryAllItemsGroupedByDay(LocalDate.now(), 8);
		assertEquals(8, historyItems.entrySet().size());
		int count = 0;
		for (List<WebHookHistoryItem> items : historyItems.values()) {
			count += items.size(); 
		}
		assertEquals(2, count);
	}

	private WebHookHistoryRepository setupMocks() {
		Period fiveDays = new Period().withDays(5);
		MockitoAnnotations.initMocks(this);
		stats01.setStatusCode(200);
		stats01.setInitTimeStamp(LocalDateTime.now().minus(fiveDays).toDate());
		stats02.setStatusCode(403);
		stats02.setErrored(true);
		stats02.setInitTimeStamp(LocalDateTime.now().minus(fiveDays).toDate());

		WebHook webhook01 = new SimpleMockedWebHook(stats01);
		WebHook webhook02 = new SimpleMockedWebHook(stats02);
		when(sBuild01.getBuildTypeId()).thenReturn("bt01");
		when(sBuild02.getBuildTypeId()).thenReturn("bt02");
		when(sBuild01.getProjectId()).thenReturn("project01");
		when(sBuild02.getProjectId()).thenReturn("project01");
		when(sBuild01.getBuildId()).thenReturn(01L);
		when(sBuild02.getBuildId()).thenReturn(02L);
		
		whc1 = new WebHookConfig("project01", "MyProject", "http://url/1", true, new BuildState().setAllEnabled(), "testFormat", "jsonTemplate", true, true, null, null);
		whc2 = new WebHookConfig("project01", "MyProject", "http://url/2", true, new BuildState().setAllEnabled(), "testFormat", "jsonTemplate", true, true, null, null);
		
		WebHookHistoryRepository historyRepository = new WebHookHistoryRepositoryImpl();
		historyRepository.addHistoryItem(new WebHookHistoryItem(whc1, webhook01.getExecutionStats(), sBuild01, null));
		historyRepository.addHistoryItem(new WebHookHistoryItem(whc2, webhook02.getExecutionStats(), sBuild02, null));
		return historyRepository;
	}

	
	public static class SimpleMockedWebHook implements WebHook {

		private String errorReason;
		private Boolean errored;
		private boolean enabled;
		private String url;
		private Integer status;
		private WebHookExecutionStats stats;
		
		public SimpleMockedWebHook(WebHookExecutionStats stats) {
			this.stats = stats;
			this.status = stats.getStatusCode();
		}

		@Override
		public void setProxy(WebHookProxyConfig proxyConfig) {
			notImplemented();
		}

		@Override
		public void setProxy(String proxyHost, Integer proxyPort) {
			notImplemented();
			
		}

		@Override
		public void setProxyUserAndPass(String username, String password) {
			notImplemented();
			
		}

		@Override
		public void post() throws IOException {
			notImplemented();
			
		}

		@Override
		public Integer getStatus() {
			return this.status;
		}

		@Override
		public String getProxyHost() {
			notImplemented();
			return null;
		}

		@Override
		public int getProxyPort() {
			notImplemented();
			return 0;
		}

		@Override
		public String getUrl() {
			return this.url;
		}

		@Override
		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String getParameterisedUrl() {
			notImplemented();
			return null;
		}

		@Override
		public String parametersAsQueryString() {
			notImplemented();
			return null;
		}

		@Override
		public void addParam(String key, String value) {
			notImplemented();
		}

		@Override
		public void addParams(List<NameValuePair> paramsList) {
			notImplemented();
		}

		@Override
		public void addParams(Map<String, String> paramsList) {
			notImplemented();
		}

		@Override
		public String getParam(String key) {
			notImplemented();
			return null;
		}

		@Override
		public Boolean isEnabled() {
			return this.enabled;
		}

		@Override
		public void setEnabled(Boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void setEnabled(String enabled) {
			if ("true".equalsIgnoreCase(enabled)){
				this.enabled = true;
			} else {
				this.enabled = false;
			}
		}

		@Override
		public Boolean isErrored() {
			return this.errored;
		}

		@Override
		public void setErrored(Boolean errored) {
			this.errored = errored;
			
		}

		@Override
		public String getErrorReason() {
			return this.errorReason;
		}

		@Override
		public void setErrorReason(String errorReason) {
			this.errorReason = errorReason;
		}

		@Override
		public BuildState getBuildStates() {
			notImplemented();
			return null;
		}

		@Override
		public void setBuildStates(BuildState states) {
			notImplemented();
		}

		@Override
		public String getProxyUsername() {
			notImplemented();
			return null;
		}

		@Override
		public void setProxyUsername(String proxyUsername) {
			notImplemented();
		}

		@Override
		public String getProxyPassword() {
			notImplemented();
			return null;
		}

		@Override
		public void setProxyPassword(String proxyPassword) {
			notImplemented();
		}

		@Override
		public String getPayload() {
			notImplemented();
			return null;
		}

		@Override
		public void setPayload(String payloadContent) {
			notImplemented();
			
		}

		@Override
		public void setContentType(String contentType) {
			notImplemented();
		}

		@Override
		public void setCharset(String charset) {
			notImplemented();
		}

		@Override
		public void setAuthentication(WebHookAuthenticator authenticator) {
			notImplemented();
			
		}

		@Override
		public boolean checkFilters(VariableResolver variableResolver) {
			notImplemented();
			return false;
		}

		@Override
		public void addFilter(WebHookFilterConfig filterHolder) {
			notImplemented();
		}

		@Override
		public String getDisabledReason() {
			notImplemented();
			return null;
		}

		@Override
		public WebHookExecutionStats getExecutionStats() {
			return this.stats;
		}

		@Override
		public SFinishedBuild getPreviousNonPersonalBuild() {
			notImplemented();
			return null;
		}

		@Override
		public void setPreviousNonPersonalBuild(SFinishedBuild localSFinishedBuild) {
			notImplemented();
		}

		@Override
		public void setConnectionTimeOut(int httpConnectionTimeout) {
			notImplemented();
		}

		@Override
		public void setResponseTimeOut(int httpResponseTimeout) {
			notImplemented();
		}
		
		private void notImplemented() {
			throw new RuntimeException("I'm a mock");
		}

		@Override
		public void setEnabledForBuildState(BuildStateEnum buildState, boolean enabled) {
			this.setEnabled(enabled);
			if (! enabled) {
				this.getExecutionStats().setStatusReason(buildState.getShortDescription());
			}
		}

		@Override
		public void addHeaders(List<WebHookHeaderConfig> headers) {
			notImplemented();
		}

		@Override
		public void resolveHeaders(VariableResolver variableResolver) {
			notImplemented();
		}

		@Override
		public VariableResolverFactory getVariableResolverFactory() {
			notImplemented();
			return null;
		}

		@Override
		public void setVariableResolverFactory(VariableResolverFactory variableResolverFactory) {
			notImplemented();
		}
		
	}
}
