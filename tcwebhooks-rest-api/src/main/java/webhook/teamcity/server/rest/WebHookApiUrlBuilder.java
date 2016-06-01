package webhook.teamcity.server.rest;

import jetbrains.buildServer.server.rest.PathTransformer;

import org.jetbrains.annotations.NotNull;

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
	
	public String getDefaultTemplateHref(final WebHookTemplateEntity template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateHref(final WebHookTemplateEntity template) {
		return myPathTransformer.transform(TemplateRequest.getDefaultBranchTemplateTextHref(template));
	}
	
	public String getTemplateHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getTemplateTextHref(webHookTemplateEntity, webHookTemplateItem));
	}

	public String getBranchTemplateHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getBranchTemplateTextHref(webHookTemplateEntity, webHookTemplateItem));
	}
	
	public String getBranchTemplateTextHref(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return myPathTransformer.transform(TemplateRequest.getBranchTemplateTextHref(webHookTemplateEntity, webHookTemplateItem));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
		return myPathTransformer.transform(internalRelativePath);
	}

	
}
