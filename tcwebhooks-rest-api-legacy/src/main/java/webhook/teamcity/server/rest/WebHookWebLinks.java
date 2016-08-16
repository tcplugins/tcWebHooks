package webhook.teamcity.server.rest;

import jetbrains.buildServer.RootUrlHolder;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;

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
	public String getWebHookTemplateUrl(@NotNull WebHookTemplateEntity webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}
	
	@NotNull
	private String makeWebHookTemplateUrl(@NotNull WebHookTemplateEntity entity) {
	    return makeUrl("webhooks/templates.html?template=" + entity.getName());
	}
	
	@NotNull
	private String makeUrl(@NotNull String relativePart) {
	    String baseUrl = myHolder.getRootUrl();
	    if (!baseUrl.endsWith("/")) baseUrl += "/";
	    	return baseUrl + relativePart;
	}

	@NotNull
	public String getWebHookDefaultTemplateTextUrl(@NotNull WebHookTemplateEntity webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

	@NotNull
	public String getWebHookDefaultBranchTemplateTextUrl(WebHookTemplateEntity webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

	@NotNull
	public String getWebHookBranchTemplateTextUrl(WebHookTemplateEntity webHookTemplateEntity, WebHookTemplateItem webHookTemplateItem) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}

}
