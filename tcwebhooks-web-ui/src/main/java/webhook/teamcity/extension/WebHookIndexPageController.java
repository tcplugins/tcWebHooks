package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.Constants;
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
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookIndexPageController extends BaseController {
		private static final Logger LOG = Logger.getInstance(WebHookIndexPageController.class.getName());

		private final WebControllerManager myWebManager;
		private final WebHookMainSettings myMainSettings;
		private final WebHookSettingsManager myWebhookSettingsManager;
		private final PluginDescriptor myPluginDescriptor;
		private final WebHookPayloadManager myPayloadManager;
		private final WebHookTemplateResolver myTemplateResolver;
		private final WebHookAuthenticatorProvider myAuthenticatorProvider;
		private final WebHookParameterStore myWebHookParameterStore;
		private final WebHookPluginDataResolver myWebHookPluginDataResolver;
		private final WebHookTemplateManager myWebHookTemplateManager;
		private final ProjectManager myProjectManager;


		public WebHookIndexPageController(
				SBuildServer server,
				WebControllerManager webManager,
				WebHookSettingsManager settings,
				PluginDescriptor pluginDescriptor,
				WebHookPayloadManager manager,
				WebHookTemplateResolver templateResolver,
				WebHookMainSettings configSettings,
				WebHookAuthenticatorProvider authenticatorProvider,
				WebHookParameterStoreFactory webHookParameterStoreFactory,
				WebHookPluginDataResolver webHookPluginDataResolver,
				WebHookTemplateManager webHookTemplateManager,
				ProjectManager projectManager
			) {
			super(server);
			myWebManager = webManager;
			myWebhookSettingsManager = settings;
			myPluginDescriptor = pluginDescriptor;
			myMainSettings = configSettings;
			myPayloadManager = manager;
			myTemplateResolver = templateResolver;
			myAuthenticatorProvider = authenticatorProvider;
			myWebHookParameterStore = webHookParameterStoreFactory.getWebHookParameterStore();
			myWebHookPluginDataResolver = webHookPluginDataResolver;
			myWebHookTemplateManager = webHookTemplateManager;
			myProjectManager = projectManager;

		}

		public void register(){
		  myWebManager.registerController("/webhooks/index.html", this);
		}

		@Nullable
		protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

			HashMap<String,Object> params = new HashMap<>();
			params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
			params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
			params.put("rootContext", myServer.getServerRootPath());
			params.put("pluginVersion", myPluginDescriptor.getPluginVersion());

			if (myMainSettings.getInfoUrl() != null && myMainSettings.getInfoUrl().length() > 0){
				params.put("moreInfoText", "<li><a href=\"" + myMainSettings.getInfoUrl() + "\">" + myMainSettings.getInfoText() + "</a></li>");
				if (myMainSettings.getWebhookShowFurtherReading()){
					params.put("ShowFurtherReading", "ALL");
				} else {
					params.put("ShowFurtherReading", "SINGLE");
				}
			} else if (myMainSettings.getWebhookShowFurtherReading()){
				params.put("ShowFurtherReading", "DEFAULT");
			} else {
				params.put("ShowFurtherReading", "NONE");
			}

			if(request.getParameter("projectId") != null){

				SProject currentProject = TeamCityIdResolver.findProjectById(this.myServer.getProjectManager(), request.getParameter("projectId"));
				if (currentProject != null){
					params.put("haveProject", "true");

					SUser myUser = SessionUser.getUser(request);
					params.put("hasPermission", myUser.isPermissionGrantedForProject(currentProject.getProjectId(), Permission.EDIT_PROJECT));

					List<ProjectWebHooksAndTemplatesBean> projectWebHooksAndTemplates = new ArrayList<>();
					List<SProject> parentProjects = currentProject.getProjectPath();

					List<ProjectWebHooksBean> parentProjectBeans = new ArrayList<>();
					ProjectWebHooksBean projectBean = null;
					Collection<SimpleTemplate> projectTemplatesBean = null;
					Map<String, ProjectWebHookParameterBean> projectParameters = new TreeMap<>();

					Map<String, List<WebHookPayloadTemplate>> templates = myWebHookTemplateManager.getRegisteredTemplatesForProjects(
							parentProjects.stream().map(SProject::getProjectId).collect(Collectors.toList())
						);

					params.put("permissionError", "");
					for (SProject projectParent : parentProjects){
						LOG.debug("WebHookProjectSettingsTab: Assembling webhooks for project: " + projectParent.getName());
						if (currentProject.getProjectId().equals(projectParent.getProjectId())) {

							projectBean =  ProjectWebHooksBean.buildWithoutNew(this.myWebhookSettingsManager.getWebHooksForProject(currentProject), 
									currentProject,
									myPayloadManager.getRegisteredFormatsAsCollection(),
									myTemplateResolver.findWebHookTemplatesForProject(currentProject),
									myWebhookSettingsManager.iswebHooksEnabledForProject(currentProject.getProjectId())
								);

							projectTemplatesBean = RegisteredWebHookTemplateBean.build(
									myWebHookTemplateManager,
									templates.getOrDefault(projectParent.getProjectId(), Collections.emptyList()),
									myPayloadManager.getRegisteredFormats(),
									myWebhookSettingsManager,
									myProjectManager)
								.getTemplateList();
							
							myWebHookParameterStore.getOwnWebHookParameters(projectParent).forEach(param -> {
								projectParameters.put(param.getName(), new ProjectWebHookParameterBean(projectParent, param));
							});
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
					}

					params.put("projectWebHooksAndTemplates", projectWebHooksAndTemplates);
					params.put("buildList", currentProject.getOwnBuildTypes());
					params.put("parentProjectBeans", parentProjectBeans);
					params.put("projectBean", projectBean);
					params.put("webHookTemplates", projectTemplatesBean);
					params.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(currentProject),
							myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myProjectManager).getTemplateList());
					params.put("projectWebhookParameters", projectParameters.values());
					params.put("project", currentProject);

					params.put("projectId", currentProject.getProjectId());
					params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(currentProject));
					params.put("externalId", TeamCityIdResolver.getExternalProjectId(currentProject));
					params.put("projectName", currentProject.getName());
					params.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());

					params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
							TemplatesAndProjectWebHooksBean.build(
									RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(currentProject),
																		myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myProjectManager),
									null,
									//ProjectHistoryResolver.getProjectHistory(project),
									RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
									)
								)
							);

				} else {
					params.put("haveProject", "false");
					params.put("errorReason", "The project requested does not appear to be valid.");
				}
			} else if (request.getParameter("buildTypeId") != null){
				SBuildType sBuildType = TeamCityIdResolver.findBuildTypeById(this.myServer.getProjectManager(), request.getParameter("buildTypeId"));
				if (sBuildType != null){
					SProject project = sBuildType.getProject();

					WebHookProjectSettings projSettings = myWebhookSettingsManager.getSettings(project.getProjectId());

					SUser myUser = SessionUser.getUser(request);
					params.put("hasPermission", myUser.isPermissionGrantedForProject(project.getProjectId(), Permission.EDIT_PROJECT));

					ProjectWebHooksBean bean = ProjectWebHooksBean.buildWithoutNew(projSettings, sBuildType,
							project,
							myPayloadManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(project)
							);

					params.put("webHookList", bean);
					params.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
							myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myServer.getProjectManager()).getTemplateList());
					params.put("webHooksDisabled", !projSettings.isEnabled());
					params.put("projectId", project.getProjectId());
					params.put("haveProject", "true");
					params.put("projectName", getProjectName(TeamCityIdResolver.getExternalProjectId(project), project.getName()));
					params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
					params.put("haveBuild", "true");
					params.put("buildName", sBuildType.getName());
					params.put("buildExternalId", TeamCityIdResolver.getExternalBuildId(sBuildType));
					params.put("buildTypeList", project.getBuildTypes());
					params.put("noWebHooks", bean.getWebHookList().isEmpty());
					params.put("webHooks", ! bean.getWebHookList().isEmpty());

					params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
							TemplatesAndProjectWebHooksBean.build(
									RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																		myPayloadManager.getRegisteredFormats(), myWebhookSettingsManager, myServer.getProjectManager()),
									ProjectWebHooksBean.build(projSettings, sBuildType, project, myPayloadManager.getRegisteredFormatsAsCollection(),
																myTemplateResolver.findWebHookTemplatesForProject(project)
																),
									//ProjectHistoryResolver.getBuildHistory(sBuildType),
									RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
									)
								)
							);
				} else {
					params.put("haveProject", "false");
					params.put("errorReason", "The build requested does not appear to be valid.");
				}
			} else {
				params.put("haveProject", "false");
				params.put("errorReason", "No project specified.");
			}
			params.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());

			return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/webhookEdit.jsp", params);
		}

		private String getProjectName(String externalProjectId, String name) {
			if (externalProjectId.equalsIgnoreCase(Constants.ROOT_PROJECT_ID)){
				return externalProjectId;
			}
			return name;
		}

		@Getter @AllArgsConstructor
		public static class ProjectWebHooksAndTemplatesBean {
			ProjectWebHooksBean webhooks;
			ProjectTemplatesBean templates;
			ProjectParametersBean parameters;
		}
}
