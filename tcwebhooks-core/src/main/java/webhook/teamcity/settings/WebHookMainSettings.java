package webhook.teamcity.settings;

import java.util.List;

import jetbrains.buildServer.serverSide.MainConfigProcessor;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.jdom.Element;

import webhook.WebHookProxyConfig;
import webhook.teamcity.Loggers;

public class WebHookMainSettings implements MainConfigProcessor {
	private static final String ATTRIBUTENAME_USE_THREADED_EXECUTOR = "useThreadedExecutor";
	private static final String ATTRIBUTENAME_ENABLED = "enabled";
	private static final String NAME = WebHookMainSettings.class.getName();
	private WebHookMainConfig webHookMainConfig;
	private SBuildServer server;

	public WebHookMainSettings(SBuildServer server) {
		this.server = server;
		webHookMainConfig = new WebHookMainConfig();
	}

	public void register(){
		Loggers.SERVER.debug(NAME + ":: Registering");
		server.registerExtension(MainConfigProcessor.class, "webhooks", this);
	}

	public String getProxyListasString(){
		return this.webHookMainConfig.getProxyListasString();
	}

	@SuppressWarnings("unchecked")
	public void readFrom(Element rootElement)
	/* Is passed an Element by TC, and is expected to persist it to the settings object.
	 * Old settings should be overwritten.
	 */
	{
		Loggers.SERVER.info("WebHookMainSettings: re-reading main settings");
		Loggers.SERVER.debug(NAME + ":readFrom :: " + rootElement.toString());
		WebHookMainConfig tempConfig = new WebHookMainConfig();
		Element webhooksElement = rootElement.getChild("webhooks");
		if(webhooksElement != null){
			if (webhooksElement.getAttribute(ATTRIBUTENAME_USE_THREADED_EXECUTOR) != null) {
				tempConfig.setThreadPoolExecutor(Boolean.parseBoolean(webhooksElement.getAttributeValue(ATTRIBUTENAME_USE_THREADED_EXECUTOR)));
			}
			Element extraInfoElement = webhooksElement.getChild("info");
			if(extraInfoElement != null)
			{
				if ((extraInfoElement.getAttribute("text") != null)
				&& (extraInfoElement.getAttribute("url")  != null)){
					tempConfig.setWebhookInfoText(extraInfoElement.getAttributeValue("text"));
					tempConfig.setWebhookInfoUrl(extraInfoElement.getAttributeValue("url"));
					Loggers.SERVER.debug(NAME + ":readFrom :: info text " + tempConfig.getWebhookInfoText());
					Loggers.SERVER.debug(NAME + ":readFrom :: info url  " + tempConfig.getWebhookInfoUrl());
				}
				if (extraInfoElement.getAttribute("show-reading") != null){
					tempConfig.setWebhookShowFurtherReading(Boolean.parseBoolean(extraInfoElement.getAttributeValue("show-reading")));
					Loggers.SERVER.debug(NAME + ":readFrom :: show reading " + tempConfig.getWebhookShowFurtherReading().toString());
				}
			}

			Element timeoutsElement = webhooksElement.getChild("http-timeout");
			if (timeoutsElement != null) {
				if (timeoutsElement.getAttribute("connect") != null) {
					tempConfig.setHttpConnectionTimeout(Integer.valueOf(timeoutsElement.getAttributeValue("connect")));
				}
				if (timeoutsElement.getAttribute("response") != null) {
					tempConfig.setHttpResponseTimeout(Integer.valueOf(timeoutsElement.getAttributeValue("response")));
				}
			}

			Element statisticsElement = webhooksElement.getChild("statistics");
			if (statisticsElement != null && statisticsElement.getAttribute(ATTRIBUTENAME_ENABLED) != null) {
				tempConfig.setAssembleStatistics(Boolean.valueOf(statisticsElement.getAttributeValue(ATTRIBUTENAME_ENABLED)));
			}
			if (statisticsElement != null) {
				Element reportingElement = statisticsElement.getChild("reporting");
				if (reportingElement != null && reportingElement.getAttribute(ATTRIBUTENAME_ENABLED) != null) {
					tempConfig.setReportStatistics(Boolean.valueOf(reportingElement.getAttributeValue(ATTRIBUTENAME_ENABLED)));
				}
				if (reportingElement != null && reportingElement.getAttribute("frequency") != null) {
					tempConfig.setReportStatisticsFrequency(Integer.valueOf(reportingElement.getAttributeValue("frequency")));
				}
			}

			Element proxyElement = webhooksElement.getChild("proxy");
			if(proxyElement != null)
			{
				if (proxyElement.getAttribute("proxyShortNames") != null){
					tempConfig.setProxyShortNames(Boolean.parseBoolean(proxyElement.getAttributeValue("proxyShortNames")));
				}

				if (proxyElement.getAttribute("host") != null){
					tempConfig.setProxyHost(proxyElement.getAttributeValue("host"));
				}

				if (proxyElement.getAttribute("port") != null){
					tempConfig.setProxyPort(Integer.parseInt(proxyElement.getAttributeValue("port")));
				}

				if (proxyElement.getAttribute("username") != null){
					tempConfig.setProxyUsername(proxyElement.getAttributeValue("username"));
				}

				if (proxyElement.getAttribute("password") != null){
					tempConfig.setProxyPassword(proxyElement.getAttributeValue("password"));
				}

				List<Element> namedChildren = proxyElement.getChildren("noproxy");
				if(!namedChildren.isEmpty()) {
					for(Element e : namedChildren){
						String url = e.getAttributeValue("url");
						tempConfig.addNoProxyUrl(url);
						Loggers.SERVER.debug(NAME + ":readFrom :: noProxyUrl " + url);
					}
				}
			}
		}
		this.webHookMainConfig = tempConfig;
	}

