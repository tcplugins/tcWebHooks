package webhook.teamcity.server.rest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class WebHookWebLinks {
	
	RootUrlHolder myHolder;

	public WebHookWebLinks(RootUrlHolder urlHolder) {
		myHolder = urlHolder;
	}
	
	@NotNull
	public String getWebHookUrl(@NotNull String  projectId) {
		return makeWebHookUrl(projectId);
	}
	
	@NotNull
	private String makeWebHookUrl(@NotNull String projectId) {
	    return makeUrl("webhooks/index.html?projectId=" + projectId);
	}
	
	/**
	 * @param build specified build
	 * @return URL to view template for specific template. 
	 */
	@NotNull
	public String getWebHookTemplateUrl(@NotNull WebHookTemplateConfig webHookTemplateEntity) {
		return makeWebHookTemplateUrl(webHookTemplateEntity);
	}
	
	@NotNull
	private String makeWebHookTemplateUrl(@NotNull WebHookTemplateConfig entity) {
	    return makeUrl("webhooks/template.html?template=" + entity.getId());
	}
	
	@NotNull
	private String makeUrl(@NotNull String relativePart) {
	    String baseUrl = myHolder.getRootUrl();
	    if (!baseUrl.endsWith("/")) {
	    	baseUrl += "/";
	    }
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
