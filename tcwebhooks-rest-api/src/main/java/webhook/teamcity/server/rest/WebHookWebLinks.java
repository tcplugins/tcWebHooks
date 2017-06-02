package webhook.teamcity.server.rest;

import jetbrains.buildServer.RootUrlHolder;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;

public class WebHookWebLinks {
	
	RootUrlHolder myHolder;

	public WebHookWebLinks(RootUrlHolder urlHolder) {
		myHolder = urlHolder;
	}
	
	/**
	 * @param build specified build
	 * @return URL to view results page of the specified build
	 */
	@NotNull
	public String getWebHookTemplateUrl(@NotNull WebHookTemplateConfig webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}
	
	@NotNull
	private String makeWebHookTemplateUrl(@NotNull WebHookTemplateConfig entity) {
	    return makeUrl("webhooks/templates.html?template=" + entity.getName());
	}
	
	@NotNull
	private String makeUrl(@NotNull String relativePart) {
	    String baseUrl = myHolder.getRootUrl();
	    if (!baseUrl.endsWith("/")) baseUrl += "/";
	    	return baseUrl + relativePart;
	}

	@NotNull
	public String getWebHookDefaultTemplateTextUrl(@NotNull WebHookTemplateConfig webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

	@NotNull
	public String getWebHookDefaultBranchTemplateTextUrl(WebHookTemplateConfig webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

	@NotNull
	public String getWebHookBranchTemplateTextUrl(WebHookTemplateConfig webHookTemplateEntity, WebHookTemplateItemRest webHookTemplateItem) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

}
