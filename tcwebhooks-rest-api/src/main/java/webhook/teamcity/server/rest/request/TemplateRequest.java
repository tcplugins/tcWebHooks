package webhook.teamcity.server.rest.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jetbrains.annotations.NotNull;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.errors.OperationException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.ProbableJaxbJarConflictErrorException;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.server.rest.data.TemplateDataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.data.WebHookTemplateStates;
import webhook.teamcity.server.rest.errors.BadRequestException;
import webhook.teamcity.server.rest.errors.JaxbClassCastException;
import webhook.teamcity.server.rest.errors.TemplateInUseException;
import webhook.teamcity.server.rest.errors.TemplatePermissionException;
import webhook.teamcity.server.rest.errors.UnprocessableEntityException;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItems;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

@Path(TemplateRequest.API_TEMPLATES_URL)
public class TemplateRequest {
  private static final String UPDATE_TEMPLATE = "update Template";
private static final String PROJECT_ID = "projectId";
private static final String DEFAULT_TEMPLATE = "defaultTemplate";
  private static final String TEMPLATE_ITEMS = "templateItems";
  private static final String TEMPLATE_ITEM_CONTAINED_INVALID_DATA = "TemplateItem contained invalid data";
  private static final String NO_TEMPLATE_FOUND_BY_THAT_ID = "No template found by that id";
  private static final String TEMPLATE_CONTAINED_INVALID_DATA = "Template contained invalid data";
  private static final String ERROR_SAVING_TEMPLATE = "There was an error saving your template. Sorry.";
  private static final String IT_WAS_NOT_POSSIBLE_TO_PROCESS_YOUR_REQUEST_FOR_TEMPLATE_CONTENT = "Sorry. It was not possible to process your request for template content.";
  private static final Permission PROJECT_TEMPLATE_READ_PERMISSION = Permission.VIEW_PROJECT;
  private static final Permission PROJECT_TEMPLATE_EDIT_PERMISSION = Permission.EDIT_PROJECT;

  @Context @NotNull private TemplateDataProvider myDataProvider;
  @Context @NotNull private WebHookTemplateManager myTemplateManager;
  @Context @NotNull private TemplateValidator myTemplateValidator;

  @Context @NotNull private ServiceLocator myServiceLocator;
  @Context @NotNull private BeanContext myBeanContext;
  @Context @NotNull public PermissionChecker myPermissionChecker;

  public static final String API_TEMPLATES_URL = Constants.API_URL + "/templates";
  protected static final String PARAMETERS = "/parameters";

  @NotNull
  public static String getHref() {
    return API_TEMPLATES_URL;
  }

