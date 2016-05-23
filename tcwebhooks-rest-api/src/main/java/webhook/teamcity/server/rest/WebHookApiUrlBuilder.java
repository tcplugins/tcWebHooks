package webhook.teamcity.server.rest;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;
import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.PathTransformer;

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
		return myPathTransformer.transform(TemplateRequest.getTemplateTextHref(template));
	}

	public String getDefaultBranchTemplateHref(final WebHookTemplateEntity template) {
		return myPathTransformer.transform(TemplateRequest.getBranchTemplateTextHref(template));
	}
	
	public String transformRelativePath(final String internalRelativePath) {
	    return myPathTransformer.transform(internalRelativePath);
	}
}
