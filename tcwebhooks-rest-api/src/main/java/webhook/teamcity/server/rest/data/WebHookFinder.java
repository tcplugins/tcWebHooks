package webhook.teamcity.server.rest.data;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.util.StringUtil;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookFinder {

	@NotNull private final ProjectManager projectManager;
	@NotNull private final ProjectSettingsManager projectSettingsManager;
	
	public WebHookFinder(
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectSettingsManager projectSettingsManager)
	{
		this.projectManager = projectManager;
		this.projectSettingsManager = projectSettingsManager;
	}
	
	public static String getLocator(final WebHookConfig webhook) {
		return Locator.createEmptyLocator().setDimension("id", webhook.getUniqueKey()).getStringRepresentation();
	}

	public WebHookProjectSettings getWebHookProjectSettings(String projectExternalId) {
		SProject sProject = this.projectManager.findProjectByExternalId(projectExternalId);
		if (sProject == null) {
			throw new NotFoundException("No project found with that project id");
		}
		return (WebHookProjectSettings) projectSettingsManager.getSettings(sProject.getProjectId(), WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);
	}
	
	public ProjectWebhook findWebHookById(String projectExternalId, String webHookLocator, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {

		if (StringUtil.isEmpty(webHookLocator)) {
			throw new BadRequestException("Empty webhook locator is not supported.");
		}

		final Locator locator = new Locator(webHookLocator, "id",
				Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);

		if (locator.isSingleValue()) {
			// no dimensions found, assume it's a name or internal id or
			// external id
			@NotNull final String singleValue = locator.getSingleValue();		
			return getWebHookConfigById(projectExternalId, fields, beanContext, singleValue);
		} else if (locator.getSingleDimensionValue("id") != null){
			@NotNull final String webHookId = locator.getSingleDimensionValue("id");			
			return getWebHookConfigById(projectExternalId, fields, beanContext, webHookId);
		}
		
		throw new BadRequestException("Sorry: Searching for multiple template is not supported.");		
	}

	private ProjectWebhook getWebHookConfigById(String projectExternalId, final Fields fields, final BeanContext beanContext,
			final String singleValue) {
		for (WebHookConfig webHookConfig : getWebHookProjectSettings(projectExternalId).getWebHooksConfigs()) {
			
			if (singleValue.equals(webHookConfig.getUniqueKey())) {
				return new ProjectWebhook(webHookConfig, projectExternalId, fields, beanContext);
			}
		}
		throw new NotFoundException("Could not find a webhook with that id");
	}
}