  @NotNull
  public static String getTemplateHref(WebHookTemplateConfig template) {
    return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template);
  }
	
	@NotNull
	public static String getTemplatesHref() {
		return API_TEMPLATES_URL;
	}
	
  @NotNull
  public static String getDefaultTemplateTextHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + DEFAULT_TEMPLATE + "/templateContent" ;
  }

  @NotNull
  public static String getDefaultBranchTemplateTextHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + DEFAULT_TEMPLATE + "/branchTemplateContent" ;
  }

  @NotNull
  public static String getTemplateDefaultItemHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + DEFAULT_TEMPLATE;
  }

  @NotNull
  public static String getTemplateItemHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId());
  }

  @NotNull
  public static String getTemplateItemTextHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId()) + "/templateContent" ;
  }

  @NotNull
  public static String getTemplateItemBranchTextHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/" + TEMPLATE_ITEMS + "/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId()) +  "/branchTemplateContent" ;
  }

  @NotNull
  public static String getTemplateStateHref(WebHookTemplateConfig template,	String state) {
		return getTemplateDefaultItemHref(template) + "/buildStates/" + state;
  }

  @NotNull
  public static String getTemplateItemStateHref(WebHookTemplateConfig template,	WebHookTemplateItemRest templateItem, String state) {
	  return getTemplateItemHref(template, templateItem) + "/buildStates/" + state;
  }

  @GET
  @Produces({"application/xml", "application/json"})
  public Templates serveTemplates(@QueryParam("fields") String fields) {
	  checkTemplateReadPermission();
	  return new Templates(myDataProvider.getWebHookTemplates(), new PagerData(getHref()), new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{templateLocator}")
  @Produces({"application/xml", "application/json"})
  public Template serveTemplate(@PathParam("templateLocator") String templateLocator, @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  return new Template(wrapper, new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{templateLocator}/export")
  @Produces({"application/json"})
  public Response exportTemplate(@PathParam("templateLocator") String templateLocator, @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  Template template = new Template(wrapper, new Fields(fields), myBeanContext);
	  return Response.ok(template)
			  .header("Content-Disposition", "attachment; filename=\"" + template.getId() + ".json\"")
			  .build();
  }

	private Template buildAndPersistTemplate(Template newTemplate, String updateMode, WebHookTemplateConfig template) {
		template.setTemplateDescription(newTemplate.description);
		template.setProjectInternalId(myDataProvider.getProjectIdResolver().getInternalProjectId(newTemplate.projectId));
	    template.setFormat(newTemplate.format);
		template.setRank(newTemplate.rank);

		if (newTemplate.preferredDateFormat != null) {
			template.setPreferredDateTimeFormat(newTemplate.preferredDateFormat);
		}

		if (newTemplate.toolTip != null) {
			template.setTemplateToolTip(newTemplate.toolTip);
		}

	    if (newTemplate.defaultTemplate != null) {
	    	WebHookTemplateItem templateItemConfig = buildDefaultTemplateItem(newTemplate.defaultTemplate);
	    	template.setDefaultTemplate(templateItemConfig.getTemplateText());
	    	template.setDefaultBranchTemplate(templateItemConfig.getBranchTemplateText());
	    }

	    if (newTemplate.getTemplates() != null && ! newTemplate.getTemplates().isEmpty()) {
	    	WebHookTemplateItems templates = template.getTemplates();
	    	templates.setMaxId(0);
		    for (TemplateItem templateItem : newTemplate.getTemplates()) {
		  	  	WebHookTemplateItem templateItemConfig = buildTemplateItem(templateItem, templates);
		  	  	templates.addTemplateItem(templateItemConfig);
		    }
	    }

	    myTemplateManager.registerTemplateFormatFromXmlConfig(template);

		if (persistAllXmlConfigTemplates(updateMode)){
	    	return new Template(new WebHookTemplateConfigWrapper(template, myDataProvider.getProjectIdResolver().getExternalProjectId(template.getProjectInternalId()), myTemplateManager.getTemplateState(newTemplate.id, TemplateState.BEST), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
	    } else {
	    	throw new OperationException(ERROR_SAVING_TEMPLATE);
	    }
	}

	  @POST
	  @Path("/{projectId}")
	  @Consumes({"application/xml", "application/json"})
	  @Produces({"application/xml", "application/json"})
	  public Template createNewTemplate(@PathParam(PROJECT_ID) String externalProjectId, Template newTemplate) {
		String updateMode = "create Template";
		checkTemplateWritePermission(externalProjectId);
		newTemplate.projectId = externalProjectId;
		ErrorResult validationResult = myTemplateValidator.validateNewTemplate(externalProjectId, newTemplate, new ErrorResult());
		if (validationResult.isErrored()) {
			throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
		}
	    WebHookTemplateConfig template = new WebHookTemplateConfig(newTemplate.id, true);

	    if (myTemplateManager.getTemplate(template.getId()) != null){
	    	validationResult.addError("name", "Template of that name already exists. To update existing template, please use PUT");
	    	throw new BadRequestException("Template name already exists", validationResult);
	    }

	    return buildAndPersistTemplate(newTemplate, updateMode, template);
	  }


	@PUT
	@Path("/{templateLocator}")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	  public Template replaceTemplate(@PathParam("templateLocator") String templateLocator, Template newTemplate) {
		final String updateMode = "replace Template";
		WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		
		  String externalProjectId  = findExternalProjectId(templateLocator, newTemplate.projectId, templateConfigWrapper);

		  checkTemplateWritePermission(externalProjectId);
		  checkTemplateWritePermission(templateConfigWrapper.getExternalProjectId());
		  checkTemplateCanBeRelocated(externalProjectId, templateConfigWrapper);
		  
		if (templateConfigWrapper.getTemplateConfig() == null) {
			throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
		}
		ErrorResult validationResult = myTemplateValidator.validateTemplate(templateConfigWrapper, newTemplate,	new ErrorResult());

		if (!templateConfigWrapper.getTemplateConfig().getId().equals(newTemplate.id)) {
			validationResult.addError("id", "The templateId in the template does not match the templateId in the URL.");
		}
		
		if (validationResult.isErrored()) {
			throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
		}
		WebHookTemplateConfig template = new WebHookTemplateConfig(newTemplate.id, true);

		return buildAndPersistTemplate(newTemplate, updateMode, template);
	}

	  private void checkTemplateCanBeRelocated(String externalProjectId, WebHookTemplateConfigWrapper webHookTemplateConfigWrapper) {
		if (!externalProjectId.equals(webHookTemplateConfigWrapper.getExternalProjectId())) {
			  if (webHookTemplateConfigWrapper.getTemplateConfig() == null){
				  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
			  }
			  if (webHookTemplateConfigWrapper.getStatus().isStateProvided()) {
				  throw new OperationException("You cannot relocate a tcWebHooks provided template.");
			  }
			  if (webHookTemplateConfigWrapper.getStatus().isStateUnknown()) {
				  throw new OperationException("You cannot relocate a tcWebHooks template in an unknown state. Please report this as a bug against the tcPlugins/tcWebHooks project on GitHub.");
			  }
			  int templateUsageCount = myDataProvider.getWebHookFinder().getTemplateUsageCount(webHookTemplateConfigWrapper.getTemplateConfig().getId());
			  if (webHookTemplateConfigWrapper.getStatus().equals(TemplateState.USER_DEFINED) &&  templateUsageCount > 0) {
				  throw new TemplateInUseException(
						  "Cannot relocate template with associated webhooks",
						  new ErrorResult()
						  	.addError("error", "Cannot relocate template with associated webhooks")
						  	.addError("webHookCount", "Associated webhook count: " + templateUsageCount));
			  }
		}
	}

	@POST
	  @Path("/{templateLocator}/patch")
	  @Consumes({"application/xml", "application/json"})
	  @Produces({"application/xml", "application/json"})
	  public Template updateTemplate(@PathParam("templateLocator") String templateLocator, Template newTemplate) {
		  final String updateMode = UPDATE_TEMPLATE;
		  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  
		  String externalProjectId  = findExternalProjectId(templateLocator, newTemplate.projectId, templateConfigWrapper);

		  checkTemplateWritePermission(externalProjectId);
		  checkTemplateWritePermission(templateConfigWrapper.getExternalProjectId());
		  checkTemplateCanBeRelocated(externalProjectId, templateConfigWrapper);
		  
		  if (templateConfigWrapper.getTemplateConfig() == null){
			  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
		  }

		  ErrorResult validationResult = myTemplateValidator.validateTemplate(templateConfigWrapper, newTemplate, new ErrorResult());
		  if (validationResult.isErrored()) {
			  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
		  }
		  WebHookTemplateConfig template = templateConfigWrapper.getTemplateConfig();

		  if(newTemplate.description != null) {
			  template.setTemplateDescription(newTemplate.description);
		  }
		  
		  if (newTemplate.projectId != null) {
			  template.setProjectInternalId(myDataProvider.getProjectIdResolver().getInternalProjectId(externalProjectId));
		  }

		  if(newTemplate.format != null) {
			  template.setFormat(newTemplate.format);
		  }

		  if(newTemplate.rank != null) {
			  template.setRank(newTemplate.rank);
		  }

		  if (newTemplate.preferredDateFormat != null) {
			  template.setPreferredDateTimeFormat(newTemplate.preferredDateFormat);
		  }

		  if (newTemplate.toolTip != null) {
			  template.setTemplateToolTip(newTemplate.toolTip);
		  }

		  // We don't (currently) support updating defaultTemplate or other templateItems
		  // The validator will have thrown an error above.

		  myTemplateManager.registerTemplateFormatFromXmlConfig(template);
		  if (persistAllXmlConfigTemplates(updateMode)){
			  return new Template(new WebHookTemplateConfigWrapper(template, myDataProvider.getProjectIdResolver().getExternalProjectId(template.getProjectInternalId()), myTemplateManager.getTemplateState(newTemplate.id, TemplateState.BEST), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
		  } else {
			  throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  }

  @GET
  @Path("/{templateLocator}/rawConfig")
  @Produces({"application/xml"})
  public WebHookTemplateEntity serveRawConfigTemplate(@PathParam("templateLocator") String templateLocator) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  return WebHookTemplateConfigBuilder.buildEntity(wrapper.getTemplateConfig());
  }

  @GET
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateConfig serveFullConfigTemplate(@PathParam("templateLocator") String templateLocator) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  return wrapper.getTemplateConfig();
  }

  @PUT
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public WebHookTemplateConfig updateFullConfigTemplate(@PathParam("templateLocator") String templateLocator,  WebHookTemplateConfig rawConfig) {
	  WebHookTemplateConfigWrapper webHookTemplateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  if (webHookTemplateConfigWrapper == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  
	  String externalProjectId  = findExternalProjectId(templateLocator, rawConfig);
	  checkTemplateWritePermission(externalProjectId);
	  checkTemplateWritePermission(webHookTemplateConfigWrapper.getExternalProjectId());
	  checkTemplateCanBeRelocated(externalProjectId, webHookTemplateConfigWrapper);

	  WebHookTemplateConfigWrapper newConfigWrapper = new WebHookTemplateConfigWrapper(rawConfig, externalProjectId, myTemplateManager.getTemplateState(webHookTemplateConfigWrapper.getTemplateConfig().getId(), TemplateState.BEST), WebHookTemplateStates.build(webHookTemplateConfigWrapper.getTemplateConfig()));
	  
	  Template newTemplate = new Template(newConfigWrapper, Fields.LONG, myBeanContext);

	  ErrorResult validationResult = myTemplateValidator.validateTemplate(webHookTemplateConfigWrapper, newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
	  }

	  // The above will throw errors if the template is not found, so let's attempt to update it.
	  if (webHookTemplateConfigWrapper.getTemplateConfig().getId().equals(rawConfig.getId())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(rawConfig);
		  if (persistAllXmlConfigTemplates(UPDATE_TEMPLATE)){
		  	return myTemplateManager.getTemplateConfig(rawConfig.getId(), TemplateState.BEST);
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  }
	  throw new OperationException("The template id in the payload did not match the template id in the URL.");
  }

	private String findExternalProjectId(String templateLocator, WebHookTemplateConfig rawConfig) {
		final Locator locator = new Locator(templateLocator, PROJECT_ID);
		if (locator.getSingleDimensionValue(PROJECT_ID) != null ) {
			return locator.getSingleDimensionValue(PROJECT_ID);
		}
		return this.myDataProvider.getProjectIdResolver().getExternalProjectId(rawConfig.getProjectInternalId());
	}
	
	private String findExternalProjectId(String templateLocator, WebHookTemplateEntity rawConfig) {
		final Locator locator = new Locator(templateLocator, PROJECT_ID);
		if (locator.getSingleDimensionValue(PROJECT_ID) != null ) {
			return locator.getSingleDimensionValue(PROJECT_ID);
		}
		return this.myDataProvider.getProjectIdResolver().getExternalProjectId(rawConfig.getAssociatedProjectId());
	}
	
	private String findExternalProjectId(String templateLocator, String externalProjectId, WebHookTemplateConfigWrapper configWrapper) {
		final Locator locator = new Locator(templateLocator, PROJECT_ID);
		if (locator.getSingleDimensionValue(PROJECT_ID) != null ) {
			return locator.getSingleDimensionValue(PROJECT_ID);
		} else if (externalProjectId != null) {
			return externalProjectId;
		}
		return configWrapper.getExternalProjectId();
	}

  @PUT
  @Path("/{templateLocator}/rawConfig")
  @Consumes({"application/xml"})
  @Produces({"application/xml"})
  public WebHookTemplateEntity updateFullConfigTemplateInPlainText(@PathParam("templateLocator") String templateLocator,  WebHookTemplateEntity rawConfig) {
	  WebHookTemplateConfigWrapper webHookTemplateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  if (webHookTemplateConfigWrapper == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }

	  String externalProjectId  = findExternalProjectId(templateLocator, rawConfig);
	  checkTemplateWritePermission(externalProjectId);
	  checkTemplateWritePermission(webHookTemplateConfigWrapper.getExternalProjectId());
	  checkTemplateCanBeRelocated(externalProjectId, webHookTemplateConfigWrapper);
	  
	  WebHookTemplateConfig newConfig = WebHookTemplateConfigBuilder.buildConfig(rawConfig);
	  Template newTemplate = new Template(new WebHookTemplateConfigWrapper(newConfig, externalProjectId, myTemplateManager.getTemplateState(webHookTemplateConfigWrapper.getTemplateConfig().getId(), TemplateState.BEST), WebHookTemplateStates.build(webHookTemplateConfigWrapper.getTemplateConfig())), Fields.LONG, myBeanContext);

	  ErrorResult validationResult = myTemplateValidator.validateTemplate(webHookTemplateConfigWrapper, newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
	  }

	  if (webHookTemplateConfigWrapper.getTemplateConfig().getId().equals(rawConfig.getId())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(newConfig);
		  if (persistAllXmlConfigTemplates(UPDATE_TEMPLATE)){
			  return WebHookTemplateConfigBuilder.buildEntity(myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig());
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  }
	  throw new OperationException("The template id in the payload did not match the template id in the URL.");
  }

  /**
   * /webhooks/templates/id:elasticsearch
   */
  @DELETE
  @Path("/{templateLocator}")
  @Produces({"application/xml", "application/json"})
  public void deleteTemplate(@PathParam("templateLocator") String templateLocator, @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper webHookTemplateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(webHookTemplateConfigWrapper.getExternalProjectId());

	  if (webHookTemplateConfigWrapper.getTemplateConfig() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateProvided()) {
		  throw new OperationException("You cannot delete a tcWebHooks provided template. If you want to make a template unavailable, please mark it as disabled.");
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateUnknown()) {
		  throw new OperationException("You cannot delete a tcWebHooks template in an unknown state. Please report this as a bug against the tcPlugins/tcWebHooks project on GitHub.");
	  }
	  int templateUsageCount = myDataProvider.getWebHookFinder().getTemplateUsageCount(webHookTemplateConfigWrapper.getTemplateConfig().getId());
	  if (webHookTemplateConfigWrapper.getStatus().equals(TemplateState.USER_DEFINED) &&  templateUsageCount > 0) {
		  throw new TemplateInUseException(
				  "Cannot delete template with associated webhooks",
				  new ErrorResult()
				  	.addError("error", "Cannot delete template with associated webhooks")
				  	.addError("webHookCount", "Associated webhook count: " + templateUsageCount));
	  }
	  if (myTemplateManager.removeXmlConfigTemplateFormat(webHookTemplateConfigWrapper.getTemplateConfig().getId())) {
		  if ( ! persistAllXmlConfigTemplates("delete Template")){
			  throw new OperationException("There was an error deleting your template. Unable to persist the change to disk.");
		  }
	  } else {
		  throw new OperationException("The template you wished to delete was not removed. It does not appear to be an XML defined template.");
	  }
  }


  @GET
  @Path("/{templateLocator}/{templateType}/templateContent")
  @Produces({"text/plain"})
  public String serveTemplateContent(@PathParam("templateLocator") String templateLocator, @PathParam("templateType") String templateType) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  WebHookTemplateConfig template = wrapper.getTemplateConfig();
	  if(templateType.equals(DEFAULT_TEMPLATE)){
		  if (template.getDefaultTemplate() == null){
			  throw new NotFoundException("This template does not have a default template configured.");
		  }
		  return template.getDefaultTemplate().getTemplateContent();
	  } else if(templateType.equals("defaultBranchTemplate")){
		  if (template.getDefaultBranchTemplate() == null){
			  throw new NotFoundException("This template does not have a default branch template configured.");
		  }
		  return template.getDefaultBranchTemplate().getTemplateContent();
	  }
	  throw new BadRequestException(IT_WAS_NOT_POSSIBLE_TO_PROCESS_YOUR_REQUEST_FOR_TEMPLATE_CONTENT);
  }

  @PUT
  @Path("/{templateLocator}/{templateType}/templateContent")
  @Consumes({"text/plain"})
  @Produces({"text/plain"})
  public String updateTemplateContent(@PathParam("templateLocator") String templateLocator, @PathParam("templateType") String templateType, String templateText) {
	  WebHookTemplateConfigWrapper wrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(wrapper.getExternalProjectId());
	  WebHookTemplateConfig template = wrapper.getTemplateConfig();
	  if(templateType.equals(DEFAULT_TEMPLATE)){
		  WebHookTemplateText defaultTemplateText = template.getDefaultTemplate();
		  if (defaultTemplateText != null){
			  defaultTemplateText.setTemplateContent(templateText);
		  } else {
			  defaultTemplateText = new WebHookTemplateConfig.WebHookTemplateText(templateText);
		  }
		  template.setDefaultTemplate(defaultTemplateText);

		  myTemplateManager.registerTemplateFormatFromXmlConfig(template);
		  if (persistAllXmlConfigTemplates("update Default TemplateItem content")){
		  	return template.getDefaultTemplate().getTemplateContent();
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  } else if (templateType.equals("defaultBranchTemplate")){
		  WebHookTemplateBranchText defaultBranchTemplateText = template.getDefaultBranchTemplate();
		  if (defaultBranchTemplateText != null){
			  defaultBranchTemplateText.setTemplateContent(templateText);
		  } else {
			  defaultBranchTemplateText = new WebHookTemplateConfig.WebHookTemplateBranchText(templateText);
		  }
		  template.setDefaultBranchTemplate(defaultBranchTemplateText);

		  myTemplateManager.registerTemplateFormatFromXmlConfig(template);
		  if (persistAllXmlConfigTemplates("update TemplateItem content")){
		  	return template.getDefaultBranchTemplate().getTemplateContent();
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }

	  }

	  // TOOO: Need to handle

	  throw new BadRequestException(IT_WAS_NOT_POSSIBLE_TO_PROCESS_YOUR_REQUEST_FOR_TEMPLATE_CONTENT);
  }


  @GET
  @Path("/{templateLocator}/templateItems/{templateItemId}/{templateContentType}")
  @Produces({"text/plain"})
  public String serveSpecificTemplateContent(@PathParam("templateLocator") String templateLocator,
		  									 @PathParam("templateItemId") String templateItemId,
		  									 @PathParam("templateContentType") String templateContentType) {
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  checkTemplateReadPermission(template.getExternalProjectId());
	  return getTemplateContent(template, templateContentType);
  }

  public String getTemplateContent(WebHookTemplateItemConfigWrapper template, String templateContentType) {
	  if (template == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if(templateContentType.equals("templateContent")){
		  if (template.getTemplateItem().getTemplateText() == null){
			  throw new NotFoundException("This template does not have a non-branch template configured.");
		  }
		  return template.getTemplateItem().getTemplateText().getTemplateContent();
	  } else if(templateContentType.equals("branchTemplateContent")){
		  if (template.getTemplateItem().getBranchTemplateText() == null){
			  throw new NotFoundException("This template does not have a  branch template configured.");
		  }
		  return template.getTemplateItem().getBranchTemplateText().getTemplateContent();
	  }
	  throw new BadRequestException(IT_WAS_NOT_POSSIBLE_TO_PROCESS_YOUR_REQUEST_FOR_TEMPLATE_CONTENT);
  }

  /**
   * /webhooks/templates/id:elasticsearch/templateItems/id:1
   * /webhooks/templates/id:elasticsearch/templateItems/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItems/id:defaultTemplate
   */
  @GET
  @Path("/{templateLocator}/templateItems/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public TemplateItem serveTemplateItem(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateReadPermission(templateConfig.getExternalProjectId());
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if(DEFAULT_TEMPLATE.equals(template.getTemplateItem().getId())) {
		  return new TemplateItem(templateConfig, template.getTemplateItem().getTemplateText(), template.getTemplateItem().getBranchTemplateText(), template.getTemplateItem().getId(), new Fields(fields), myBeanContext);
	  }
	  return new TemplateItem(templateConfig, template.getTemplateItem(), template.getTemplateItem().getId(), new Fields(fields), myBeanContext);
  }

  /**
   * /webhooks/templates/id:elasticsearch/PROVIDED/templateItems/id:1/
   * /webhooks/templates/id:elasticsearch/templateItems/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItems/id:defaultTemplate
   */
  @GET
  @Path("/{templateLocator1}/templateItems/{templateItemId1}/{templateContentType1}/diff/{templateLocator2}/templateItems/{templateItemId2}/{templateContentType2}")
  @Produces({"text/plain"})
  public String diffTemplateItem(
		  @PathParam("templateLocator1") String templateLocator1,
		  @PathParam("templateItemId1") String templateItemId1,
		  @PathParam("templateContentType1") String templateContentType1,
  		  @PathParam("templateLocator2") String templateLocator2,
		  @PathParam("templateItemId2") String templateItemId2,
		  @PathParam("templateContentType2") String templateContentType2,
		  @QueryParam("context") String context) {

	  WebHookTemplateConfigWrapper templateWrapper1 = myDataProvider.getTemplateFinder().findTemplateById(templateLocator1);
	  WebHookTemplateConfigWrapper templateWrapper2 = myDataProvider.getTemplateFinder().findTemplateById(templateLocator2);
	  checkTemplateReadPermission(templateWrapper1.getExternalProjectId());
	  checkTemplateReadPermission(templateWrapper2.getExternalProjectId());
	  
	  WebHookTemplateItemConfigWrapper templateItem1 = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator1, templateItemId1);
	  WebHookTemplateItemConfigWrapper templateItem2 = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator2, templateItemId2);
	  
	  Integer contextLines = Objects.nonNull(context) ? Integer.valueOf(context) : 100;

	  Template template1 = new Template(templateWrapper1, new Fields("$short"), myBeanContext);
	  Template template2 = new Template(templateWrapper2, new Fields("$short"), myBeanContext);


	  String content1 = getTemplateContent(templateItem1, templateContentType1);
	  String content2 = getTemplateContent(templateItem2, templateContentType2);

	  List<String> text1=Arrays.asList(content1.split("\n"));
	  List<String> text2=Arrays.asList(content2.split("\n"));

	  try {
		  //generating diff information.
		  Patch<String> diff = DiffUtils.diff(text1, text2);

		  //generating unified diff format
		  List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
				"webhooks/templates/id:" + template1.getId() + ",status:" + template1.status +  "/templateItems/" + templateItemId1 + "/" + templateContentType1,
				"webhooks/templates/id:" + template2.getId() + ",status:" + template2.status +  "/templateItems/" + templateItemId2 + "/" + templateContentType2,
		  		text1, diff, contextLines);

		  return unifiedDiff.stream().collect(Collectors.joining("\n"));
	} catch (DiffException e) {
		throw new OperationException(e.getMessage());
	}
  }
  /**
   * /webhooks/templates/id:elasticsearch/PROVIDED/templateItems/id:1/
   * /webhooks/templates/id:elasticsearch/templateItems/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItems/id:defaultTemplate
   */
  @GET
  @Path("/{templateLocator1}/diff/{templateLocator2}")
  @Produces({"text/plain"})
  public String diffTemplateItem(
		  @PathParam("templateLocator1") String templateLocator1,
  		  @PathParam("templateLocator2") String templateLocator2,
		  @QueryParam("fields") String fields,
		  @QueryParam("context") String context) {

	  WebHookTemplateConfigWrapper templateWrapper1 = myDataProvider.getTemplateFinder().findTemplateById(templateLocator1);
	  WebHookTemplateConfigWrapper templateWrapper2 = myDataProvider.getTemplateFinder().findTemplateById(templateLocator2);
	  checkTemplateReadPermission(templateWrapper1.getExternalProjectId());
	  checkTemplateReadPermission(templateWrapper2.getExternalProjectId());
	  Integer contextLines = Objects.nonNull(context) ? Integer.valueOf(context) : 100;

	  Template template1 = new Template(templateWrapper1, new Fields(fields), myBeanContext);
	  Template template2 = new Template(templateWrapper2, new Fields(fields), myBeanContext);

	  int maxTemplateTypes = template1.getTemplates().size() > template2.getTemplates().size() ? template1.getTemplates().size() : template2.getTemplates().size();

	  try {

		  List<String> unifiedDiffs = new ArrayList<>();

		  if (Objects.nonNull(template1.defaultTemplate) || Objects.nonNull(template2.defaultTemplate)) {
			  unifiedDiffs.addAll(getTemplateItemContentDiffs(template1, template1.defaultTemplate, template2, template2.defaultTemplate, contextLines));
			  unifiedDiffs.addAll(getTemplateItemBranchContentDiffs(template1, template1.defaultTemplate, template2, template2.defaultTemplate, contextLines));
		  }

		  for (int i = 0; i < maxTemplateTypes; i++) {
			  TemplateItem templateItem1 = null;
			  TemplateItem templateItem2 = null;
			  if (template1.getTemplates().size() > i){
				  templateItem1 = template1.getTemplates().get(i);
			  }
			  if (template2.getTemplates().size() > i){
				  templateItem2 = template2.getTemplates().get(i);
			  }
			  unifiedDiffs.addAll(getTemplateItemContentDiffs(template1, templateItem1, template2, templateItem2, contextLines));
			  unifiedDiffs.addAll(getTemplateItemBranchContentDiffs(template1, templateItem1, template2, templateItem2, contextLines));
		  }

		  return unifiedDiffs.stream().collect(Collectors.joining("\n"));
	} catch (DiffException e) {
		throw new OperationException(e.getMessage());
	}
  }

  private List<String> getTemplateItemContentDiffs(Template template1, TemplateItem templateItem1,
		  										   Template template2, TemplateItem templateItem2,
		  										   Integer contextLines) throws DiffException {
	  String content1 = "";
	  String content2 = "";
	  String id1 = "none";
	  String id2 = "none";
	  if (Objects.nonNull(templateItem1) && Objects.nonNull(templateItem1.getTemplateText())) {
		  content1 = templateItem1.getTemplateText().content;
		  id1 = templateItem1.getId();
	  }
	  if (Objects.nonNull(templateItem2) && Objects.nonNull(templateItem2.getTemplateText())) {
		  content2 = templateItem2.getTemplateText().content;
		  id2 = templateItem2.getId();
	  }
	  List<String> text1=Arrays.asList(content1.split("\n"));
	  List<String> text2=Arrays.asList(content2.split("\n"));

	  return UnifiedDiffUtils.generateUnifiedDiff(
			    "webhooks/templates/id:" + template1.getId() + ",status:" + template1.status +  "/templateItems/" + id1 + "/templateText",
			    "webhooks/templates/id:" + template2.getId() + ",status:" + template2.status +  "/templateItems/" + id2 + "/templateText",
			  	text1,
			  	DiffUtils.diff(text1, text2),
			  	contextLines
			  	);
}

  private List<String> getTemplateItemBranchContentDiffs(Template template1, TemplateItem templateItem1,
														 Template template2, TemplateItem templateItem2,
														 Integer contextLines) throws DiffException {
	  String content1 = "";
	  String content2 = "";
	  String id1 = "none";
	  String id2 = "none";
	  if (Objects.nonNull(templateItem1) && Objects.nonNull(templateItem1.getBranchTemplateText())) {
		  content1 = templateItem1.getBranchTemplateText().content;
		  id1 = templateItem1.getId();
	  }
	  if (Objects.nonNull(templateItem2) && Objects.nonNull(templateItem2.getBranchTemplateText())) {
		  content2 = templateItem2.getBranchTemplateText().content;
		  id2 = templateItem2.getId();
	  }
	  List<String> text1=Arrays.asList(content1.split("\n"));
	  List<String> text2=Arrays.asList(content2.split("\n"));

	  return UnifiedDiffUtils.generateUnifiedDiff(
			  // webhooks/templates/id:slack.com/templateItems/defaultTemplate/branchTemplateContent
			  "webhooks/templates/id:" + template1.getId() + ",status:" + template1.status +  "/templateItems/" + id1 + "/branchTemplateText",
			  "webhooks/templates/id:" + template2.getId() + ",status:" + template2.status +  "/templateItems/" + id2 + "/branchTemplateText",
			  text1,
			  DiffUtils.diff(text1, text2),
			  contextLines
			  );
  }

/**
   * /webhooks/templates/id:elasticsearch/templateItems/id:1
   * /webhooks/templates/id:elasticsearch/templateItems/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItems/id:defaultTemplate
   */
  @DELETE
  @Path("/{templateLocator}/templateItems/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public void deleteTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId,
		  @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(templateConfig.getExternalProjectId());
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if(DEFAULT_TEMPLATE.equals(template.getTemplateItem().getId())) {
		  templateConfig.getTemplateConfig().setDefaultTemplate(null);
		  templateConfig.getTemplateConfig().setDefaultBranchTemplate(null);
	  } else {
		  templateConfig.getTemplateConfig().getTemplates().deleteTemplateItem(Integer.valueOf(template.getTemplateItem().getId()));
	  }
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig.getTemplateConfig());
	  if ( ! persistAllXmlConfigTemplates("delete TemplateItem")){
	   	throw new OperationException(ERROR_SAVING_TEMPLATE);
	  }
  }

  /**
   * /webhooks/templates/id:elasticsearch/templateItems/id:1
   * /webhooks/templates/id:elasticsearch/templateItems/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItems/id:defaultTemplate
   */
  @PUT
  @Path("/{templateLocator}/templateItems/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public TemplateItem updateTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId, @QueryParam("fields") String fields, TemplateItem templateItem) {
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(templateConfigWrapper.getExternalProjectId());
	  WebHookTemplateItemConfigWrapper templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (templateItemConfigWrapper.getTemplateItem() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if(DEFAULT_TEMPLATE.equals(templateItemConfigWrapper.getTemplateItem().getId())) {

		  TemplateItem previousDefaultTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem().getTemplateText(), templateItemConfigWrapper.getTemplateItem().getBranchTemplateText(), templateItemConfigWrapper.getTemplateItem().getId(), Fields.ALL_NESTED, myBeanContext);
		  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousDefaultTemplateItem, templateItem, new ErrorResult());
		  if (validationResult.isErrored()) {
			  throw new UnprocessableEntityException(TEMPLATE_ITEM_CONTAINED_INVALID_DATA, validationResult);
		  }
		  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();
		  if (templateItem.getTemplateText() != null) {
			  if (templateItem.getTemplateText().getUseTemplateTextForBranch() != null) {
				  templateConfig.getDefaultTemplate().setUseTemplateTextForBranch(templateItem.getTemplateText().getUseTemplateTextForBranch());
			  }
			  if (templateItem.getTemplateText().getContent() != null) {
				  templateConfig.getDefaultTemplate().setTemplateContent(templateItem.getTemplateText().getContent());
			  }
		  }
		  if (templateItem.getBranchTemplateText() != null && templateItem.getBranchTemplateText().getContent() != null) {
			  templateConfig.getDefaultBranchTemplate().setTemplateContent(templateItem.getBranchTemplateText().getContent());
		  }

		  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
		  if (persistAllXmlConfigTemplates("update Default TemplateItem")){
			  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
			  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
			  return new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem().getTemplateText(), templateItemConfigWrapper.getTemplateItem().getBranchTemplateText(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext);
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }


	  }
	  TemplateItem previousTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId(), Fields.ALL_NESTED, myBeanContext);
	  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousTemplateItem, templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_ITEM_CONTAINED_INVALID_DATA, validationResult);
	  }

	  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();

	  WebHookTemplateItem templateItemConfig = templateConfig.getTemplates().getTemplateItem(Integer.valueOf(templateItemConfigWrapper.getTemplateItem().getId()));

	  if (templateItem.getTemplateText() != null) {
		  if (templateItem.getTemplateText().getUseTemplateTextForBranch() != null) {
			  templateItemConfig.getTemplateText().setUseTemplateTextForBranch(templateItem.getTemplateText().getUseTemplateTextForBranch());
		  }
		  if (templateItem.getTemplateText().getContent() != null) {
			  templateItemConfig.getTemplateText().setTemplateContent(templateItem.getTemplateText().getContent());
		  }
	  }
	  if (templateItem.getBranchTemplateText() != null && templateItem.getBranchTemplateText().getContent() != null) {
		  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
	  }
	  if (templateItem.getBuildStates() != null) {
		  templateItemConfig.getStates().clear();

		  for (WebHookTemplateStateRest itemState : templateItem.getBuildStates()) {

				if (itemState != null && itemState.isEnabled()) {
					templateItemConfig.getStates().add(new WebHookTemplateState(itemState.getType(), itemState.isEnabled()));
				}
		  }
	  }

	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (persistAllXmlConfigTemplates("update TemplateItem")) {
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
		  return new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext);
	  } else {
	   	throw new OperationException(ERROR_SAVING_TEMPLATE);
	  }

  }

	private boolean persistAllXmlConfigTemplates(String saveContext) {
		try {
			return myTemplateManager.persistAllXmlConfigTemplates();

		} catch (ProbableJaxbJarConflictErrorException ex) {
			ErrorResult er = new ErrorResult();
			er.addError("unableToPersistTemplates",
					"Unable to " + saveContext + ".  A ClassCastError occurred whilst invoking JAXB. This is probably due to a JAXB jar conflict. See https://github.com/tcplugins/tcWebHooks/wiki/Fixing-the-REST-API-Jar-Conflict");
			throw new JaxbClassCastException("Unable to " + saveContext, ex, er);
		}
	}

  /**
   * Creates a new Build Event Template.
   * /webhooks/templates/id:elasticsearch/templateItem
   */
  @POST
  @Path("/{templateLocator}/templateItem")
  @Produces({"application/xml", "application/json"})
  public Response createTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @QueryParam("fields") String fields, TemplateItem templateItem) {
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(templateConfigWrapper.getExternalProjectId());
	  WebHookTemplateItemConfigWrapper templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, "_new");
	  if (templateConfigWrapper.getTemplateConfig() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }

	  TemplateItem previousTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId(), Fields.ALL_NESTED, myBeanContext);
	  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousTemplateItem, templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_ITEM_CONTAINED_INVALID_DATA, validationResult);
	  }

	  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();

	  WebHookTemplateItem templateItemConfig = buildTemplateItem(templateItem, templateConfig.getTemplates());

	  templateConfig.getTemplates().getTemplates().add(templateItemConfig);
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (persistAllXmlConfigTemplates("create TemplateItem")){
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemConfig.getId().toString());
		  return Response.status(201)
				  .header(
						  "Location",
						  myBeanContext.getApiUrlBuilder().getTemplateItemHref(templateConfig, templateItemConfigWrapper.getTemplateItem())
						  )
				  .entity(new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext))
				  .build();
	  } else {
		  throw new OperationException(ERROR_SAVING_TEMPLATE);
	  }

  }

