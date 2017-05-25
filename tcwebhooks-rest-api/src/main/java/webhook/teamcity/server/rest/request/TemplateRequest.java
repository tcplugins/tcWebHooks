package webhook.teamcity.server.rest.request;

import com.intellij.openapi.diagnostic.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.InvalidStateException;
import jetbrains.buildServer.server.rest.errors.OperationException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.model.Properties;
import jetbrains.buildServer.server.rest.model.Property;
import jetbrains.buildServer.server.rest.util.BuildTypeOrTemplate;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.identifiers.DuplicateExternalIdException;
import jetbrains.buildServer.util.StringUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.model.template.NewTemplateDescription;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

@Path(TemplateRequest.API_TEMPLATES_URL)
public class TemplateRequest {
  private static final Logger LOG = Logger.getInstance(TemplateRequest.class.getName());
  public static final boolean ID_GENERATION_FLAG = true;

  @Context @NotNull private DataProvider myDataProvider;
  @Context @NotNull private WebHookTemplateManager myTemplateManager;
  
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
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/defaultTemplate/templateContent" ;
  }
  
  @NotNull
  public static String getDefaultBranchTemplateTextHref(WebHookTemplateConfig template) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/defaultBranchTemplate/templateContent" ;
  }
  
  @NotNull
  public static String getTemplateItemHref(WebHookTemplateConfig template, WebHookTemplateItem webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template)+ "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString());
  }
  
  @NotNull
  public static String getTemplateItemTextHref(WebHookTemplateConfig template, WebHookTemplateItem webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template)+ "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString()) + "/templateContent" ;
  }
  
  @NotNull
  public static String getTemplateItemBranchTextHref(WebHookTemplateConfig template, WebHookTemplateItem webHookTemplateItem) {
	  return API_TEMPLATES_URL + "/" + TemplateFinder.getLocator(template) + "/templateItem/" + TemplateFinder.getTemplateTextLocator(webHookTemplateItem.getId().toString()) +  "/branchTemplateContent" ;
  }
 
  @NotNull  
  public static String getTemplateItemStateHref(WebHookTemplateConfig template,	WebHookTemplateItem templateItem, String state) {
		return getTemplateItemHref(template, templateItem) + "/buildState/" + state;
  }
  
  @GET
  @Produces({"application/xml", "application/json"})
  public Templates serveTemplates(@QueryParam("fields") String fields) {
    return new Templates(myDataProvider.getWebHookTemplates(), new PagerData(getHref()), new Fields(fields), myBeanContext);
  }
  
  @GET
  @Path("/{templateLocator}")
  @Produces({"application/xml", "application/json"})
  public Template serveTemplate(@PathParam("templateLocator") String templateLocator, @QueryParam("fields") String fields) {
	  return new Template(myDataProvider.getTemplateFinder().findTemplateById(templateLocator), new Fields(fields), myBeanContext);
  }

  @POST
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public Template createEmptyTemplate(NewTemplateDescription templateDescription) {
    if (StringUtil.isEmpty(templateDescription.getName())) {
      throw new BadRequestException("Template name cannot be empty.");
    }
    WebHookTemplateConfig template = new WebHookTemplateConfig(templateDescription.getName(), true);
    template.setTemplateDescription(templateDescription.getDescription());
    if (myTemplateManager.getTemplate(template.getName()) != null){
    	throw new BadRequestException("Template of that name already exists. To update existing template, please use PUT");
    }
    myTemplateManager.registerTemplateFormatFromXmlConfig(template);
    if (myTemplateManager.persistAllXmlConfigTemplates()){
    	return new Template(new WebHookTemplateConfigWrapper(template, myTemplateManager.getTemplateState(templateDescription.getName())), Fields.LONG, myBeanContext);
    } else {
    	throw new OperationException("There was an error saving your template. Sorry.");
    }
  }
  
  @GET
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateConfig serveFullConfigTemplate(@PathParam("templateLocator") String templateLocator) {
	  return myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getEntity();
  }
  
  @PUT
  @Path("/{templateLocator}/fullConfig")
  @Produces({"application/xml", "application/json"})
  @Consumes({"application/xml", "application/json"})
  public WebHookTemplateConfig updateFullConfigTemplate(@PathParam("templateLocator") String templateLocator,  WebHookTemplateConfig rawConfig) {
	  WebHookTemplateConfig webHookTemplateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getEntity();
	  if (webHookTemplateConfig == null){
		  throw new NotFoundException("No template found by that name/id");
	  }
	  // The above will throw errors if the template is not found, so let's attempt to update it.
	  if (webHookTemplateConfig.getName().equals(rawConfig.getName())) {
		  myTemplateManager.registerTemplateFormatFromXmlConfig(rawConfig);
		  if (myTemplateManager.persistAllXmlConfigTemplates()){
		  	return myTemplateManager.getTemplateConfig(rawConfig.getName());
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
	  return myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getEntity();
  }

  @GET
  @Path("/{templateLocator}/{templateType}/templateContent")
  @Produces({"text/plain"})
  public String serveTemplateContent(@PathParam("templateLocator") String templateLocator, @PathParam("templateType") String templateType) {
	  WebHookTemplateConfig template = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getEntity();
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
	  WebHookTemplateConfig template = myDataProvider.getTemplateFinder().findTemplateById(templateLocator).getEntity();
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
	  WebHookTemplateItem template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  if(templateContentType.equals("templateContent")){
		  if (template.getTemplateText() == null){
			  throw new NotFoundException("This template does not have a non-branch template configured.");
		  }
		  return template.getTemplateText().getTemplateContent();
	  } else if(templateContentType.equals("branchTemplateContent")){
		  if (template.getBranchTemplateText() == null){
			  throw new NotFoundException("This template does not have a  branch template configured.");
		  }
		  return template.getBranchTemplateText().getTemplateContent();
	  }
	  throw new BadRequestException("Sorry. It was not possible to process your request for template content.");
  }

  /**
   * /webhooks/templates/id:elasticsearch/templateItem/id:1
   */
  @GET
  @Path("/{templateLocator}/templateItem/{templateItemId}")
  @Produces({"application/xml", "application/json"})
  public TemplateItem serveTemplateItem(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @QueryParam("fields") String fields) {
	  WebHookTemplateConfigWrapper templateConfig = myDataProvider.getTemplateFinder().findTemplateById(templateLocator);
	  WebHookTemplateItem template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  return new TemplateItem(templateConfig.getEntity(), template, template.getId().toString(), new Fields(fields), myBeanContext);
  }

  /**
   *  /app/rest/webhooks/templates/id:flowdock/templateItem/id:2/buildState/buildStarted
   *  							  /id:elasticsearch/templateItem/id:1/buildState/buildStarted
   */
  @GET
  @Path("/{templateLocator}/templateItem/{templateItemId}/buildState/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest serveTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  											 @PathParam("templateItemId") String templateItemId,
		  											 @PathParam("buildState") String buildState,
		  											 @QueryParam("fields") String fields) {
	  WebHookTemplateItem template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  return new WebHookTemplateStateRest(template, buildState, new Fields(fields), myBeanContext);
	  
  }
  
  @PUT
  @Path("/{templateLocator}/templateItem/{templateItemId}/buildState/{buildState}")
  @Produces({"application/xml", "application/json"})
  public WebHookTemplateStateRest updateTemplateItemBuildStateSetting(@PathParam("templateLocator") String templateLocator,
		  @PathParam("templateItemId") String templateItemId,
		  @PathParam("buildState") String buildState,
		  WebHookTemplateStateRest updatedBuildState) {
	  WebHookTemplateItem template = myDataProvider.getTemplateFinder().findTemplateByIdAndTemplateContentById(templateLocator, templateItemId);
	  if (template == null){
		  throw new NotFoundException("No template item found by that name/id");
	  }
	  //for (template.get)
	  return new WebHookTemplateStateRest(template, buildState, new Fields(null), myBeanContext);
	  
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

