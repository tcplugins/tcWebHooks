package webhook.teamcity.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jdom.JDOMException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerSettings;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.format.WebHookPayloadJsonVelocityTemplate;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.payload.template.LegacyJsonWebHookTemplate;
import webhook.teamcity.payload.template.SlackComXmlWebHookTemplate;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookMainConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.WebHookSettingsManagerImpl;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookSemiMockingFrameworkImpl;
import webhook.testframework.util.ConfigLoaderUtil;

public class StatisticsReportAssemblerTest extends BaseStatisticsTest {
	
	@Mock 
	SProject sProject;
	@Mock 
	SProject sProject2;
	@Mock 
	SProject sProject3;

	@Mock
	ServerSettings serverSettings;
	
	@Mock
	SBuildServer sBuildServer;
	
	@Mock 
	ConfigActionFactory configActionFactory;
	
	@Mock
	WebHookPluginDataResolver webHookPluginDataResolver;
	
	@Mock 
	ProjectManager projectManager;
	
	StatisticsReportAssemblerImpl reportAssembler;

	@Spy
	WebHookProjectSettings projectSettings;
	@Spy
	WebHookProjectSettings projectSettings2;
	@Spy
	WebHookProjectSettings projectSettings3;
	
	@InjectMocks
	StatisticsManagerImpl statisticsManager;
	
	@Mock
	WebHookHistoryRepository webHookHistoryRepository;

	@Mock
	private WebHookFeaturesStore webhookFeaturesStore;
	
	@Spy
	private WebHookSettingsManager webHookSettingsManager;
	
	@Spy
	MockingStatisticsJaxHelper jaxHelper = new MockingStatisticsJaxHelper();
	
	@Mock
	ServerPaths serverPaths;
	
	@Mock
	WebHookMainSettings webHookMainSettings;
	
	ValueHasher plainHasher = new NoOpValueHasher();
	ValueHasher cryptHasher = new CryptValueHasher();


	@Before
	public void setup() throws JDOMException, IOException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(webHookPluginDataResolver.getWebHooksCoreVersion()).thenReturn("1.2.0");
		Mockito.when(webHookPluginDataResolver.getWebHooksRestApiVersion()).thenReturn("1.2.1");
		
		Mockito.when(serverSettings.getServerUUID()).thenReturn("1234567890");
		Mockito.when(sBuildServer.getBuildNumber()).thenReturn("98765");
		Mockito.when(sBuildServer.getFullServerVersion()).thenReturn("2019.2.1 (build 71758)");
		
		when(sProject.getProjectId()).thenReturn("project01");
		when(sProject.getExternalId()).thenReturn("MyProject");
		when(sProject.getName()).thenReturn("My Project");
		
		when(sProject2.getProjectId()).thenReturn("project02");
		when(sProject2.getExternalId()).thenReturn("MyProject2");
		when(sProject2.getName()).thenReturn("My Project2");
		
		when(sProject3.getProjectId()).thenReturn("project03");
		when(sProject3.getExternalId()).thenReturn("MyProject3");
		when(sProject3.getName()).thenReturn("My Project3");
		
		when(projectManager.getActiveProjects()).thenReturn(Arrays.asList(sProject, sProject2, sProject3));
		when(projectManager.findProjectById("project01")).thenReturn(sProject);
		when(projectManager.findProjectById("project02")).thenReturn(sProject2);
		when(projectManager.findProjectById("project03")).thenReturn(sProject3);

		projectSettings = configureProjectSettings(sProject, "src/test/resources/project-settings-test-elastic.xml");
		projectSettings2 = configureProjectSettings(sProject2, "src/test/resources/project-settings-test-slack.xml");
		projectSettings3 = configureProjectSettings(sProject3, "src/test/resources/project-settings-test-all-states-enabled-with-filters.xml");

		WebHookMockingFramework framework = WebHookSemiMockingFrameworkImpl.create(
				BuildStateEnum.BUILD_STARTED,
				new ExtraParameters(new HashMap<String, String>())
		);
		
		ElasticSearchXmlWebHookTemplate elasticTemplate = new ElasticSearchXmlWebHookTemplate(
				framework.getWebHookTemplateManager(),
				framework.getWebHookPayloadManager(),
				new WebHookTemplateJaxHelperImpl(),
				framework.getProjectIdResolver(),
				null
		);
		elasticTemplate.register();
		
		SlackComXmlWebHookTemplate slackTemplate = new SlackComXmlWebHookTemplate(
				framework.getWebHookTemplateManager(),
				framework.getWebHookPayloadManager(),
				new WebHookTemplateJaxHelperImpl(),
				framework.getProjectIdResolver(),
				null
				);
		
		slackTemplate.register();
		
		WebHookPayloadJson jsonFormat = new WebHookPayloadJson(framework.getWebHookPayloadManager(), framework.getWebHookVariableResolverManager());
		jsonFormat.register();
		
		WebHookPayloadJsonVelocityTemplate jsonVelocityFormat = new WebHookPayloadJsonVelocityTemplate(framework.getWebHookPayloadManager(), framework.getWebHookVariableResolverManager());
		jsonVelocityFormat.register();
		
		LegacyJsonWebHookTemplate legacyJsonTemplate = new LegacyJsonWebHookTemplate(
				framework.getWebHookTemplateManager()
				);
		
		legacyJsonTemplate.register();
		
		webHookSettingsManager = new WebHookSettingsManagerImpl(
				projectManager, 
				null, webhookFeaturesStore, 
				framework.getWebHookTemplateManager(), 
				framework.getWebHookPayloadManager(), 
				new WebAddressTransformerImpl());
		
		webHookSettingsManager.initialise();
		
