package webhook.teamcity.server.rest;

import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.PathTransformer;

/**
 * Extends the TeamCity default ApiBuilder and adds
 * the WebHooks urls into the resolver.
 * @author netwolfuk
 *
 */
public class WebHookApiUrlBuilder extends ApiUrlBuilder {

	public WebHookApiUrlBuilder(PathTransformer pathTransformer) {
		super(pathTransformer);
	}

}