private WebHookTemplateItem buildTemplateItem(TemplateItem templateItem, WebHookTemplateItems templates) {
	WebHookTemplateItem templateItemConfig = new WebHookTemplateItem();
	  templateItemConfig.setTemplateText(new WebHookTemplateText(""));
	  templateItemConfig.setBranchTemplateText(new WebHookTemplateBranchText(""));
	  templateItemConfig.setId(templates.getMaxId());

	  if (templateItem.getTemplateText() != null) {
		  if (templateItem.getTemplateText().getUseTemplateTextForBranch() != null) {
			  templateItemConfig.getTemplateText().setUseTemplateTextForBranch(templateItem.getTemplateText().getUseTemplateTextForBranch());
		  }
		  if (templateItem.getTemplateText().getContent() != null) {
			  templateItemConfig.getTemplateText().setTemplateContent(templateItem.getTemplateText().getContent());
		  }
	  }
	  if (templateItem.getBranchTemplateText() != null && templateItem.getBranchTemplateText().getContent() != null) {
		  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
	  }
	  if (templateItem.getBuildStates() != null) {
		  templateItemConfig.getStates().clear();

		  for (WebHookTemplateStateRest itemState : templateItem.getBuildStates()) {

			  if (itemState != null && itemState.isEnabled()) {
				  templateItemConfig.getStates().add(new WebHookTemplateState(itemState.getType(), itemState.isEnabled()));
			  }
		  }
	  }
	return templateItemConfig;
}

  /**
   *  /app/rest/webhooks/templates/id:flowdock/templateItems/id:2/buildStates/buildStarted
   *  							  /id:elasticsearch/templateItems/id:1/buildStates/buildStarted
   *  							  /id:elasticsearch/templateItems/defaultTemplate/buildStates/buildStarted
   */
  @GET
  @Path("/{templateLocator}/templateItems/{templateItemId}/buildStates/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest serveTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @PathParam("buildState") String buildState,
		  											 @QueryParam("fields") String fields) {
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  checkTemplateReadPermission(template.getExternalProjectId());
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that id");
	  }
	  if (DEFAULT_TEMPLATE.equals(template.getTemplateItem().getId())){
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
				if (state.getShortName().equals(buildState))
						return new WebHookTemplateStateRest(
													state.getShortName(),
													template.getBuildStatesWithTemplate().isAvailable(state),
													ValueWithDefault.decideDefault(new Fields(fields).isIncluded("editable",false, true), false),
													null
												);
			}
	  }
	  return new WebHookTemplateStateRest(template.getTemplateItem(), buildState, template.getBuildStatesWithTemplate(), new Fields(fields), myBeanContext);

  }

  @PUT
  @Path("/{templateLocator}/templateItems/{templateItemId}/buildStates/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest updateTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId,
		  @PathParam("buildState") String buildState,
		  WebHookTemplateStateRest updatedBuildState) {
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  checkTemplateWritePermission(template.getExternalProjectId());
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that id");
	  }
	  return new WebHookTemplateStateRest(template.getTemplateItem(), buildState, template.getBuildStatesWithTemplate(), new Fields(null), myBeanContext);

  }

  /**
   * Creates a new Build Event DefaultTemplate.
   * /webhooks/templates/id:elasticsearch/defaultTemplate
   */
  @POST
  @Path("/{templateLocator}/defaultTemplate")
  @Produces({"application/xml", "application/json"})
  public Response createDefaultTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @QueryParam("fields") String fields, TemplateItem templateItem) {
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  checkTemplateWritePermission(templateConfigWrapper.getExternalProjectId());

	  if (templateConfigWrapper.getTemplateConfig() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }

	  if (templateConfigWrapper.getTemplateConfig().getDefaultTemplate() != null){
		  throw new BadRequestException("Default Template Item already exists. To update existing default template, please use PUT");
	  }

	  final ErrorResult validationResult = myTemplateValidator.validateDefaultTemplateItem(templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("DefaultTemplateItem contained invalid data", validationResult);
	  }

	  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();

	  WebHookTemplateItem templateItemConfig = buildDefaultTemplateItem(templateItem);

	  templateConfig.setDefaultTemplate(templateItemConfig.getTemplateText());
	  templateConfig.setDefaultBranchTemplate(templateItemConfig.getBranchTemplateText());

	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (persistAllXmlConfigTemplates("create Default TemplateItem")){
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  WebHookTemplateItemConfigWrapper templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, DEFAULT_TEMPLATE);
		  return Response.status(201)
				  .header(
						  "Location",
						  myBeanContext.getApiUrlBuilder().getTemplateItemHref(templateConfig, templateItemConfigWrapper.getTemplateItem())
						  )
				  .entity(
						  new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem().getTemplateText(), templateItemConfigWrapper.getTemplateItem().getBranchTemplateText(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext)
						  )
				  .build();


	  } else {
		  throw new OperationException(ERROR_SAVING_TEMPLATE);
	  }

  }

	private WebHookTemplateItem buildDefaultTemplateItem(TemplateItem templateItem) {
		WebHookTemplateItem templateItemConfig = new WebHookTemplateItem();
		  templateItemConfig.setTemplateText(new WebHookTemplateText(""));
		  templateItemConfig.setBranchTemplateText(new WebHookTemplateBranchText(""));

		  if (templateItem.getTemplateText() != null) {
			  if (templateItem.getTemplateText().getUseTemplateTextForBranch() != null) {
				  templateItemConfig.getTemplateText().setUseTemplateTextForBranch(templateItem.getTemplateText().getUseTemplateTextForBranch());
			  }
			  if (templateItem.getTemplateText().getContent() != null) {
				  templateItemConfig.getTemplateText().setTemplateContent(templateItem.getTemplateText().getContent());
			  }
		  }
		  if (templateItem.getBranchTemplateText() != null && templateItem.getBranchTemplateText().getContent() != null) {
			  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
		  }
		return templateItemConfig;
	}


	private void checkTemplateReadPermission() {
		try {
			myDataProvider.getSecurityContext().getAuthorityHolder().isPermissionGrantedForAnyProject(Permission.EDIT_PROJECT);
		} catch (AuthorizationFailedException e) {
			throw new TemplatePermissionException("Reading Project templates requires permission 'VIEW_PROJECT' on any project project");
		}
	}
	
	private SProject checkTemplateReadPermission(String externalProjectId) {
		try {
			SProject sProject = myDataProvider.getProjectManager().findProjectByExternalId(externalProjectId);
			if (sProject == null) {
				throw new NotFoundException("No project found with supplied projectId");
			}
			myPermissionChecker.checkProjectPermission(PROJECT_TEMPLATE_READ_PERMISSION, sProject.getProjectId());
			return sProject;
		} catch (AccessDeniedException | AuthorizationFailedException e) {
			throw new TemplatePermissionException("Reading Project templates requires permission 'VIEW_PROJECT' on the relevant project");
		}		
	}
	
	private SProject checkTemplateWritePermission(String externalProjectId) {
		try {
			SProject sProject = myDataProvider.getProjectManager().findProjectByExternalId(externalProjectId);
			if (sProject == null) {
				throw new NotFoundException("No project found with supplied projectId");
			}
			myPermissionChecker.checkProjectPermission(PROJECT_TEMPLATE_EDIT_PERMISSION, sProject.getProjectId());
			return sProject;
		} catch (AccessDeniedException | AuthorizationFailedException e) {
			throw new TemplatePermissionException("Writing Project templates requires permission 'EDIT_PROJECT' on the relevant project");
		}
	}

}

