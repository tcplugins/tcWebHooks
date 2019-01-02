package webhook.teamcity.extension.bean;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import lombok.Getter;
import webhook.teamcity.BuildState;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookProjectSettings;


public class ProjectWebHooksBean {
	@Getter private String projectId;
	@Getter private String externalProjectId;
	@Getter private String 	sensibleProjectName;
	@Getter private String 	sensibleProjectFullName;
	@Getter private boolean webHooksEnabledForProject;
	private Map<String, WebhookConfigAndBuildTypeListHolder> webHookList;

	public Collection<WebhookConfigAndBuildTypeListHolder> getWebHookList(){
		return webHookList.values();
	}

	protected ProjectWebHooksBean(SProject project, boolean projectWebHooksEnabled) {
		projectId = TeamCityIdResolver.getInternalProjectId(project);
		externalProjectId = TeamCityIdResolver.getExternalProjectId(project);
		webHookList = new LinkedHashMap<>();
		sensibleProjectName = getSensibleProjectName(project);
		sensibleProjectFullName = getSensibleProjectFullName(project);
		webHooksEnabledForProject = projectWebHooksEnabled;
	}

	public static ProjectWebHooksBean buildWithoutNew(WebHookProjectSettings projSettings, SProject project, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList){
		ProjectWebHooksBean bean = new ProjectWebHooksBean(project, projSettings.isEnabled());
		List<SBuildType> projectBuildTypes = TeamCityIdResolver.getOwnBuildTypes(project);

		/* Iterate over the rest of the webhooks in this project and add them to the json config */
		for (WebHookConfig config : projSettings.getWebHooksAsList()){
			addWebHookConfigHolder(bean, projectBuildTypes, config, registeredPayloads, templateList);
		}

		return bean;

	}

	public static ProjectWebHooksBean build(WebHookProjectSettings projSettings, SProject project, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList){
		ProjectWebHooksBean bean = new ProjectWebHooksBean(project, projSettings.isEnabled());
		List<SBuildType> projectBuildTypes = TeamCityIdResolver.getOwnBuildTypes(project);

		/* Create a "new" config with blank stuff so that clicking the "new" button has a bunch of defaults to load in */
		WebHookConfig newBlankConfig = new WebHookConfig(project.getProjectId(), project.getExternalId(), "", true, new BuildState().setAllEnabled(), null, true, true, null, null);
		newBlankConfig.setUniqueKey("new");
		/* And add it to the list */
		addWebHookConfigHolder(bean, projectBuildTypes, newBlankConfig, registeredPayloads, templateList);

		/* Iterate over the rest of the webhooks in this project and add them to the json config */
		for (WebHookConfig config : projSettings.getWebHooksAsList()){
			addWebHookConfigHolder(bean, projectBuildTypes, config, registeredPayloads, templateList);
		}

		return bean;

	}

	public static ProjectWebHooksBean build(WebHookProjectSettings projSettings, SBuildType sBuildType, SProject project, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList){
		ProjectWebHooksBean bean = new ProjectWebHooksBean(project, projSettings.isEnabled());
		List<SBuildType> projectBuildTypes = TeamCityIdResolver.getOwnBuildTypes(project);
		Set<String> enabledBuildTypes = new HashSet<>();
		enabledBuildTypes.add(sBuildType.getBuildTypeId());

		/* Create a "new" config with blank stuff so that clicking the "new" button has a bunch of defaults to load in */
		WebHookConfig newBlankConfig = new WebHookConfig(project.getProjectId(), project.getExternalId(), "", true, new BuildState().setAllEnabled(), null, false, false, enabledBuildTypes, null);
		newBlankConfig.setUniqueKey("new");
		/* And add it to the list */
		addWebHookConfigHolder(bean, projectBuildTypes, newBlankConfig, registeredPayloads, templateList);

		/* Iterate over the rest of the webhooks in this project and add them to the json config */
		for (WebHookConfig config : projSettings.getBuildWebHooksAsList(sBuildType)){
			addWebHookConfigHolder(bean, projectBuildTypes, config, registeredPayloads, templateList);
		}

		return bean;

	}

