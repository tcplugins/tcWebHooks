package webhook.teamcity.server.rest.data;


import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.StringUtil;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameters;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

public class WebHookParameterFinder {
	
	@NotNull private final ProjectManager projectManager;
	@NotNull private final WebHookParameterStore myWebHookParameterStore;
	
	public WebHookParameterFinder(
			@NotNull final ProjectManager projectManager,
			@NotNull final WebHookParameterStoreFactory webHookParameterStoreFactory)
	{
		this.projectManager = projectManager;
		this.myWebHookParameterStore = webHookParameterStoreFactory.getWebHookParameterStore();
	}

	public static String getLocator(final WebHookParameter webhookParameter) {
		return Locator.createEmptyLocator().setDimension("id", webhookParameter.getId()).getStringRepresentation();
	}

	public ProjectWebhookParameters getAllWebHookParameters(SProject sProject, PagerData pagerData, Fields fields, BeanContext myBeanContext) {
		return new ProjectWebhookParameters(
				myWebHookParameterStore.getAllWebHookParameters(sProject),
				sProject.getExternalId(), 
				pagerData, 
				fields, 
				myBeanContext);
	}
	
	public ProjectWebhookParameter findWebhookParameter(SProject sProject, String paramLocator, Fields fields, BeanContext myBeanContext) {
		
		if (StringUtil.isEmpty(paramLocator)) {
			throw new BadRequestException("Empty parameter locator is not supported.");
		}

		final Locator locator = new Locator(paramLocator, "id", "name",
				Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);

		if (locator.isSingleValue()) {
			// no dimensions found, assume it's an id
			WebHookParameter param = null;
			@NotNull
			final String singleValue = locator.getSingleValue();
			param = findWebhookParameterById(sProject, singleValue);
			if (param != null) {
				return new ProjectWebhookParameter(
						param, 
						sProject.getExternalId(),
						fields,
						myBeanContext
					);
			}
			throw new NotFoundException(
					"No parameter found by id '"
							+ singleValue + "'.");
			
		} else if (locator.getSingleDimensionValue("id") != null) {
			WebHookParameter param = null;
			@NotNull final String singleValue = locator.getSingleDimensionValue("id");
			param = findWebhookParameterById(sProject, singleValue);
			if (param != null) {
				return new ProjectWebhookParameter(
						param, 
						sProject.getExternalId(),
						fields,
						myBeanContext
					);
			}
			throw new NotFoundException(
					"No parameter found by id '"
							+ singleValue + "'.");
		} else if (locator.getSingleDimensionValue("name") != null){
			WebHookParameter param = null;
			@NotNull final String singleValue = locator.getSingleDimensionValue("name");
			param = findWebhookParameter(sProject, singleValue);
			if (param != null) {
				return new ProjectWebhookParameter(
						param, 
						sProject.getExternalId(),
						fields,
						myBeanContext
					);
			}
			throw new NotFoundException(
					"No parameter found with name '"
							+ singleValue + "'.");
		}
		
		throw new BadRequestException("Sorry: This parameters query is not supported.");
	}
	
	public WebHookParameter findWebhookParameter(SProject sProject, String name) {
		return myWebHookParameterStore.findWebHookParameter(sProject, name);
	}

	public WebHookParameter findWebhookParameterById(SProject sProject, String parameterId) {
		return myWebHookParameterStore.getWebHookParameterById(sProject, parameterId);
	}
}
