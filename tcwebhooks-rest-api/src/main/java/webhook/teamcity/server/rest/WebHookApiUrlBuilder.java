package webhook.teamcity.server.rest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.util.ValueWithDefault.Value;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.server.rest.request.WebHookParametersRequest;
import webhook.teamcity.server.rest.request.WebHooksRequest;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.project.WebHookParameter;

/**
 * Adds the WebHooks urls into the resolver.
 * @author netwolfuk
 *
 */
public class WebHookApiUrlBuilder {

	private PathTransformer myPathTransformer;

	public WebHookApiUrlBuilder(@NotNull final PathTransformer pathTransformer) {
		myPathTransformer = pathTransformer;
	}
	
	public String getHref(final WebHookTemplateConfig template) {
	    return myPathTransformer.transform(TemplateRequest.getTemplateHref(template));
	}
	
	public String getHref(String projectExternalId, WebHookParameter parameter) {
		return myPathTransformer.transform(WebHookParametersRequest.getWebHookParameterHref(projectExternalId, parameter));
	}
	
	public String getHref(String projectExternalId, WebHookConfig config) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookHref(projectExternalId, config));
	}
	
	public String getTemplateDefaultItemHref(WebHookTemplateConfig WebHookTemplateConfig) {
		return myPathTransformer.transform(TemplateRequest.getTemplateDefaultItemHref(WebHookTemplateConfig));
	}
	
	public String getTemplateItemHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemHref(WebHookTemplateConfig, webHookTemplateItem));
	}	
	
	public String getDefaultTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(template));
	}

	public String getTemplateItemTextHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemTextHref(WebHookTemplateConfig, webHookTemplateItem));
	}
	
	public String getTemplateItemBranchTextHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemBranchTextHref(WebHookTemplateConfig, webHookTemplateItem));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
		return myPathTransformer.transform(internalRelativePath);
	}

	public String getWebHookTemplateStateUrl(WebHookTemplateConfig template, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateStateHref(template, state));
	}
	
	public String getWebHookTemplateItemStateUrl(WebHookTemplateConfig template, WebHookTemplateItemRest templateItem, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemStateHref(template, templateItem, state));
	}
	
}
