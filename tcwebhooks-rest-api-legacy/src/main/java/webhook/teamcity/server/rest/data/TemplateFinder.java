package webhook.teamcity.server.rest.data;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.util.StringUtil;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

public class TemplateFinder {

	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final ProjectIdResolver myProjectIdResolver;
	
	
	public TemplateFinder(@NotNull final WebHookTemplateManager templateManager, @NotNull final ProjectIdResolver projectIdResolver){
		myTemplateManager = templateManager;
		myProjectIdResolver = projectIdResolver;
	}
	
	public static String getLocator(final WebHookTemplateConfig template) {
	    return Locator.createEmptyLocator().setDimension("id", template.getId()).getStringRepresentation();
	}
	
	public static String getProjectLocator(final String projectId) {
		return Locator.createEmptyLocator().setDimension("project", projectId).getStringRepresentation();
	}

	public static String getTemplateTextLocator(final String id){
		return Locator.createEmptyLocator().setDimension("id", id).getStringRepresentation();
	}
	
	public WebHookTemplateConfigWrapper findTemplateById(String templateLocator) {

		if (StringUtil.isEmpty(templateLocator)) {
			throw new BadRequestException("Empty template locator is not supported.");
		}

		final Locator locator = new Locator(templateLocator, "id", "name", "type",
				Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);

		if (locator.isSingleValue()) {
			// no dimensions found, assume it's a name or internal id or
			// external id
			WebHookTemplateConfig template = null;
			@NotNull
			final String singleValue = locator.getSingleValue();
			template = myTemplateManager.getTemplateConfig(singleValue, TemplateState.BEST);
			if (template != null) {
				return new WebHookTemplateConfigWrapper(
						template, 
						myProjectIdResolver.getExternalProjectId(template.getProjectInternalId()),
						myTemplateManager.getTemplateState(template.getId(), TemplateState.BEST), 
						WebHookTemplateStates.build(template)
					);
			}
			throw new NotFoundException(
					"No template found by id '"
							+ singleValue + "'.");
			
		} else if (locator.getSingleDimensionValue("id") != null){
			WebHookTemplateConfig template = null;
			@NotNull final String templateId = locator.getSingleDimensionValue("id");
			@NotNull final TemplateState templateState = getTemplateStateDimension(locator);
			template = myTemplateManager.getTemplateConfig(templateId, templateState);
			if (template != null) {
				return new WebHookTemplateConfigWrapper(
						template, 
						myProjectIdResolver.getExternalProjectId(template.getProjectInternalId()),
						myTemplateManager.getTemplateState(template.getId(), templateState),
						WebHookTemplateStates.build(template)
					);
			}
			throw new NotFoundException(
					"No template found by id '"
							+ templateId + "' and state '"+ templateState.toString() +"'.");
			
		} else if (locator.getSingleDimensionValue("name") != null){
			WebHookTemplateConfig template = null;
			@NotNull final String templateName = locator.getSingleDimensionValue("name");
			@NotNull final TemplateState templateState = getTemplateStateDimension(locator);

			template = myTemplateManager.getTemplateConfig(templateName, templateState);
			if (template != null) {
				return new WebHookTemplateConfigWrapper(
						template, 
						myProjectIdResolver.getExternalProjectId(template.getProjectInternalId()),
						myTemplateManager.getTemplateState(template.getId(), templateState),
						WebHookTemplateStates.build(template)
					);
			}
			throw new NotFoundException(
					"No template found by name '"
							+ templateName + "' and state '"+ templateState.toString() +"'.");
			
		}
		
		throw new BadRequestException("Sorry: Searching for multiple template is not supported.");

	}
	
	private TemplateState getTemplateStateDimension(Locator locator) {
		if (locator.getSingleDimensionValue("status") != null) {
			return TemplateState.valueOf(locator.getSingleDimensionValue("status"));
		}
		return TemplateState.BEST;
	}

	public WebHookTemplateItemConfigWrapper findTemplateByIdAndTemplateContentById(String templateLocator, String templateContentLocator) {
		
		WebHookTemplateConfigWrapper templateConfigWrapper =  findTemplateById(templateLocator);
		
		if (StringUtil.isEmpty(templateLocator)) {
			throw new BadRequestException("Empty template locator is not supported.");
		}

		final Locator locator = new Locator(templateContentLocator, "id", "name",
				Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);

		if (locator.isSingleValue()) {
			// no dimensions found, assume it's a name or id without "id:"
			@NotNull
			final String templateId = locator.getSingleValue();
			return buildWebHookTemplateItemConfigWrapper(templateConfigWrapper, templateId);
		} else if (locator.getSingleDimensionValue("id") != null){
			@NotNull
			final String templateId = locator.getSingleDimensionValue("id");
			return buildWebHookTemplateItemConfigWrapper(templateConfigWrapper, templateId);
		} else {
			throw new BadRequestException("Sorry: Searching for multiple templates is not supported.");
		}
		
	}

	private WebHookTemplateItemConfigWrapper buildWebHookTemplateItemConfigWrapper(
			WebHookTemplateConfigWrapper templateConfigWrapper, final String templateId) {
		if ("defaultTemplate".equals(templateId)){
			if (templateConfigWrapper.getTemplateConfig().getDefaultTemplate() != null) {
				WebHookTemplateConfig config = templateConfigWrapper.getTemplateConfig();
				WebHookTemplateItemRest defaultTemplateItem = new WebHookTemplateItemRest(
																	config.getDefaultTemplate(), 
																	config.getDefaultBranchTemplate(), 
																	templateId, 
																	null
																);
				return new WebHookTemplateItemConfigWrapper(templateConfigWrapper.getExternalProjectId(), defaultTemplateItem, templateConfigWrapper.getBuildStatesWithTemplate());
			} else {
				throw new NotFoundException(
						"This template does not have a default template '"
								+ templateId + "'.");
			}
		}
		
		if ("_new".equals(templateId)){
			WebHookTemplateItem template = new WebHookTemplateItem();
			template.setTemplateText(new WebHookTemplateText(""));
			template.setBranchTemplateText(new WebHookTemplateBranchText(""));
			template.setId(templateConfigWrapper.getTemplateConfig().getTemplates().getMaxId());
			return new WebHookTemplateItemConfigWrapper(template, templateConfigWrapper.getBuildStatesWithTemplate());
		}
		
		
		for (WebHookTemplateItem template : templateConfigWrapper.getTemplateConfig().getTemplates().getTemplates()){
			if (template.getId().intValue() == Integer.valueOf(templateId)){
				return new WebHookTemplateItemConfigWrapper(template, templateConfigWrapper.getBuildStatesWithTemplate());
			}
		}
		throw new NotFoundException(
				"No templateItem found by id '"
						+ templateId + "'.");
	}

}
