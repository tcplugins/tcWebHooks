package webhook.teamcity.server.rest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.util.ValueWithDefault.Value;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.server.rest.request.WebHooksRequest;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

/**
 * Adds the WebHooks urls into the resolver.
 * @author netwolfuk
 *
 */
public class WebHookApiUrlBuilder {

	private PathTransformer myPathTransformer;
	private ProjectIdResolver myProjectIdResolver;

	public WebHookApiUrlBuilder(@NotNull final PathTransformer pathTransformer, @NotNull final ProjectIdResolver projectIdResolver) {
		myPathTransformer = pathTransformer;
		myProjectIdResolver = projectIdResolver;
	}
	
	public String getHref(final WebHookTemplateConfig webHookTemplateConfig) {
	    return myPathTransformer.transform(TemplateRequest.getTemplateHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig));
	}
	
	public String getHref(String projectExternalId, WebHookConfig config) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookHref(projectExternalId, config));
	}
	
	public String getTemplateDefaultItemHref(WebHookTemplateConfig webHookTemplateConfig) {
		return myPathTransformer.transform(TemplateRequest.getTemplateDefaultItemHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig));
	}
	
	public String getTemplateItemHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig, webHookTemplateItem));
	}	
	
	public String getDefaultTemplateTextHref(final WebHookTemplateConfig webHookTemplateConfig) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig));
	}

	public String getDefaultBranchTemplateTextHref(final WebHookTemplateConfig webHookTemplateConfig) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig));
	}

	public String getTemplateItemTextHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemTextHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig, webHookTemplateItem));
	}
	
	public String getTemplateItemBranchTextHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemBranchTextHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig, webHookTemplateItem));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
		return myPathTransformer.transform(internalRelativePath);
	}

	public String getWebHookTemplateStateUrl(WebHookTemplateConfig webHookTemplateConfig, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateStateHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig, state));
	}
	
	public String getWebHookTemplateItemStateUrl(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest templateItem, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemStateHref(myProjectIdResolver.getExternalProjectId(webHookTemplateConfig.getProjectInternalId()), webHookTemplateConfig, templateItem, state));
	}
	
}