	public void writeTo(Element parentElement)
	/* Is passed an (probably empty) Element by TC, which is expected to be populated from the settings
	 * in memory.
	 */
	{
		Loggers.SERVER.info("WebHookMainSettings: re-writing main settings");
		Loggers.SERVER.debug(NAME + ":writeTo :: " + parentElement.toString());
		Element webhooksElement = new Element("webhooks");
		if (webHookMainConfig.useThreadedExecutorIsDefined()) {
			webhooksElement.setAttribute(ATTRIBUTENAME_USE_THREADED_EXECUTOR, Boolean.toString(webHookMainConfig.useThreadedExecutor()));
		}
		if(webHookMainConfig.getProxyHost() != null && webHookMainConfig.getProxyHost().length() > 0
				&& webHookMainConfig.getProxyPort() != null && webHookMainConfig.getProxyPort() > 0 )
		{
			webhooksElement.addContent(webHookMainConfig.getProxyAsElement());
			Loggers.SERVER.debug(NAME + "writeTo :: proxyHost " + webHookMainConfig.getProxyHost());
			Loggers.SERVER.debug(NAME + "writeTo :: proxyPort " + webHookMainConfig.getProxyPort().toString());
		}

		if(webHookMainConfig.getTimeoutsAsElement() != null) {
			webhooksElement.addContent(webHookMainConfig.getTimeoutsAsElement());
		}

		if(webHookMainConfig.getInfoUrlAsElement() != null){
			webhooksElement.addContent(webHookMainConfig.getInfoUrlAsElement());
			Loggers.SERVER.debug(NAME + "writeTo :: infoText " + webHookMainConfig.getWebhookInfoText());
			Loggers.SERVER.debug(NAME + "writeTo :: InfoUrl  " + webHookMainConfig.getWebhookInfoUrl());
			Loggers.SERVER.debug(NAME + "writeTo :: show-reading  " + webHookMainConfig.getWebhookShowFurtherReading().toString());
		}

		if (webHookMainConfig.getStatisticsAsElement() != null) {
		    webhooksElement.addContent(webHookMainConfig.getStatisticsAsElement());
		}

		parentElement.addContent(webhooksElement);
	}

	public String getProxyForUrl(String url){
		return this.webHookMainConfig.getProxyConfigForUrl(url).getProxyHost();
	}

	public String getInfoText(){
		return this.webHookMainConfig.getWebhookInfoText();
	}

	public String getInfoUrl(){
		return this.webHookMainConfig.getWebhookInfoUrl();
	}

	public Boolean getWebhookShowFurtherReading(){
		return this.webHookMainConfig.getWebhookShowFurtherReading();
	}

	public void dispose() {
		Loggers.SERVER.debug(NAME + ":dispose() called");
	}

	public WebHookProxyConfig getProxyConfigForUrl(String url) {
		return this.webHookMainConfig.getProxyConfigForUrl(url);
	}

	public WebHookMainConfig getWebHookMainConfig() {
		return webHookMainConfig;
	}

	public int getHttpConnectionTimeout() {
		return this.webHookMainConfig.getHttpConnectionTimeout();
	}

	public int getHttpResponseTimeout() {
		return this.webHookMainConfig.getHttpResponseTimeout();
	}

	public boolean useThreadedExecutor() {
		return this.webHookMainConfig.useThreadedExecutor();
	}

	public boolean isReportStatisticsEnabled() {
		return this.webHookMainConfig.isReportStatisticsEnabled();
	}

	public int getReportStatisticsFrequency() {
		return this.webHookMainConfig.getReportStatisticsFrequency();
	}

	public boolean isAssembleStatisticsEnabled() {
		return this.webHookMainConfig.isAssembleStatistics();
	}

}
