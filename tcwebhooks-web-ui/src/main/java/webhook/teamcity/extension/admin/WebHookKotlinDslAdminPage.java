package webhook.teamcity.extension.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.diagnostics.DslDiagnosticsResult;
import webhook.teamcity.diagnostics.DslDiagnosticsService;
import webhook.teamcity.exception.DslDiagnotisticsException;

public class WebHookKotlinDslAdminPage extends AdminPage {
	public static final String ADMIN_ID = "tcWebHooksKotlinDsl";

	private final DslDiagnosticsService myDslDiagnosticsService;
	private final TeamCityCoreFacade myTeamCityCoreFacade;
	private final ServerSettings myServerSettings;


	public WebHookKotlinDslAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull TeamCityCoreFacade teamCityCoreFacade,
								  @NotNull ServerSettings serverSettings,
								  @NotNull DslDiagnosticsService dslDiagnosticsService
								  ) {
		super(pagePlaces);
		this.myDslDiagnosticsService = dslDiagnosticsService;
		this.myTeamCityCoreFacade = teamCityCoreFacade;
		this.myServerSettings = serverSettings;

		setPluginName(ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHook/kotlinDslAdminTab.jsp"));
		addCssFile(descriptor.getPluginResourcesPath("WebHook/css/styles.css"));
		addJsFile(descriptor.getPluginResourcesPath("WebHook/3rd-party/highlight/highlight.min.js"));
		setTabTitle("WebHooks Kotlin DSL");
		setPosition(PositionConstraint.after("plugins", "tcWebHooks"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
	}

	@NotNull
	public String getGroup() {
		return SERVER_RELATED_GROUP;
	}
	
	@Override
	public void fillModel(Map<String, Object> model, HttpServletRequest request) {
		
		DslDiagnosticsResult dslDiagnosticsResult;
		try {
			dslDiagnosticsResult = this.myDslDiagnosticsService.getDiagnostics();
			model.put("dslDiagnosticsResult", dslDiagnosticsResult);
		} catch (DslDiagnotisticsException e) {
			model.put("dslDiagnosticsResultError", e.getMessage());
		}
		
		long countOfActiveKotlinProjects = this.myTeamCityCoreFacade.getActiveProjects().stream()
				.filter(p -> this.myTeamCityCoreFacade.getProjectVcsStatus(p).isKotlin()).count();
		
		model.put("countOfActiveKotlinProjects", countOfActiveKotlinProjects);
		model.put("rootUrl", this.myServerSettings.getRootUrl());
	}
	
}