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
import webhook.teamcity.settings.WebHookFilterConfig;
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
	
	public String getTemplatesHref() {
		return myPathTransformer.transform(TemplateRequest.getTemplatesHref());
	}
	

	public String getParametersHref() {
		return myPathTransformer.transform(WebHookParametersRequest.API_PARAMETERS_URL);
	}

	public String getProjectParameterHref(String projectExternalId, WebHookParameter parameter) {
		return myPathTransformer.transform(WebHookParametersRequest.getWebHookParameterHref(projectExternalId, parameter));
	}
	
	public String getWebHookParameterHref(String projectExternalId, WebHookConfig webhook, WebHookParameter parameter) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookParameterHref(projectExternalId, webhook, parameter));
	}
	
	/**
	 * Gets the href for the specific filterId for the specified webhook.
	 * 
	 * @param projectExternalId
	 * @param config
	 * @param filterId
	 * @return transformed href for this specific filter
	 */
	public String getWebHookFilterHref(String projectExternalId, WebHookConfig config, Integer filterId) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookFilterHref(projectExternalId, config, filterId));
	}
	
	/**
	 * Gets the href for the list of filters for the specified webhook.
	 * @param projectExternalId
	 * @param config
	 * @param filterId
	 * @return transfored href for the list of filters
	 */
	public String getWebHookFiltersHref(String projectExternalId, WebHookConfig config) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookFiltersHref(projectExternalId, config));
	}
	
	public String getConfigurationsHref() {
		return myPathTransformer.transform(WebHooksRequest.API_WEBHOOKS_URL);
	}
	
	public String getHref(String projectExternalId, WebHookConfig config) {
		return myPathTransformer.transform(WebHooksRequest.getWebHookHref(projectExternalId, config));
	}
	
	public String getTemplateDefaultItemHref(WebHookTemplateConfig webHookTemplateConfig) {
		return myPathTransformer.transform(TemplateRequest.getTemplateDefaultItemHref(webHookTemplateConfig));
	}
	
	public String getTemplateItemHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemHref(webHookTemplateConfig, webHookTemplateItem));
	}	
	
	public String getDefaultTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(template));
	}

	public String getTemplateItemTextHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemTextHref(webHookTemplateConfig, webHookTemplateItem));
	}
	
	public String getTemplateItemBranchTextHref(WebHookTemplateConfig webHookTemplateConfig, WebHookTemplateItemRest webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemBranchTextHref(webHookTemplateConfig, webHookTemplateItem));
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
