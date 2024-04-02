package webhook.teamcity.server.rest.util;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.WebHookWebLinks;

public class WebHookBeanContext {
	private final @NotNull WebHookApiUrlBuilder myApiUrlBuilder;
	private final @NotNull WebHookWebLinks myWebHookWebLinks;

	public WebHookBeanContext(@NotNull final WebHookWebLinks webHookWebLinks, @NotNull WebHookApiUrlBuilder apiUrlBuilder) {
		myWebHookWebLinks = webHookWebLinks;
		myApiUrlBuilder = apiUrlBuilder;
	}

	@NotNull
	public WebHookApiUrlBuilder getApiUrlBuilder() {
		return myApiUrlBuilder;
	}

	public WebHookWebLinks getWebHookWebLinks() {
		return myWebHookWebLinks;
	}
}