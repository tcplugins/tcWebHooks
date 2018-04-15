package webhook.teamcity.server.rest.request;

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

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.errors.OperationException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.ProbableJaxbJarConflictErrorException;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.data.WebHookTemplateStates;
import webhook.teamcity.server.rest.errors.BadRequestException;
import webhook.teamcity.server.rest.errors.JaxbClassCastException;
import webhook.teamcity.server.rest.errors.TemplatePermissionException;
import webhook.teamcity.server.rest.errors.UnprocessableEntityException;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.ErrorResult;
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
  private static final String DEFAULT_TEMPLATE = "defaultTemplate";
  private static final String TEMPLATE_ITEMS = "templateItems";
  private static final String TEMPLATE_ITEM_CONTAINED_INVALID_DATA = "TemplateItem contained invalid data";
  private static final String NO_TEMPLATE_FOUND_BY_THAT_ID = "No template found by that id";
  private static final String TEMPLATE_CONTAINED_INVALID_DATA = "Template contained invalid data";
  private static final String ERROR_SAVING_TEMPLATE = "There was an error saving your template. Sorry.";
  private static final String IT_WAS_NOT_POSSIBLE_TO_PROCESS_YOUR_REQUEST_FOR_TEMPLATE_CONTENT = "Sorry. It was not possible to process your request for template content.";
  private static final Permission templateEditPermission = Permission.CHANGE_SERVER_SETTINGS;
  private static final Permission[] templateReadPermissions = { 
		  													   Permission.VIEW_PROJECT, 
		  													   Permission.VIEW_BUILD_CONFIGURATION_SETTINGS, 
		  													   Permission.EDIT_PROJECT 
		  													  };

  @Context @NotNull private DataProvider myDataProvider;
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
	  checkTemplateReadPermission();
	  return new Template(myDataProvider.getTemplateFinder().findTemplateById(templateLocator), new Fields(fields), myBeanContext);
  }

	private Template buildAndPersistTemplate(Template newTemplate, String updateMode, WebHookTemplateConfig template) {
		template.setTemplateDescription(newTemplate.description);
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
	    	return new Template(new WebHookTemplateConfigWrapper(template, myTemplateManager.getTemplateState(newTemplate.id), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
	    } else {
	    	throw new OperationException(ERROR_SAVING_TEMPLATE);
	    }
	}

	  @POST
	  @Consumes({"application/xml", "application/json"})
	  @Produces({"application/xml", "application/json"})
	  public Template createNewTemplate(Template newTemplate) {
		String updateMode = "create Template";
		checkTemplateWritePermission();
		ErrorResult validationResult = myTemplateValidator.validateNewTemplate(newTemplate, new ErrorResult());
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
	  @Consumes({"application/xml", "application/json"})
	  @Produces({"application/xml", "application/json"})
	  public Template replaceTemplate(@PathParam("templateLocator") String templateLocator, Template newTemplate) {
		  final String updateMode = "replace Template";
		  checkTemplateWritePermission();
		  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  if (templateConfigWrapper.getTemplateConfig() == null){
			  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
		  }
		  ErrorResult validationResult = myTemplateValidator.validateNewTemplate(newTemplate, new ErrorResult());
		  
		  if ( ! templateConfigWrapper.getTemplateConfig().getId().equals(newTemplate.id)) {
			  validationResult.addError("id", "The templateId in the template does not match the templateId in the URL.");
		  }	
		  
		  if (validationResult.isErrored()) {
			  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
		  }	  
		  WebHookTemplateConfig template = new WebHookTemplateConfig(newTemplate.id, true);
		  
		  return buildAndPersistTemplate(newTemplate, updateMode, template);
	  }
  
	  @POST
	  @Path("/{templateLocator}/patch")
	  @Consumes({"application/xml", "application/json"})
	  @Produces({"application/xml", "application/json"})
	  public Template updateTemplate(@PathParam("templateLocator") String templateLocator, Template newTemplate) {
		  final String updateMode = "update Template";
		  checkTemplateWritePermission();
		  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  if (templateConfigWrapper.getTemplateConfig() == null){
			  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
		  }
		  
		  ErrorResult validationResult = myTemplateValidator.validateTemplate(templateConfigWrapper.getTemplateConfig(), newTemplate, new ErrorResult());
		  if (validationResult.isErrored()) {
			  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
		  }	  
		  WebHookTemplateConfig template = templateConfigWrapper.getTemplateConfig();
		  
		  if(newTemplate.description != null) {
			  template.setTemplateDescription(newTemplate.description);
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
			  return new Template(new WebHookTemplateConfigWrapper(template, myTemplateManager.getTemplateState(newTemplate.id), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
		  } else {
			  throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  }
  
  @GET
  @Path("/{templateLocator}/rawConfig")
  @Produces({"application/xml"})
  public WebHookTemplateEntity serveRawConfigTemplate(@PathParam("templateLocator") String templateLocator) {
	  checkTemplateReadPermission();
	  return WebHookTemplateConfigBuilder.buildEntity(myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig());
  }
  
  @GET
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateConfig serveFullConfigTemplate(@PathParam("templateLocator") String templateLocator) {
	  checkTemplateReadPermission();
	  return myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
  }
  
  @PUT
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public WebHookTemplateConfig updateFullConfigTemplate(@PathParam("templateLocator") String templateLocator,  WebHookTemplateConfig rawConfig) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfig webHookTemplateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
	  if (webHookTemplateConfig == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }

	  Template newTemplate = new Template(new WebHookTemplateConfigWrapper(webHookTemplateConfig, myTemplateManager.getTemplateState(webHookTemplateConfig.getId()), WebHookTemplateStates.build(webHookTemplateConfig)), Fields.LONG, myBeanContext);
	  
	  ErrorResult validationResult = myTemplateValidator.validateTemplate(webHookTemplateConfig, newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
	  }	  
	  
	  // The above will throw errors if the template is not found, so let's attempt to update it.
	  if (webHookTemplateConfig.getId().equals(rawConfig.getId())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(rawConfig);
		  if (persistAllXmlConfigTemplates("update Template")){
		  	return myTemplateManager.getTemplateConfig(rawConfig.getId());
		  } else {
		   	throw new OperationException(ERROR_SAVING_TEMPLATE);
		  }
	  }
	  throw new OperationException("The template id in the payload did not match the template id in the URL.");
  }
  
  @PUT
  @Path("/{templateLocator}/rawConfig")
  @Consumes({"application/xml"})
  @Produces({"application/xml"})
  public WebHookTemplateEntity updateFullConfigTemplateInPlainText(@PathParam("templateLocator") String templateLocator,  WebHookTemplateEntity rawConfig) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfig webHookTemplateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
	  if (webHookTemplateConfig == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  
	  WebHookTemplateConfig newConfig = WebHookTemplateConfigBuilder.buildConfig(rawConfig);
	  Template newTemplate = new Template(new WebHookTemplateConfigWrapper(newConfig, myTemplateManager.getTemplateState(webHookTemplateConfig.getId()), WebHookTemplateStates.build(webHookTemplateConfig)), Fields.LONG, myBeanContext);
	  
	  ErrorResult validationResult = myTemplateValidator.validateTemplate(webHookTemplateConfig, newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException(TEMPLATE_CONTAINED_INVALID_DATA, validationResult);
	  }	  

	  if (webHookTemplateConfig.getId().equals(rawConfig.getId())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(newConfig);
		  if (persistAllXmlConfigTemplates("update Template")){
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
  public void deleteTemplate(@PathParam("templateLocator") String templateLocator,
		  @QueryParam("fields") String fields) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper webHookTemplateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  if (webHookTemplateConfigWrapper.getTemplateConfig() == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateProvided()) {
		  throw new OperationException("You cannot delete a tcWebHooks provided template. If you want to make a template unavailable, please mark it as disabled.");
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateUnknown()) {
		  throw new OperationException("You cannot delete a tcWebHooks template in an unknown state. Please report this as a bug against the tcPlugins/tcWebHooks project on GitHub.");
	  }
	  if (myTemplateManager.removeXmlConfigTemplateFormat(webHookTemplateConfigWrapper.getTemplateConfig().getId())) {
		  if (persistAllXmlConfigTemplates("delete Template")){
			  return;
		  } else {
			  throw new OperationException("There was an error deleting your template. It was possible persist the change.");
		  }
	  } else {
		  throw new OperationException("The template you wished to delete was not removed. It does not appear to be an XML defined template.");
	  }
  }


  @GET
  @Path("/{templateLocator}/{templateType}/templateContent")
  @Produces({"text/plain"})
  public String serveTemplateContent(@PathParam("templateLocator") String templateLocator, @PathParam("templateType") String templateType) {
	  checkTemplateReadPermission();	  
	  WebHookTemplateConfig template = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
	  if (template == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
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
	  checkTemplateWritePermission();
	  WebHookTemplateConfig template = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
	  if (template == null){
		  throw new NotFoundException(NO_TEMPLATE_FOUND_BY_THAT_ID);
	  }
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
		  template.getDefaultBranchTemplate().setTemplateContent(templateText);
		  
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
	  checkTemplateReadPermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
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
	  checkTemplateReadPermission();
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
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
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
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
	  if (persistAllXmlConfigTemplates("delete TemplateItem")){
		  return;
	  } else {
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
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
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
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
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
	  checkTemplateReadPermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
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
	  checkTemplateWritePermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
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
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  
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
			myPermissionChecker.checkGlobalPermissionAnyOf(templateReadPermissions);
		} catch (AuthorizationFailedException e) {
			throw new TemplatePermissionException("Reading templates requires at least one of the following permissions: 'VIEW_PROJECT, VIEW_BUILD_CONFIGURATION_SETTINGS, EDIT_PROJECT'");
		}
	}
	
	private void checkTemplateWritePermission() {
		try {
			myPermissionChecker.checkGlobalPermission(templateEditPermission);
		} catch (AuthorizationFailedException e) {
			throw new TemplatePermissionException("Writing templates requires permission 'CHANGE_SERVER_SETTINGS'");
		}
	}
	
}

