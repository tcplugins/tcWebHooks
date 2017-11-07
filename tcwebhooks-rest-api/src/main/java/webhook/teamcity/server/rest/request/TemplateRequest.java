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

import com.intellij.openapi.diagnostic.Logger;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.errors.OperationException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.util.StringUtil;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.data.WebHookTemplateStates;
import webhook.teamcity.server.rest.errors.BadRequestException;
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
  private static final Logger LOG = Logger.getInstance(TemplateRequest.class.getName());
  public static final boolean ID_GENERATION_FLAG = true;
  private static final Permission templateEditPermission = Permission.CHANGE_SERVER_SETTINGS;
  private static final Permission[] templateReadPermissions = { 
		  													   Permission.VIEW_PROJECT, 
		  													   Permission.VIEW_BUILD_CONFIGURATION_SETTINGS, 
		  													   Permission.EDIT_PROJECT 
		  													  };

  @Context @NotNull private DataProvider myDataProvider;
  @Context @NotNull private WebHookTemplateManager myTemplateManager;
  @Context @NotNull private TemplateValidator myTemplateValidator;
  
//  @Autowired
//  private DataProvider myDataProvider;
//  
//  @Autowired
//  private WebHookTemplateManager myTemplateManager;

  //@Context @NotNull private WebHookApiUrlBuilder myApiUrlBuilder;
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
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/templateItem/defaultTemplate/templateContent" ;
  }
  
  @NotNull
  public static String getDefaultBranchTemplateTextHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/templateItem/defaultTemplate/branchTemplateContent" ;
  }
  
  @NotNull
  public static String getTemplateDefaultItemHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template)+ "/templateItem/defaultTemplate";
  }
  
  @NotNull
  public static String getTemplateItemHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template)+ "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString());
  }
  
  @NotNull
  public static String getTemplateItemTextHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template)+ "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString()) + "/templateContent" ;
  }
  
  @NotNull
  public static String getTemplateItemBranchTextHref(WebHookTemplateConfig template, WebHookTemplateItemRest webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString()) +  "/branchTemplateContent" ;
  }
 
  @NotNull  
  public static String getTemplateStateHref(WebHookTemplateConfig template,	String state) {
		return getTemplateDefaultItemHref(template) + "/buildState/" + state;
  }
  
  @NotNull  
  public static String getTemplateItemStateHref(WebHookTemplateConfig template,	WebHookTemplateItemRest templateItem, String state) {
	  return getTemplateItemHref(template, templateItem) + "/buildState/" + state;
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

  @POST
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Template createNewTemplate(Template newTemplate) {
	checkTemplateWritePermission();
	ErrorResult validationResult = myTemplateValidator.validateNewTemplate(newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("Template contained invalid data", validationResult);
	  }	  
    WebHookTemplateConfig template = new WebHookTemplateConfig(newTemplate.id, true);
    template.setTemplateDescription(newTemplate.description);
    
    if (myTemplateManager.getTemplate(template.getId()) != null){
    	validationResult.addError("name", "Template of that name already exists. To update existing template, please use PUT");
    	throw new BadRequestException("Template name already exists", validationResult);
    }
    
    template.setFormat(newTemplate.format);
	template.setRank(newTemplate.rank);
	
	if (newTemplate.preferredDateFormat != null) {
		template.setPreferredDateTimeFormat(newTemplate.preferredDateFormat);
	}
	
	if (newTemplate.toolTip != null) {
		template.setTemplateToolTip(newTemplate.toolTip);
	}
	
    if (newTemplate.defaultTemplate != null) {
    	WebHookTemplateItem templateItemConfig = buildDeafultTemplateItem(newTemplate.defaultTemplate);
    	template.setDefaultTemplate(templateItemConfig.getTemplateText());
    	template.setDefaultBranchTemplate(templateItemConfig.getBranchTemplateText());
    }
    
    if (newTemplate.getTemplates() != null && newTemplate.getTemplates().size() > 0) {
    	WebHookTemplateItems templates = template.getTemplates();
    	templates.setMaxId(0);
	    for (TemplateItem templateItem : newTemplate.getTemplates()) {
	  	  	WebHookTemplateItem templateItemConfig = buildTemplateItem(templateItem, templates);
	  	  	templates.addTemplateItem(templateItemConfig);
	    }
    }    
    
    myTemplateManager.registerTemplateFormatFromXmlConfig(template);
    if (myTemplateManager.persistAllXmlConfigTemplates()){
    	return new Template(new WebHookTemplateConfigWrapper(template, myTemplateManager.getTemplateState(newTemplate.id), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
    } else {
    	throw new OperationException("There was an error saving your template. Sorry.");
    }
  }
  
  @PUT
  @Path("/{templateLocator}")
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Template updateTemplate(@PathParam("templateLocator") String templateLocator, Template newTemplate) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  if (templateConfigWrapper.getTemplateConfig() == null){
		  throw new NotFoundException("No template found by that name/id");
	  }
	  
	  ErrorResult validationResult = myTemplateValidator.validateTemplate(templateConfigWrapper.getTemplateConfig(), newTemplate, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("Template contained invalid data", validationResult);
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
	  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  return new Template(new WebHookTemplateConfigWrapper(template, myTemplateManager.getTemplateState(newTemplate.id), WebHookTemplateStates.build(template)), Fields.LONG, myBeanContext);
	  } else {
		  throw new OperationException("There was an error saving your template. Sorry.");
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
		  throw new NotFoundException("No template found by that name/id");
	  }
	  // The above will throw errors if the template is not found, so let's attempt to update it.
	  if (webHookTemplateConfig.getId().equals(rawConfig.getId())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(rawConfig);
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  	return myTemplateManager.getTemplateConfig(rawConfig.getId());
		  } else {
		   	throw new OperationException("There was an error saving your template. Sorry.");
		  }
	  }
	  throw new OperationException("The template name in the payload did not match the template name in the URL.");
  }
  
  @PUT
  @Path("/{templateLocator}/fullConfig")
  @Produces({"text/plain"})
  @Consumes({"text/plain"})
  public WebHookTemplateConfig updateFullConfigTemplateInPlainText(@PathParam("templateLocator") String templateLocator,  String rawConfig) {
	  checkTemplateWritePermission();
	  return myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
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
		  throw new NotFoundException("No template found by that name/id");
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateProvided()) {
		  throw new OperationException("You cannot delete a tcWebHooks provided template. If you want to make a template unavailable, please mark it as disabled.");
	  }
	  if (webHookTemplateConfigWrapper.getStatus().isStateUnknown()) {
		  throw new OperationException("You cannot delete a tcWebHooks template in an unknown state. Please report this as a bug against the tcPlugins/tcWebHooks project on GitHub.");
	  }
	  if (myTemplateManager.removeXmlConfigTemplateFormat(webHookTemplateConfigWrapper.getTemplateConfig().getId())) {
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
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
		  throw new NotFoundException("No template found by that name/id");
	  }
	  if(templateType.equals("defaultTemplate")){
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
	  throw new BadRequestException("Sorry. It was not possible to process your request for template content.");
  }
  
  @PUT
  @Path("/{templateLocator}/{templateType}/templateContent")
  @Consumes({"text/plain"})
  @Produces({"text/plain"})
  public String updateTemplateContent(@PathParam("templateLocator") String templateLocator, @PathParam("templateType") String templateType, String templateText) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfig template = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getTemplateConfig();
	  if (template == null){
		  throw new NotFoundException("No template found by that name/id");
	  }
	  if(templateType.equals("defaultTemplate")){
		  WebHookTemplateText defaultTemplateText = template.getDefaultTemplate();
		  if (defaultTemplateText != null){
			  defaultTemplateText.setTemplateContent(templateText);
		  } else {
			  defaultTemplateText = new WebHookTemplateConfig.WebHookTemplateText(templateText);
		  }
		  template.setDefaultTemplate(defaultTemplateText);
		  
		  myTemplateManager.registerTemplateFormatFromXmlConfig(template);
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  	return template.getDefaultTemplate().getTemplateContent();
		  } else {
		   	throw new OperationException("There was an error saving your template. Sorry.");
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
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  	return template.getDefaultBranchTemplate().getTemplateContent();
		  } else {
		   	throw new OperationException("There was an error saving your template. Sorry.");
		  }
		  
	  } 
	  
	  // TOOO: Need to handle 
	  
	  throw new BadRequestException("Sorry. It was not possible to process your request for template content.");
  }


  @GET
  @Path("/{templateLocator}/templateItem/{templateItemId}/{templateContentType}")
  @Produces({"text/plain"})
  public String serveSpecificTemplateContent(@PathParam("templateLocator") String templateLocator, 
		  									 @PathParam("templateItemId") String templateItemId, 
		  									 @PathParam("templateContentType") String templateContentType) {
	  checkTemplateReadPermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template == null){
		  throw new NotFoundException("No template item found by that name/id");
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
	  throw new BadRequestException("Sorry. It was not possible to process your request for template content.");
  }
  
  /**
   * /webhooks/templates/id:elasticsearch/templateItem/id:1
   * /webhooks/templates/id:elasticsearch/templateItem/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItem/id:defaultTemplate
   */
  @GET
  @Path("/{templateLocator}/templateItem/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public TemplateItem serveTemplateItem(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @QueryParam("fields") String fields) {
	  checkTemplateReadPermission();
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  if("defaultTemplate".equals(template.getTemplateItem().getId())) {
		  return new TemplateItem(templateConfig, template.getTemplateItem().getTemplateText(), template.getTemplateItem().getBranchTemplateText(), template.getTemplateItem().getId(), new Fields(fields), myBeanContext);
	  }
	  return new TemplateItem(templateConfig, template.getTemplateItem(), template.getTemplateItem().getId().toString(), new Fields(fields), myBeanContext);
  }
  
  /**
   * /webhooks/templates/id:elasticsearch/templateItem/id:1
   * /webhooks/templates/id:elasticsearch/templateItem/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItem/id:defaultTemplate
   */
  @DELETE
  @Path("/{templateLocator}/templateItem/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public void deleteTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId,
		  @QueryParam("fields") String fields) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  if("defaultTemplate".equals(template.getTemplateItem().getId())) {
		  templateConfig.getTemplateConfig().setDefaultTemplate(null);
		  templateConfig.getTemplateConfig().setDefaultBranchTemplate(null);
	  } else {
		  templateConfig.getTemplateConfig().getTemplates().deleteTemplateItem(Integer.valueOf(template.getTemplateItem().getId()));
	  }
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig.getTemplateConfig());
	  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  return;
	  } else {
	   	throw new OperationException("There was an error saving your template. Sorry.");
	  }
  }
  
  /**
   * /webhooks/templates/id:elasticsearch/templateItem/id:1
   * /webhooks/templates/id:elasticsearch/templateItem/defaultTemplate
   * /webhooks/templates/id:elasticsearch/templateItem/id:defaultTemplate
   */
  @PUT
  @Path("/{templateLocator}/templateItem/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public TemplateItem updateTemplateItem(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId, @QueryParam("fields") String fields, TemplateItem templateItem) {
	  checkTemplateWritePermission();
	  WebHookTemplateConfigWrapper templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  WebHookTemplateItemConfigWrapper templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (templateItemConfigWrapper.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  if("defaultTemplate".equals(templateItemConfigWrapper.getTemplateItem().getId())) {
		  
		  TemplateItem previousDefaultTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem().getTemplateText(), templateItemConfigWrapper.getTemplateItem().getBranchTemplateText(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext);
		  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousDefaultTemplateItem, templateItem, new ErrorResult());
		  if (validationResult.isErrored()) {
			  throw new UnprocessableEntityException("TemplateItem contained invalid data", validationResult);
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
		  if (templateItem.getBranchTemplateText() != null) {
			  if (templateItem.getBranchTemplateText().getContent() != null) {
				  templateConfig.getDefaultBranchTemplate().setTemplateContent(templateItem.getBranchTemplateText().getContent());
			  }
		  }
		  
		  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
			  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
			  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
			  return new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem().getTemplateText(), templateItemConfigWrapper.getTemplateItem().getBranchTemplateText(), templateItemConfigWrapper.getTemplateItem().getId(), new Fields(fields), myBeanContext);
		  } else {
		   	throw new OperationException("There was an error saving your template. Sorry.");
		  }
		  
	  }
	  TemplateItem previousTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId().toString(), new Fields(fields), myBeanContext);
	  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousTemplateItem, templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("TemplateItem contained invalid data", validationResult);
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
	  if (templateItem.getBranchTemplateText() != null) {
		  if (templateItem.getBranchTemplateText().getContent() != null) {
			  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
		  }
	  }
	  if (templateItem.getStates() != null) {
		  templateItemConfig.getStates().clear();
		  
		  for (WebHookTemplateStateRest itemState : templateItem.getStates()) {
					
				if (itemState != null && itemState.isEnabled()) {
					templateItemConfig.getStates().add(new WebHookTemplateState(itemState.getType(), itemState.isEnabled()));
				}
		  }
	  }
	  
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
		  return new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId().toString(), new Fields(fields), myBeanContext);
	  } else {
	   	throw new OperationException("There was an error saving your template. Sorry.");
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
		  throw new NotFoundException("No template found by that name/id");
	  }
	  
	  TemplateItem previousTemplateItem = new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId().toString(), new Fields(fields), myBeanContext);
	  final ErrorResult validationResult = myTemplateValidator.validateTemplateItem(previousTemplateItem, templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("TemplateItem contained invalid data", validationResult);
	  }
	  
	  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();
	  
	  WebHookTemplateItem templateItemConfig = buildTemplateItem(templateItem, templateConfig.getTemplates());
	  
	  templateConfig.getTemplates().getTemplates().add(templateItemConfig);
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemConfig.getId().toString());
		  return Response.status(201)
				  .header(
						  "Location",
						  myBeanContext.getApiUrlBuilder().getTemplateItemHref(templateConfig, templateItemConfigWrapper.getTemplateItem())
						  )
				  .entity(new TemplateItem(templateConfigWrapper, templateItemConfigWrapper.getTemplateItem(), templateItemConfigWrapper.getTemplateItem().getId().toString(), new Fields(fields), myBeanContext))
				  .build();
	  } else {
		  throw new OperationException("There was an error saving your template. Sorry.");
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
	  if (templateItem.getBranchTemplateText() != null) {
		  if (templateItem.getBranchTemplateText().getContent() != null) {
			  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
		  }
	  }
	  if (templateItem.getStates() != null) {
		  templateItemConfig.getStates().clear();
		  
		  for (WebHookTemplateStateRest itemState : templateItem.getStates()) {
			  
			  if (itemState != null && itemState.isEnabled()) {
				  templateItemConfig.getStates().add(new WebHookTemplateState(itemState.getType(), itemState.isEnabled()));
			  }
		  }
	  }
	return templateItemConfig;
}
  
  /**
   *  /app/rest/webhooks/templates/id:flowdock/templateItem/id:2/buildState/buildStarted
   *  							  /id:elasticsearch/templateItem/id:1/buildState/buildStarted
   *  							  /id:elasticsearch/templateItem/defaultTemplate/buildState/buildStarted
   */
  @GET
  @Path("/{templateLocator}/templateItem/{templateItemId}/buildState/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest serveTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @PathParam("buildState") String buildState,
		  											 @QueryParam("fields") String fields) {
	  checkTemplateReadPermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  if ("defaultTemplate".equals(template.getTemplateItem().getId())){
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
  @Path("/{templateLocator}/templateItem/{templateItemId}/buildState/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest updateTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId,
		  @PathParam("buildState") String buildState,
		  WebHookTemplateStateRest updatedBuildState) {
	  checkTemplateWritePermission();
	  WebHookTemplateItemConfigWrapper template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template.getTemplateItem() == null){
		  throw new NotFoundException("No template item found by that name/id");
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
		  throw new NotFoundException("No template found by that name/id");
	  }
	  
	  if (templateConfigWrapper.getTemplateConfig().getDefaultTemplate() != null){
		  throw new BadRequestException("Default Template Item already exists. To update existing default template, please use PUT");
	  }

	  final ErrorResult validationResult = myTemplateValidator.validateDefaultTemplateItem(templateItem, new ErrorResult());
	  if (validationResult.isErrored()) {
		  throw new UnprocessableEntityException("DefaultTemplateItem contained invalid data", validationResult);
	  }
	  
	  WebHookTemplateConfig templateConfig = templateConfigWrapper.getTemplateConfig();
	  
	  WebHookTemplateItem templateItemConfig = buildDeafultTemplateItem(templateItem);
	  
	  templateConfig.setDefaultTemplate(templateItemConfig.getTemplateText());
	  templateConfig.setDefaultBranchTemplate(templateItemConfig.getBranchTemplateText());
	  
	  myTemplateManager.registerTemplateFormatFromXmlConfig(templateConfig);
	  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  templateConfigWrapper = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
		  WebHookTemplateItemConfigWrapper templateItemConfigWrapper = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, "defaultTemplate");
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
		  throw new OperationException("There was an error saving your template. Sorry.");
	  }
	  
  }

	private WebHookTemplateItem buildDeafultTemplateItem(TemplateItem templateItem) {
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
		  if (templateItem.getBranchTemplateText() != null) {
			  if (templateItem.getBranchTemplateText().getContent() != null) {
				  templateItemConfig.getBranchTemplateText().setTemplateContent(templateItem.getBranchTemplateText().getContent());
			  }
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
  /*
  @POST
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Template createTemplate(NewTemplateDescription descriptor) {
    if (StringUtil.isEmpty(descriptor.name)) {
      throw new BadRequestException("Template name cannot be empty.");
    }
    
    final webhook.teamcity.settings.entity.WebHookTemplate entityTemplate = new webhook.teamcity.settings.entity.WebHookTemplate(descriptor.name, false);
    
    WebHookTemplateFromXml resultingTemplate;
    
    resultingTemplate = WebHookTemplateFromXml.`
    @Nullable SProject sourceProject = descriptor. getSourceProject(myServiceLocator);
    final ProjectManager projectManager = myDataProvider.getServer().getProjectManager();
    final SProject parentProject = descriptor.getParentProject(myServiceLocator);
    if (sourceProject == null) {
      resultingProject = parentProject.createProject(descriptor.getId(myServiceLocator), descriptor.name);
    } else {
      final CopyOptions copyOptions = descriptor.getCopyOptions();
      //see also getExampleNewProjectDescription which prepares NewProjectDescription
      copyOptions.addProjectExternalIdMapping(Collections.singletonMap(sourceProject.getExternalId(), descriptor.getId(myServiceLocator)));
      copyOptions.setGenerateExternalIdsBasedOnOriginalExternalIds(ID_GENERATION_FLAG);
      if (descriptor.name != null) copyOptions.setNewProjectName(descriptor.name);
      try {
        resultingProject = projectManager.copyProject(sourceProject, parentProject, copyOptions);
      } catch (MaxNumberOfBuildTypesReachedException e) {
        throw new BadRequestException("Build configurations number limit is reached", e);
      } catch (NotAllIdentifiersMappedException e) {
        throw new BadRequestException("Not all ids are mapped", e);
      } catch (InvalidNameException e) {
        throw new BadRequestException("Invalid name", e);
      } catch (DuplicateExternalIdException e) {
        throw new BadRequestException("Duplicate id", e);
      }
      try {
        if (descriptor.name != null) resultingProject.setName(descriptor.name);
        //todo: TeamCity api: is this necessary? http://youtrack.jetbrains.com/issue/TW-28495
        resultingProject.setExternalId(descriptor.getId(myServiceLocator));
      } catch (InvalidIdentifierException e) {
        processCreatiedProjectFinalizationError(resultingProject, projectManager, e);
      } catch (DuplicateExternalIdException e) {
        processCreatiedProjectFinalizationError(resultingProject, projectManager, e);
      }
    }

    try {
      resultingProject.persist();
    } catch (PersistFailedException e) {
      processCreatiedProjectFinalizationError(resultingProject, projectManager, e);
    }
    return new Project(resultingProject, Fields.LONG, myBeanContext);
  }

  private void processCreatiedProjectFinalizationError(final SProject resultingProject, final ProjectManager projectManager, final Exception e) {
    try {
      projectManager.removeProject(resultingProject.getProjectId());
    } catch (ProjectRemoveFailedException e1) {
      LOG.warn("Rallback of project creation failed", e1);
      //ignore
    }
    throw new InvalidStateException("Error during project creation finalization", e);
  }

  @GET
  @Path("/{projectLocator}")
  @Produces({"application/xml", "application/json"})
  public Project serveProject(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    return new Project(myProjectFinder.getProject(projectLocator),  new Fields(fields), myBeanContext);
  }

  @DELETE
  @Path("/{projectLocator}")
  public void deleteProject(@PathParam("projectLocator") String projectLocator) {
    final SProject project = myProjectFinder.getProject(projectLocator);
    myDataProvider.getServer().getProjectManager().removeProject(project.getProjectId());
  }

  @GET
  @Path("/{projectLocator}/{field}")
  @Produces("text/plain")
  public String serveProjectField(@PathParam("projectLocator") String projectLocator, @PathParam("field") String fieldName) {
    return Project.getFieldValue(myProjectFinder.getProject(projectLocator), fieldName);
  }

  @PUT
  @Path("/{projectLocator}/{field}")
  @Consumes("text/plain")
  @Produces("text/plain")
  public String setProjectFiled(@PathParam("projectLocator") String projectLocator, @PathParam("field") String fieldName, String newValue) {
    final SProject project = myProjectFinder.getProject(projectLocator);
    Project.setFieldValueAndPersist(project, fieldName, newValue, myDataProvider);
    return Project.getFieldValue(project, fieldName);
  }

  @GET
  @Path("/{projectLocator}/buildTypes")
  @Produces({"application/xml", "application/json"})
  public BuildTypes serveBuildTypesInProject(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return new BuildTypes(BuildTypes.fromBuildTypes(project.getOwnBuildTypes()), null, new Fields(fields), myBeanContext);
  }

  @POST
  @Path("/{projectLocator}/buildTypes")
  @Produces({"application/xml", "application/json"})
  @Consumes({"text/plain"})
  public BuildType createEmptyBuildType(@PathParam("projectLocator") String projectLocator, String name, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    if (StringUtil.isEmpty(name)) {
      throw new BadRequestException("Build type name cannot be empty.");
    }
    final SBuildType buildType = project.createBuildType(name);
    buildType.persist();
    return new BuildType(new BuildTypeOrTemplate(buildType),  new Fields(fields), myBeanContext);
  }
*/
  /**
   * Creates a new build configuration by copying existing one.
   *
   * @param projectLocator
   * @param descriptor     reference to the build configuration to copy and copy options.
   *                       e.g. <newBuildTypeDescription name='Conf Name' id='ProjectId_ConfId' copyAllAssociatedSettings='true'><sourceBuildType id='sourceConfId'/></newBuildTypeDescription>
   * @return the build configuration created
   */
  
/*
  @POST
  @Path("/{projectLocator}/buildTypes")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public BuildType createBuildType(@PathParam("projectLocator") String projectLocator, NewBuildTypeDescription descriptor, @QueryParam("fields") String fields) {
    @NotNull SProject project = myProjectFinder.getProject(projectLocator);
    SBuildType resultingBuildType;
    @Nullable final BuildTypeOrTemplate sourceBuildType = descriptor.getSourceBuildTypeOrTemplate(myServiceLocator);
    if (sourceBuildType == null) {
      resultingBuildType = project.createBuildType(descriptor.getId(myServiceLocator, project), descriptor.getName());
    } else {
      if (sourceBuildType.isBuildType()) {
        resultingBuildType =
          project.copyBuildType(sourceBuildType.getBuildType(), descriptor.getId(myServiceLocator, project), descriptor.getName(), descriptor.getCopyOptions());
      } else {
        throw new BadRequestException("Could not create build type as a copy of a template.");
      }
    }
    resultingBuildType.persist();
    return new BuildType(new BuildTypeOrTemplate(resultingBuildType),  new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{projectLocator}/buildTypes/{btLocator}")
  @Produces({"application/xml", "application/json"})
  public BuildType serveBuildType(@PathParam("projectLocator") String projectLocator, @PathParam("btLocator") String buildTypeLocator, @QueryParam("fields") String fields) {
    SBuildType buildType = myBuildTypeFinder.getBuildType(myProjectFinder.getProject(projectLocator), buildTypeLocator);
    return new BuildType(new BuildTypeOrTemplate(buildType),  new Fields(fields), myBeanContext);
  }


  @GET
  @Path("/{projectLocator}/templates")
  @Produces({"application/xml", "application/json"})
  public BuildTypes serveTemplatesInProject(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return new BuildTypes(BuildTypes.fromTemplates(project.getOwnBuildTypeTemplates()), null, new Fields(fields), myBeanContext);
  }

  @POST
  @Path("/{projectLocator}/templates")
  @Produces({"application/xml", "application/json"})
  @Consumes({"text/plain"})
  public BuildType createEmptyBuildTypeTemplate(@PathParam("projectLocator") String projectLocator, String name, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    if (StringUtil.isEmpty(name)) {
      throw new BadRequestException("Build type template name cannot be empty.");
    }
    final BuildTypeTemplate buildType = project.createBuildTypeTemplate(name);
    buildType.persist();
    return new BuildType(new BuildTypeOrTemplate(buildType),  new Fields(fields), myBeanContext);
  }
*/
  /**
   * Creates a new build configuration template by copying existing one.
   *
   * @param projectLocator
   * @param descriptor     reference to the build configuration template to copy and copy options.
   *                       e.g. <newBuildTypeDescription name='Conf Name' id='ProjectId_ConfId' copyAllAssociatedSettings='true'><sourceBuildType id='sourceConfId'/></newBuildTypeDescription>
   * @return the build configuration created
   */
/*  
  @POST
  @Path("/{projectLocator}/templates")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public BuildType createBuildTypeTemplate(@PathParam("projectLocator") String projectLocator, NewBuildTypeDescription descriptor, @QueryParam("fields") String fields) {
    @NotNull SProject project = myProjectFinder.getProject(projectLocator);
    BuildTypeTemplate resultingBuildType;
    @Nullable final BuildTypeOrTemplate sourceBuildType = descriptor.getSourceBuildTypeOrTemplate(myServiceLocator);
    if (sourceBuildType == null) {
      resultingBuildType = project.createBuildTypeTemplate(descriptor.getId(myServiceLocator, project), descriptor.getName());
    } else {
      if (sourceBuildType.isBuildType()) {
        resultingBuildType =
          project.extractBuildTypeTemplate(sourceBuildType.getBuildType(), descriptor.getId(myServiceLocator, project), descriptor.getName());
      } else {
        resultingBuildType =
          project.copyBuildTypeTemplate(sourceBuildType.getTemplate(), descriptor.getId(myServiceLocator, project), descriptor.getName());
      }
    }
    resultingBuildType.persist();
    return new BuildType(new BuildTypeOrTemplate(resultingBuildType),  new Fields(fields), myBeanContext);
  }


  @GET
  @Path("/{projectLocator}/templates/{btLocator}")
  @Produces({"application/xml", "application/json"})
  public BuildType serveBuildTypeTemplates(@PathParam("projectLocator") String projectLocator, @PathParam("btLocator") String buildTypeLocator, @QueryParam("fields") String fields) {
    BuildTypeTemplate buildType = myBuildTypeFinder.getBuildTemplate(myProjectFinder.getProject(projectLocator), buildTypeLocator);
    return new BuildType(new BuildTypeOrTemplate(buildType),  new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{projectLocator}" + PARAMETERS)
  @Produces({"application/xml", "application/json"})
  public Properties serveParameters(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return new Properties(project.getParametersCollection(), project.getOwnParametersCollection(), getParametersHref(project),
                           new Fields(fields), myServiceLocator);
  }

  @PUT
  @Path("/{projectLocator}" + PARAMETERS)
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Properties changeAllParameters(@PathParam("projectLocator") String projectLocator, Properties properties, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    BuildTypeUtil.removeAllParameters(project);
    for (Parameter p : properties.getFromPosted(myServiceLocator)) {
      project.addParameter(p);
    }
    project.persist();
    return new Properties(project.getParametersCollection(), project.getOwnParametersCollection(), getParametersHref(project),
                           new Fields(fields), myServiceLocator);
  }

  @POST
  @Path("/{projectLocator}" + PARAMETERS)
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Property setParameter(@PathParam("projectLocator") String projectLocator, Property parameter) {
    SProject project = myProjectFinder.getProject(projectLocator);
    project.addParameter(parameter.getFromPosted(myServiceLocator));
    project.persist();
    return Property.createFrom(parameter.name, project, Fields.LONG, myServiceLocator);
  }

  @DELETE
  @Path("/{projectLocator}" + PARAMETERS)
  public void deleteAllParameters(@PathParam("projectLocator") String projectLocator) {
    SProject project = myProjectFinder.getProject(projectLocator);
    BuildTypeUtil.removeAllParameters(project);
    project.persist();
  }

  @GET
  @Path("/{projectLocator}" + PARAMETERS + "/{name}")
  @Produces("text/plain")
  public String getParameterValue(@PathParam("projectLocator") String projectLocator, @PathParam("name") String parameterName) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return BuildTypeUtil.getParameter(parameterName, project, true, false);
  }

  @GET
  @Path("/{projectLocator}" + PARAMETERS + "/{name}")
  @Produces({"application/xml", "application/json"})
  public Property getParameter(@PathParam("projectLocator") String projectLocator, @PathParam("name") String parameterName) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return Property.createFrom(parameterName, project, Fields.LONG, myServiceLocator);
  }

  @GET
  @Path("/{projectLocator}" + PARAMETERS + "/{name}/value")
  @Produces("text/plain")
  public String getParameterValueLong(@PathParam("projectLocator") String projectLocator, @PathParam("name") String parameterName) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return BuildTypeUtil.getParameter(parameterName, project, true, false);
  }

  @PUT
  @Path("/{projectLocator}" + PARAMETERS + "/{name}")
  @Consumes("text/plain")
  @Produces("text/plain")
  public String putParameter(@PathParam("projectLocator") String projectLocator, @PathParam("name") String parameterName, String newValue) {
    SProject project = myProjectFinder.getProject(projectLocator);
    BuildTypeUtil.changeParameter(parameterName, newValue, project, myServiceLocator);
    project.persist();
    return BuildTypeUtil.getParameter(parameterName, project, false, false);
  }

  @DELETE
  @Path("/{projectLocator}" + PARAMETERS + "/{name}")
  public void deleteParameter(@PathParam("projectLocator") String projectLocator, @PathParam("name") String parameterName) {
    SProject project = myProjectFinder.getProject(projectLocator);
    BuildTypeUtil.deleteParameter(parameterName, project);
    project.persist();
  }


  @GET
  @Path("/{projectLocator}/buildTypes/{btLocator}/{field}")
  @Produces("text/plain")
  public String serveBuildTypeFieldWithProject(@PathParam("projectLocator") String projectLocator,
                                               @PathParam("btLocator") String buildTypeLocator,
                                               @PathParam("field") String fieldName) {
    BuildTypeOrTemplate buildType = myBuildTypeFinder.getBuildTypeOrTemplate(myProjectFinder.getProject(projectLocator), buildTypeLocator);

    return buildType.getFieldValue(fieldName);
  }
*/
  //todo: separate methods to serve running builds

  /**
   * Serves builds matching supplied condition.
   *
   * @param locator           Build locator to filter builds
   * @param buildTypeLocator  Deprecated, use "locator" parameter instead
   * @param status            Deprecated, use "locator" parameter instead
   * @param userLocator       Deprecated, use "locator" parameter instead
   * @param includePersonal   Deprecated, use "locator" parameter instead
   * @param includeCanceled   Deprecated, use "locator" parameter instead
   * @param onlyPinned        Deprecated, use "locator" parameter instead
   * @param tags              Deprecated, use "locator" parameter instead
   * @param agentName         Deprecated, use "locator" parameter instead
   * @param sinceBuildLocator Deprecated, use "locator" parameter instead
   * @param sinceDate         Deprecated, use "locator" parameter instead
   * @param start             Deprecated, use "locator" parameter instead
   * @param count             Deprecated, use "locator" parameter instead, defaults to 100
   * @return
   */
/*  
  @GET
  @Path("/{projectLocator}/buildTypes/{btLocator}/builds")
  @Produces({"application/xml", "application/json"})
  public Builds serveBuilds(@PathParam("projectLocator") String projectLocator,
                            @PathParam("btLocator") String buildTypeLocator,
                            @QueryParam("status") String status,
                            @QueryParam("triggeredByUser") String userLocator,
                            @QueryParam("includePersonal") boolean includePersonal,
                            @QueryParam("includeCanceled") boolean includeCanceled,
                            @QueryParam("onlyPinned") boolean onlyPinned,
                            @QueryParam("tag") List<String> tags,
                            @QueryParam("agentName") String agentName,
                            @QueryParam("sinceBuild") String sinceBuildLocator,
                            @QueryParam("sinceDate") String sinceDate,
                            @QueryParam("start") Long start,
                            @QueryParam("count") Integer count,
                            @QueryParam("locator") String locator,
                            @QueryParam("fields") String fields,
                            @Context UriInfo uriInfo, @Context HttpServletRequest request) {
    SBuildType buildType = myBuildTypeFinder.getBuildType(myProjectFinder.getProject(projectLocator), buildTypeLocator);
    return myBuildFinder.getBuildsForRequest(buildType, status, userLocator, includePersonal, includeCanceled, onlyPinned, tags, agentName,
                                             sinceBuildLocator, sinceDate, start, count, locator, "locator", uriInfo, request,  new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{projectLocator}/buildTypes/{btLocator}/builds/{buildLocator}")
  @Produces({"application/xml", "application/json"})
  public Build serveBuildWithProject(@PathParam("projectLocator") String projectLocator,
                                     @PathParam("btLocator") String buildTypeLocator,
                                     @PathParam("buildLocator") String buildLocator,
                                     @QueryParam("fields") String fields) {
    SBuildType buildType = myBuildTypeFinder.getBuildType(myProjectFinder.getProject(projectLocator), buildTypeLocator);
    SBuild build = myBuildFinder.getBuild(buildType, buildLocator);

    return new Build(build,  new Fields(fields), myBeanContext);
  }

  @GET
  @Path("/{projectLocator}/buildTypes/{btLocator}/builds/{buildLocator}/{field}")
  @Produces("text/plain")
  public String serveBuildFieldWithProject(@PathParam("projectLocator") String projectLocator,
                                           @PathParam("btLocator") String buildTypeLocator,
                                           @PathParam("buildLocator") String buildLocator,
                                           @PathParam("field") String field) {
    SBuildType buildType = myBuildTypeFinder.getBuildType(myProjectFinder.getProject(projectLocator), buildTypeLocator);
    SBuild build = myBuildFinder.getBuild(buildType, buildLocator);

    return Build.getFieldValue(build.getBuildPromotion(), field, myBeanContext);
  }

  @GET
  @Path("/{projectLocator}/parentProject")
  @Produces({"application/xml", "application/json"})
  public Project getParentProject(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    final SProject actulParentProject = project.getParentProject();
    return actulParentProject == null
           ? null
           : new Project(actulParentProject,  new Fields(fields), myBeanContext);
  }

  @PUT
  @Path("/{projectLocator}/parentProject")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public Project setParentProject(@PathParam("projectLocator") String projectLocator, Project parentProject) {
    SProject project = myProjectFinder.getProject(projectLocator);
    project.moveToProject(parentProject.getProjectFromPosted(myProjectFinder));
    project.persist();
    return new Project(project, Fields.LONG, myBeanContext);
  }

  @GET
  @Path("/{projectLocator}/agentPools")
  @Produces({"application/xml", "application/json"})
  public AgentPools getProjectAgentPools(@PathParam("projectLocator") String projectLocator, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    return new AgentPools(myAgentPoolsFinder.getPoolsForProject(project), null, new Fields(fields), myBeanContext);
  }

  @DELETE
  @Path("/{projectLocator}/agentPools/{agentPoolLocator}")
  public void deleteProjectAgentPools(@PathParam("projectLocator") String projectLocator, @PathParam("agentPoolLocator") String agentPoolLocator) {
    SProject project = myProjectFinder.getProject(projectLocator);
    final jetbrains.buildServer.serverSide.agentPools.AgentPool agentPool = myAgentPoolsFinder.getAgentPool(agentPoolLocator);
    final AgentPoolManager agentPoolManager = myServiceLocator.getSingletonService(AgentPoolManager.class);
    final int agentPoolId = agentPool.getAgentPoolId();
    try {
      agentPoolManager.dissociateProjectsFromPool(agentPoolId, Collections.singleton(project.getProjectId()));
    } catch (NoSuchAgentPoolException e) {
      throw new IllegalStateException("Agent pool with id \'" + agentPoolId + "' is not found.");
    }
  }

  @PUT
  @Path("/{projectLocator}/agentPools")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public AgentPools setProjectAgentPools(@PathParam("projectLocator") String projectLocator, AgentPools pools, @QueryParam("fields") String fields) {
    SProject project = myProjectFinder.getProject(projectLocator);
    myDataProvider.setProjectPools(project, pools.getPoolsFromPosted(myAgentPoolsFinder));
    return new AgentPools(myAgentPoolsFinder.getPoolsForProject(project), null, new Fields(fields), myBeanContext);
  }

  @POST
  @Path("/{projectLocator}/agentPools")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public AgentPool setProjectAgentPools(@PathParam("projectLocator") String projectLocator, AgentPool pool) {
    SProject project = myProjectFinder.getProject(projectLocator);
    final AgentPoolManager agentPoolManager = myServiceLocator.getSingletonService(AgentPoolManager.class);
    final jetbrains.buildServer.serverSide.agentPools.AgentPool agentPoolFromPosted = pool.getAgentPoolFromPosted(myAgentPoolsFinder);
    final int agentPoolId = agentPoolFromPosted.getAgentPoolId();
    try {
      agentPoolManager.associateProjectsWithPool(agentPoolId, Collections.singleton(project.getProjectId()));
    } catch (NoSuchAgentPoolException e) {
      throw new IllegalStateException("Agent pool with id \'" + agentPoolId + "' is not found.");
    }
    return new AgentPool(agentPoolFromPosted, Fields.LONG, myBeanContext);
  }
*/
  /**
   * For compatibility with experimental feature of 8.0
   */ /*
  @GET
  @Path("/{projectLocator}/newProjectDescription")
  @Produces({"application/xml", "application/json"})
  public NewProjectDescription getExampleNewProjectDescriptionCompatibilityVersion1(@PathParam("projectLocator") String projectLocator, @QueryParam("id") String newId) {
    return getExampleNewProjectDescription(projectLocator, newId);
  }

*/  /**
   * Experimental support only.
   * Use this to get an example of the bean to be posted to the /projects request to create a new project
   *
   * @param projectLocator
   * @return
   */ /*
  @GET
  @Path("/{projectLocator}/example/newProjectDescription")
  @Produces({"application/xml", "application/json"})
  public NewProjectDescription getExampleNewProjectDescription(@PathParam("projectLocator") String projectLocator, @QueryParam("id") String newId) {
    final SProject project = myProjectFinder.getProject(projectLocator);
    final SProject parentProject = project.getParentProject();
    final Project parentProjectRef =
      parentProject != null ? new Project(parentProject, Fields.SHORT, myBeanContext) : null;
    @NotNull final String newNotEmptyId = StringUtil.isEmpty(newId) ? project.getExternalId() : newId;
    final ProjectManagerEx.IdsMaps idsMaps =
      ((ProjectManagerEx)myDataProvider.getServer().getProjectManager()).generateDefaultExternalIds(project, newNotEmptyId, ID_GENERATION_FLAG, true);
    final Map<String, String> projectIdsMap = idsMaps.getProjectIdsMap();
    projectIdsMap.remove(project.getExternalId()); // remove ptoject's own id to make the object more clean
    return new NewProjectDescription(project.getName(), newNotEmptyId, new Project(project, Fields.SHORT, myBeanContext),
                                     parentProjectRef, true,
                                     getNullOrCollection(projectIdsMap),
                                     getNullOrCollection(idsMaps.getBuildTypeIdsMap()),
                                     getNullOrCollection(idsMaps.getVcsRootIdsMap()));
  }


*/  /**
   * Experimental support only
   */ /*
  @GET
  @Path("/{projectLocator}/settingsFile")
  @Produces({"text/plain"})
  public String getSettingsFile(@PathParam("projectLocator") String projectLocator) {
    myPermissionChecker.checkGlobalPermission(Permission.CHANGE_SERVER_SETTINGS);
    final SProject project = myProjectFinder.getProject(projectLocator);
    return project.getConfigurationFile().getAbsolutePath();
  }

  @Nullable
  private Map<String, String> getNullOrCollection(final @NotNull Map<String, String> map) {
    return map.size() > 0 ? map : null;
  }
  */
}

