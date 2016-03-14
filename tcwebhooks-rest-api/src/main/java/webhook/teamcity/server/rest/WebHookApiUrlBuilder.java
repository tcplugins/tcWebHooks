package webhook.teamcity.server.rest;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.settings.entity.WebHookTemplate;
import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.PathTransformer;

/**
 * Extends the TeamCity default ApiBuilder and adds
 * the WebHooks urls into the resolver.
 * @author netwolfuk
 *
 */
public class WebHookApiUrlBuilder {

	private PathTransformer myPathTransformer;

	public WebHookApiUrlBuilder(@NotNull final PathTransformer pathTransformer) {
		//super(pathTransformer);
		myPathTransformer = pathTransformer;
	}
	
	public String getHref(final WebHookTemplate template) {
	    return myPathTransformer.transform(TemplateRequest.getTemplateHref(template));
	}

	public String transformRelativePath(final String internalRelativePath) {
	    return myPathTransformer.transform(internalRelativePath);
	}
}
