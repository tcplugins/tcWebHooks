package webhook.teamcity.extension.project;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.extension.bean.ProjectParametersBean;
import webhook.teamcity.extension.bean.ProjectTemplatesBean;
import webhook.teamcity.extension.bean.ProjectWebHookParameterBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanGsonSerialiser;
import webhook.teamcity.extension.bean.RegisteredWebhookAuthenticationTypesBean;
import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean.SimpleTemplate;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

public class WebHookProjectSettingsTab extends EditProjectTab {
	private static final String TAB_TITLE = "WebHooks & Templates";
	private final WebHookSettingsManager myWebhookSettingsManager;
	private final WebHookPayloadManager myPayloadManager;
	private final WebHookTemplateManager myWebHookTemplateManager;
	private final WebHookTemplateResolver myTemplateResolver;
	private final WebHookAuthenticatorProvider myAuthenticatorProvider;


	private final WebHookParameterStore myWebHookParameterStore;
	private final ProjectManager myProjectManager;
	String myPluginPath;
	private final WebHookPluginDataResolver myWebHookPluginDataResolver;

	public WebHookProjectSettingsTab(@NotNull PagePlaces pagePlaces,
									@NotNull WebHookSettingsManager settings,
									@NotNull PluginDescriptor pluginDescriptor,
									@NotNull WebHookPayloadManager payloadManager,
									@NotNull WebHookTemplateManager webHookTemplateManager,
									@NotNull WebHookTemplateResolver templateResolver,
									@NotNull WebHookParameterStoreFactory webHookParameterStoreFactory,
									@NotNull WebHookAuthenticatorProvider authenticatorProvider,
									@NotNull ProjectManager projectManager,
									@NotNull WebHookPluginDataResolver webHookPluginDataResolver) {
		super(pagePlaces, pluginDescriptor.getPluginName(), "WebHook/webHookProjectSettingsTab.jsp", TAB_TITLE);
		this.myWebhookSettingsManager = settings;
		this.myPayloadManager = payloadManager;
		this.myTemplateResolver = templateResolver;
		this.myWebHookTemplateManager = webHookTemplateManager;
		this.myWebHookParameterStore = webHookParameterStoreFactory.getWebHookParameterStore();
		this.myAuthenticatorProvider = authenticatorProvider;
		this.myProjectManager = projectManager;
		this.myWebHookPluginDataResolver = webHookPluginDataResolver;
		addCssFile(pluginDescriptor.getPluginResourcesPath("WebHook/css/styles.css"));
		addJsFile(pluginDescriptor.getPluginResourcesPath("WebHook/3rd-party/jquery.color.js"));
		addJsFile(pluginDescriptor.getPluginResourcesPath("WebHook/3rd-party/jquery.easytabs.min.js"));
		addJsFile(pluginDescriptor.getPluginResourcesPath("WebHook/js/editWebhookCommon.js"));
		addJsFile(pluginDescriptor.getPluginResourcesPath("WebHook/js/editWebhookParameter.js"));
		addJsFile(pluginDescriptor.getPluginResourcesPath("WebHook/js/editWebhookConfiguration.js"));

	}

	@NotNull
	@Override
	public String getTabTitle(@NotNull final HttpServletRequest request) {
		final SProject currentProject = getProject(request);
		if (currentProject == null) {
			return TAB_TITLE;
		}
		ProjectWebHooksBean config = ProjectWebHooksBean.buildWithoutNew(
				this.myWebhookSettingsManager.getSettings(currentProject.getProjectId()),
				currentProject,
				myPayloadManager.getRegisteredFormatsAsCollection(),
				myTemplateResolver.findWebHookTemplatesForProject(currentProject)
			);
		final int count = config.getWebHookList().size();
		if (count == 0) {
			return TAB_TITLE;
		}
		return TAB_TITLE + " (" + count + ")";
	}

	@Override
	public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
		final SProject currentProject = getProject(request);
		if (currentProject == null) {
			return;
		}


		List<ProjectWebHooksAndTemplatesBean> projectWebHooksAndTemplates = new ArrayList<>();
		List<SProject> parentProjects = currentProject.getProjectPath();

		List<ProjectWebHooksBean> parentProjectBeans = new ArrayList<>();
		ProjectWebHooksBean projectBean = null;
		Collection<SimpleTemplate> projectTemplatesBean = null;
		Map<String, ProjectWebHookParameterBean> projectParameters = new TreeMap<>();

		Map<String, List<WebHookPayloadTemplate>> templates = myWebHookTemplateManager.getRegisteredTemplatesForProjects(
				parentProjects.stream().map(SProject::getProjectId).collect(Collectors.toList())
			);

		model.put("permissionError", "");
		for (SProject projectParent : parentProjects){
			Loggers.SERVER.debug("WebHookProjectSettingsTab: Assembling webhooks for project: " + projectParent.getName());
			if (currentProject.getProjectId().equals(projectParent.getProjectId())) {

				projectBean = ProjectWebHooksBean.buildWithoutNew(
						this.myWebhookSettingsManager.getSettings(projectParent.getProjectId()),
						currentProject,
						myPayloadManager.getRegisteredFormatsAsCollection(),
						myTemplateResolver.findWebHookTemplatesForProject(currentProject)
					);

				projectTemplatesBean = RegisteredWebHookTemplateBean.build(
						myWebHookTemplateManager,
						templates.getOrDefault(projectParent.getProjectId(), Collections.emptyList()),
						myPayloadManager.getRegisteredFormats(),
						myWebhookSettingsManager,
						myProjectManager)
					.getTemplateList();
			} else {
				projectWebHooksAndTemplates.add(
						new ProjectWebHooksAndTemplatesBean(
						ProjectWebHooksBean.buildWithoutNew(
							this.myWebhookSettingsManager.getSettings(projectParent.getProjectId()),
							projectParent,
							myPayloadManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(projectParent)
							),
						ProjectTemplatesBean.newInstance(projectParent, templates.get(projectParent.getProjectId())), 
						ProjectParametersBean.newInstance(projectParent, myWebHookParameterStore.getOwnWebHookParameters(projectParent))
						)
					);
			}
			// Just put any new params in. The more specific project ones will overwrite parent project ones.
			myWebHookParameterStore.getOwnWebHookParameters(projectParent).forEach(param -> {
				projectParameters.put(param.getName(), new ProjectWebHookParameterBean(projectParent, param));
			});
		}

		model.put("projectWebHooksAndTemplates", projectWebHooksAndTemplates);
		model.put("buildList", currentProject.getBuildTypes());
		model.put("parentProjectBeans", parentProjectBeans);
		model.put("projectBean", projectBean);
		model.put("webHookTemplates", projectTemplatesBean);
		model.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(currentProject),
				myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myProjectManager).getTemplateList());
		model.put("projectWebhookParameters", projectParameters.values());
		model.put("project", currentProject);

		model.put("projectId", currentProject.getProjectId());
		model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(currentProject));
		model.put("externalId", TeamCityIdResolver.getExternalProjectId(currentProject));
		model.put("projectName", currentProject.getName());
		model.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());
		
		model.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
				TemplatesAndProjectWebHooksBean.build(
						RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(currentProject),
															myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myProjectManager), 
						null,
						//ProjectHistoryResolver.getProjectHistory(project),
						RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
						)
					)
				);

	}

	@Getter @AllArgsConstructor
	public static class ProjectWebHooksAndTemplatesBean {
		ProjectWebHooksBean webhooks;
		ProjectTemplatesBean templates;
		ProjectParametersBean parameters;
	}
}
