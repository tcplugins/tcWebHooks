package webhook.teamcity.server.rest.data;

import java.util.List;

import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.StringUtil;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.builder.WebHookTemplateEntityBuilder;

import com.intellij.openapi.diagnostic.Logger;

public class TemplateFinder {

	private static final Logger LOG = Logger.getInstance(TemplateFinder.class.getName());
	
	@NotNull private final WebHookTemplateManager myTemplateManager;
	
	public TemplateFinder(@NotNull final WebHookTemplateManager templateManager){
		myTemplateManager = templateManager;
	}
	
	public static String getLocator(final WebHookTemplateEntity template) {
	    return Locator.createEmptyLocator().setDimension("id", template.getName()).getStringRepresentation();
	}

	
	
	
	public WebHookTemplateEntity findTemplateById(String templateLocator) {

		if (StringUtil.isEmpty(templateLocator)) {
			throw new BadRequestException("Empty template locator is not supported.");
		}

		final Locator locator = new Locator(templateLocator, "id", "name",
				Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);

		if (locator.isSingleValue()) {
			// no dimensions found, assume it's a name or internal id or
			// external id
			WebHookTemplateEntity template = null;
			@NotNull
			final String singleValue = locator.getSingleValue();
			template = myTemplateManager.getTemplateEntity(singleValue);
			if (template != null) {
				return template;
			}
			throw new NotFoundException(
					"No template found by name '"
							+ singleValue + "'.");
			
		} else if (locator.getSingleDimensionValue("id") != null){
			WebHookTemplateEntity template = null;
			@NotNull
			final String templateId = locator.getSingleDimensionValue("id");
			template = myTemplateManager.getTemplateEntity(templateId);
			if (template != null) {
				return template;
			}
			throw new NotFoundException(
					"No template found by id '"
							+ templateId + "'.");
			
		} else if (locator.getSingleDimensionValue("name") != null){
			WebHookTemplateEntity template = null;
			@NotNull
			final String templateName = locator.getSingleDimensionValue("name");
			template = myTemplateManager.getTemplateEntity(templateName);
			if (template != null) {
				return template;
			}
			throw new NotFoundException(
					"No template found by name '"
							+ templateName + "'.");			
			
			//TODO: Add support for returning more than one template.
			
/*			final List<WebHookTemplate> projectsByName = findProjectsByName(null,
					singleValue);
			if (projectsByName.size() == 1) {
				project = projectsByName.get(0);
				if (project != null) {
					return project;
				}
			}
			project = myProjectManager.findProjectById(singleValue);
			if (project != null) {
				return project;
			}*/
/*			throw new NotFoundException(
					"No template found by name id '"
							+ singleValue + "'.");
*/		}
		
		throw new BadRequestException("Sorry: Searching for multiple template is not supported.");

	}
}
