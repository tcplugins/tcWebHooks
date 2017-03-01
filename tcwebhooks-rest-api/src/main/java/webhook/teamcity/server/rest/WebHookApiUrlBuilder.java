package webhook.teamcity.server.rest;

import jetbrains.buildServer.server.rest.PathTransformer;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;

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
	
	public String getHref(final WebHookTemplateEntity template) {
	    return myPathTransformer.transform(TemplateRequest.getTemplateHref(template));
	}
	
	public String getTemplateItemHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemHref(webHookTemplateEntity, webHookTemplateItem));
	}	
	
	public String getDefaultTemplateTextHref(final WebHookTemplateEntity template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateTextHref(final WebHookTemplateEntity template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(template));
	}

	public String getTemplateItemTextHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemTextHref(webHookTemplateEntity, webHookTemplateItem));
	}
	
	public String getTemplateItemBranchTextHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemBranchTextHref(webHookTemplateEntity, webHookTemplateItem));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
		return myPathTransformer.transform(internalRelativePath);
	}

	public String getWebHookTemplateItemStateUrl(WebHookTemplateEntity template, WebHookTemplateItem templateItem, String state) {
		return myPathTransformer.transform(TemplateRequest.getTemplateItemStateHref(template, templateItem, state));
	}

	
}
