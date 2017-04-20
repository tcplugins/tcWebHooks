package webhook.teamcity.server.rest;

import jetbrains.buildServer.server.rest.PathTransformer;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;

/**
 * Adds the WebHooks urls into the resolver.
 * @author netwolfuk
 *
 */
public class WebHookApiUrlBuilder {

	private PathTransformer myPathTransformer;

	public WebHookApiUrlBuilder(@NotNull final PathTransformer pathTransformer) {
		//super(pathTransformer);
		myPathTransformer = pathTransformer;
	}
	
	public String getHref(final WebHookTemplateConfig template) {
	    return myPathTransformer.transform(TemplateRequest.getTemplateHref(template));
	}
	
	public String getTemplateItemHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemHref(WebHookTemplateConfig, webHookTemplateItem));
	}	
	
	public String getDefaultTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateTextHref(final WebHookTemplateConfig template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(template));
	}

	public String getTemplateItemTextHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemTextHref(WebHookTemplateConfig, webHookTemplateItem));
	}
	
	public String getTemplateItemBranchTextHref(WebHookTemplateConfig WebHookTemplateConfig, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemBranchTextHref(WebHookTemplateConfig, webHookTemplateItem));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
		return myPathTransformer.transform(internalRelativePath);
	}

	public String getWebHookTemplateItemStateUrl(WebHookTemplateConfig template, WebHookTemplateItem templateItem, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemStateHref(template, templateItem, state));
	}

	
}