		WebHookMainConfig webHookMainConfig = new WebHookMainConfig();
		webHookMainConfig.setProxyHost("proxyHost");
		webHookMainConfig.setProxyPort(8080);
		
		when(webHookMainSettings.getWebHookMainConfig()).thenReturn(webHookMainConfig);
		reportAssembler = new StatisticsReportAssemblerImpl(serverSettings, sBuildServer, webHookPluginDataResolver, webHookSettingsManager, webHookMainSettings, framework.getWebHookTemplateManager());
	}

	private WebHookProjectSettings configureProjectSettings(SProject sProject, String pathname) throws JDOMException, IOException {
		WebHookProjectSettings settings = new WebHookProjectSettings();
		settings.readFrom(ConfigLoaderUtil.getFullConfigElement(new File(pathname)).getChild("webhooks"));
		settings.getWebHooksConfigs().forEach(c -> {
			c.setProjectInternalId(sProject.getProjectId());
			c.setProjectExternalId(sProject.getProjectId());
		});
		when(webhookFeaturesStore.getWebHookConfigs(sProject)).thenReturn(settings);
		return settings;
	}

	@Test
	public void testAssembleStatisticsReports() { //NOSONAR - just information
		List<StatisticsEntity> stats = statisticsManager.getUnreportedHistoricalStatisticsEntities(LocalDate.parse("2020-11-01"), LocalDate.parse("2020-11-05"));
		StatisticsReport plainReport = reportAssembler.assembleStatisticsReports(plainHasher, stats);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(plainReport));
	}
	
	@Test
	public void testAssembleStatisticsReportsWithCryptHasher() { //NOSONAR - just information
		List<StatisticsEntity> stats = statisticsManager.getUnreportedHistoricalStatisticsEntities(LocalDate.parse("2020-11-01"), LocalDate.parse("2020-11-05"));
		StatisticsReport plainReport = reportAssembler.assembleStatisticsReports(cryptHasher, stats);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(plainReport));
	}

	@Test
	public void testAssembleWebHooksPluginInfo() {
		WebHooksPluginInfo webHookPluginInfo = reportAssembler.assembleWebHooksPluginInfo(plainHasher);
		assertEquals("1.2.0", webHookPluginInfo.getTcWehooksVersion());
		assertEquals("1.2.1", webHookPluginInfo.getTcWebHooksRestApiVersion());
	}
	
	@Test
	public void testAssembleWebHooksPluginInfoWithCryptHasher() {
		WebHooksPluginInfo webHookPluginInfo = reportAssembler.assembleWebHooksPluginInfo(cryptHasher);
		assertEquals("1.2.0", webHookPluginInfo.getTcWehooksVersion());
		assertEquals("1.2.1", webHookPluginInfo.getTcWebHooksRestApiVersion());
	}

	@Test
	public void testAssembleTeamCityInstanceInfo() {
		TeamCityInstanceInfo instanceInfo = reportAssembler.assembleTeamCityInstanceInfo(plainHasher);
		assertEquals("1234567890", instanceInfo.getTeamcityId());
		assertEquals("2019.2.1 (build 71758)", instanceInfo.getTeamcityVersion());
		assertEquals("98765", instanceInfo.getTeamcityBuild());
		assertEquals(true, instanceInfo.isWebHookProxyConfigured());
	}

	@Test
	public void testAssembleWebHookConfigurationStatistics() {
		WebHookConfigurationStatistics configStats = reportAssembler.assembleWebHookConfigurationStatistics(plainHasher);
		assertEquals((Integer)3, configStats.getAuthTypes().get("none"));
		assertEquals((Integer)1, configStats.getFormats().get("json"));
		assertEquals((Integer)1, configStats.getFormats().get("jsonTemplate"));
		assertEquals((Integer)1, configStats.getFormats().get("jsonVelocityTemplate"));
		assertEquals((Integer)3, configStats.getConfigurationCount());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(configStats));
	}

	@Test
	public void testAssembleWebHookStatisticsReports() throws StatisticsFileOperationException, JDOMException, IOException {
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T01:30:00")));
		Mockito.when(serverPaths.getConfigDir()).thenReturn(folder.getRoot().getAbsolutePath());
		
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T03:00:00.000"));
		assertEquals(1, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T03:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T04:30:00")));
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T05:00:00.000"));
		assertEquals(2, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T05:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		List<StatisticsEntity> stats = statisticsManager.getUnreportedHistoricalStatisticsEntities(LocalDate.parse("2020-11-01"), LocalDate.parse("2020-11-05"));
		List<StatisticsSnapshot> snapshots = reportAssembler.assembleWebHookStatisticsReports(plainHasher, stats);
		assertFalse(snapshots.isEmpty());
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdaptor()).setPrettyPrinting().create();
		System.out.println(gson.toJson(snapshots));
	}
	
	@Test
	public void testAssembleWebHookStatisticsReportsWithCryptHasher() throws StatisticsFileOperationException, JDOMException, IOException {
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T01:30:00")));
		Mockito.when(serverPaths.getConfigDir()).thenReturn(folder.getRoot().getAbsolutePath());
		
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T03:00:00.000"));
		assertEquals(1, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T03:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T04:30:00")));
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T05:00:00.000"));
		assertEquals(2, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T05:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		List<StatisticsEntity> stats = statisticsManager.getUnreportedHistoricalStatisticsEntities(LocalDate.parse("2020-11-01"), LocalDate.parse("2020-11-05"));
		List<StatisticsSnapshot> snapshots = reportAssembler.assembleWebHookStatisticsReports(cryptHasher, stats);
		assertFalse(snapshots.isEmpty());
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdaptor()).setPrettyPrinting().create();
		System.out.println(gson.toJson(snapshots));
	}

}