	/** A builder which takes a {@link WebHookProjectSettings} object.
	 * Does not create a place holder "new" webhook config.
	 *
	 * @param projSettings : A {@link WebHookProjectSettings} containing a list of wenhooks, etc.
	 * @param sBuildType : The build type these webhooks belong to.
	 * @param project : The TeamCity project.
	 * @param registeredPayloads : A collection of {@link WebHookPayload}s.
	 * @param templateList : A list of {@link WebHookPayloadTemplate}s.
	 * @return
	 */
	public static ProjectWebHooksBean buildWithoutNew(WebHookProjectSettings projSettings, SBuildType sBuildType, SProject project, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList){
		ProjectWebHooksBean bean = new ProjectWebHooksBean(project, projSettings.isEnabled());
		List<SBuildType> projectBuildTypes = TeamCityIdResolver.getOwnBuildTypes(project);
		Set<String> enabledBuildTypes = new HashSet<>();
		enabledBuildTypes.add(sBuildType.getBuildTypeId());

		/* Iterate over the rest of the webhooks in this project and add them to the json config */
		for (WebHookConfig config : projSettings.getBuildWebHooksAsList(sBuildType)){
			addWebHookConfigHolder(bean, projectBuildTypes, config, registeredPayloads, templateList);
		}

		return bean;

	}

	/** A builder which takes a list of {@link WebHookConfigEnhanced} rather than the settings object itself.<br>
	 * Does not create a place holder "new" webhook config.
	 *
	 * @param webHookConfigs : A List &lt;WebHookConfigEnhanced&gt;
	 * @param project : A TeamCity project
	 * @param registeredFormats : A collection of {@link WebHookPayload}s.
	 * @param templateList : A list of {@link WebHookPayloadTemplate}s.
	 * @param iswebHooksEnabledForProject : Whether webhooks are enabled at all for this project.
	 * @return a {@link ProjectAndBuildWebhooksBean} which represents a project and its webhooks.
	 */
	public static ProjectWebHooksBean buildWithoutNew(
			List<WebHookConfigEnhanced> webHookConfigs,
			SProject project,
			Collection<WebHookPayload> registeredFormats,
			List<WebHookPayloadTemplate> templateList,
			boolean iswebHooksEnabledForProject)
	{
		ProjectWebHooksBean bean = new ProjectWebHooksBean(project, iswebHooksEnabledForProject);
		List<SBuildType> projectBuildTypes = TeamCityIdResolver.getOwnBuildTypes(project);

		/* Iterate over the rest of the webhooks in this project and add them to the json config */
		for (WebHookConfigEnhanced config : webHookConfigs){
			addWebHookConfigHolder(bean, projectBuildTypes, config, registeredFormats, templateList);
		}

		return bean;
	}


	private static void addWebHookConfigHolder(ProjectWebHooksBean bean,
			List<SBuildType> projectBuildTypes, WebHookConfig config, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList) {
		WebhookConfigAndBuildTypeListHolder holder = new WebhookConfigAndBuildTypeListHolder(config, registeredPayloads, templateList);
		for (SBuildType sBuildType : projectBuildTypes){
			holder.addWebHookBuildType(new WebhookBuildTypeEnabledStatusBean(
													sBuildType.getBuildTypeId(),
													sBuildType.getName(),
													config.isEnabledForBuildType(sBuildType)
													)
										);
		}
		bean.webHookList.put(holder.getUniqueKey(), holder);
	}
	
	private static void addWebHookConfigHolder(ProjectWebHooksBean bean,
			List<SBuildType> projectBuildTypes, WebHookConfigEnhanced config, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList) {
		WebhookConfigAndBuildTypeListHolder holder = new WebhookConfigAndBuildTypeListHolder(config, registeredPayloads, templateList);
		for (SBuildType sBuildType : projectBuildTypes){
			holder.addWebHookBuildType(new WebhookBuildTypeEnabledStatusBean(
					sBuildType.getBuildTypeId(),
					sBuildType.getName(),
					config.getWebHookConfig().isEnabledForBuildType(sBuildType)
					)
					);
		}
		bean.webHookList.put(holder.getUniqueKey(), holder);
	}

	private String getSensibleProjectName(SProject project){
		if (project.getProjectId().equals("_Root")) {
			return project.getProjectId();
		}
		return project.getName();
	}

	private String getSensibleProjectFullName(SProject project){
		if (project.getProjectId().equals("_Root")) {
			return project.getProjectId();
		}
		return project.getFullName();
	}

}
