package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import org.jdom.Element;

import lombok.Setter;
import webhook.WebHookProxyConfig;


public class WebHookMainConfig {

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";
	public static final String SINGLE_HOST_REGEX = "^[^./~`'\"]+(?:/.*)?$";
	public static final String HOSTNAME_ONLY_REGEX = "^([^/]+)(?:/.*)?$";
	private static final int HTTP_CONNECT_TIMEOUT_DEFAULT = 120;
	private static final int HTTP_RESPONSE_TIMEOUT_DEFAULT = 120;
	private static final int STATISTICS_REPORTING_DAYS_DEFAULT = 5;
	private static final boolean WEBHOOK_DEDICATED_EXECUTOR = true; // Whether to use a dedicated thread pool for WebHooks.
	private static final int WEB_HOOK_MIN_THREADS = 1;
	private static final int WEB_HOOK_MAX_THREADS = 50;
	private static final int WEB_HOOK_THREAD_QUEUE_SIZE = 3000;

	private String webhookInfoUrl = null;
	private String webhookInfoText = null;
	private Boolean webhookShowFurtherReading = null;
	private Integer proxyPort = null;
	private String proxyHost = null;
	private String proxyUsername = null;
	private String proxyPassword = null;
	private Boolean proxyShortNames = false;
	private List<String> noProxyUrls = null;
	private List<Pattern> noProxyPatterns = null;
	private Integer httpConnectionTimeout = null;
	private Integer httpResponseTimeout = null;
	private Boolean useThreadedExecutor = null;

	private Boolean assembleStatistics = null;
	private Boolean reportStatistics = null;
	private Integer reportStatisticsFrequencyDays = null;

	@Setter private Boolean dedicatedThreadPool = null;
	@Setter private Integer minPoolSize = null;
	@Setter private Integer maxPoolSize = null;
	@Setter private Integer queueSize = null;


	private Pattern singleHostPattern;
	private Pattern hostnameOnlyPattern;

	public WebHookMainConfig() {
		noProxyUrls = new ArrayList<>();
		noProxyPatterns = new ArrayList<>();
		singleHostPattern = Pattern.compile(SINGLE_HOST_REGEX);
		hostnameOnlyPattern = Pattern.compile(HOSTNAME_ONLY_REGEX);
	}

	public String getProxyListasString(){
		return " host:" + this.proxyHost + " port: " + this.proxyPort;
	}

	private Pattern generatePatternFromURL(String noProxyUrl){
		if(this.stripProtocolFromUrl(noProxyUrl).startsWith(".")){
			return Pattern.compile("^.+" + Pattern.quote(noProxyUrl), Pattern.UNICODE_CASE);
		} else if (this.stripProtocolFromUrl(noProxyUrl).endsWith(".")){
			return Pattern.compile("^" + Pattern.quote(noProxyUrl) + ".+", Pattern.UNICODE_CASE);
		} else {
			return Pattern.compile("^" + Pattern.quote(noProxyUrl), Pattern.UNICODE_CASE);
		}
	}

	public void addNoProxyUrl(String noProxyUrl) {
		noProxyUrls.add(noProxyUrl);
		noProxyPatterns.add(generatePatternFromURL(noProxyUrl));
	}

	public WebHookProxyConfig getProxyConfigForUrl(String url) {
		if(this.matchProxyForURL(url)){
			if (   this.proxyPassword != null && this.proxyPassword.length() > 0
				&& this.proxyUsername != null && this.proxyUsername.length() > 0 ){
				return new WebHookProxyConfig(this.proxyHost,this.proxyPort, this.proxyUsername, this.proxyPassword);
			} else {
				return new WebHookProxyConfig(this.proxyHost,this.proxyPort);
			}
		} else {
			return null;
		}
	}

	public String stripProtocolFromUrl(String url){
		String tmpURL = url;
		if(tmpURL.length() > HTTPS_PREFIX.length()
			&& tmpURL.substring(0,HTTPS_PREFIX.length()).equalsIgnoreCase(HTTPS_PREFIX))
		{
				tmpURL = tmpURL.substring(HTTPS_PREFIX.length());
		} else if (tmpURL.length() > HTTP_PREFIX.length()
			&& tmpURL.substring(0,HTTP_PREFIX.length()).equalsIgnoreCase(HTTP_PREFIX))
		{
				tmpURL = tmpURL.substring(HTTP_PREFIX.length());
		}
		return tmpURL;
	}

	public String getHostNameFromUrl(String url){
		Matcher m = hostnameOnlyPattern.matcher(this.stripProtocolFromUrl(url));
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	public boolean isUrlShortName(String url){
		return singleHostPattern.matcher(stripProtocolFromUrl(url)).find();
	}

	public boolean matchProxyForURL(String url) {
		if ((this.proxyHost == null)
				|| (this.proxyHost.length() == 0)
				|| (this.proxyPort == null)
				|| (this.proxyPort <= 0)){
			/* If we don't have all the components of a proxy
			 * configured, don't proxy the URL.
			 */
			return false;
		} else if (!Boolean.TRUE.equals(this.proxyShortNames) && this.isUrlShortName(url)){
			/* If the hostname part of the URL does not contain a dot, and we have proxyShortNames unset
			 * then don't proxy the URL.
			 */
			return false;
		} else {
			/* Else loop around the patterns matching the URL and don't
			 * proxy the URL if we have a match.
			 */
			for(Iterator<Pattern> noProxyPattern = noProxyPatterns.iterator(); noProxyPattern.hasNext();)
			{
				Pattern tempPat = noProxyPattern.next();
				if (tempPat.matcher(this.getHostNameFromUrl(url)).find()){
					return false;
				}
			}
		}
		return true;
	}

	public Element getInfoUrlAsElement(){
		/*
			<info url="http://acme.com/" text="Using WebHooks in Acme Inc." />
		 */
		if (this.webhookInfoUrl != null && this.webhookInfoUrl.length() > 0){
			Element e = new Element("info");
			e.setAttribute("url", webhookInfoUrl);
			if (this.webhookInfoText != null && this.webhookInfoText.length() > 0){
				e.setAttribute("text", webhookInfoText);
			} else {
				e.setAttribute("text", webhookInfoUrl);
			}
			e.setAttribute("show-reading", webhookShowFurtherReading.toString());

			return e;
		}
		return null;
	}

	private Element getNoProxyAsElement(String noProxyUurl){
		Element e = new Element("noproxy");
		e.setAttribute("url", noProxyUurl);
		return e;
	}

	public Element getProxyAsElement(){
		/*
			<proxy host="myproxy.mycompany.com" port="8080" >
				<noproxy url=".mycompany.com" />
				<noproxy url="192.168.0." />
			</proxy>
		 */
		if (this.getProxyHost() == null || this.getProxyPort() == null){
			return null;
		}
		Element el = new Element("proxy");
		el.setAttribute("host", this.getProxyHost());
		el.setAttribute("port", String.valueOf(this.getProxyPort()));
		el.setAttribute("proxyShortNames", String.valueOf(this.isProxyShortNames()));
		if (   this.proxyPassword != null && this.proxyPassword.length() > 0
			&& this.proxyUsername != null && this.proxyUsername.length() > 0 )
		{
			el.setAttribute("username", this.getProxyUsername());
			el.setAttribute("password", this.getProxyPassword());

		}

		if (!this.noProxyUrls.isEmpty()){
			for (Iterator<String> i = this.noProxyUrls.iterator(); i.hasNext();){
				el.addContent(this.getNoProxyAsElement(i.next()));
			}
		}
		return el;
	}

	public Element getTimeoutsAsElement () {
		if (this.httpConnectionTimeout == null && this.httpResponseTimeout == null) {
			return null;
		}
		return new Element("http-timeout")
				.setAttribute("connect", String.valueOf(getHttpConnectionTimeout()))
				.setAttribute("response", String.valueOf(this.getHttpResponseTimeout()));
	}

	public boolean isAssembleStatistics() {
		return defaultIfNull(this.assembleStatistics, Boolean.TRUE);
	}

	public void setAssembleStatistics(boolean assembleStatistics) {
		this.assembleStatistics = assembleStatistics;
	}

	public boolean isReportStatisticsEnabled() {
		return defaultIfNull(this.reportStatistics, Boolean.FALSE);
	}

	public void setReportStatistics(Boolean reportStatistics) {
		this.reportStatistics = reportStatistics;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public Boolean isProxyShortNames() {
		return proxyShortNames;
	}

	public void setProxyShortNames(Boolean proxyShortNames) {
		this.proxyShortNames = proxyShortNames;
	}

	public List<String> getNoProxyUrls() {
		return noProxyUrls;
	}

	public void setNoProxyUrls(List<String> noProxyUrls) {
		this.noProxyUrls = noProxyUrls;
	}

	public String getWebhookInfoUrl() {
		return webhookInfoUrl;
	}

	public String getWebhookInfoText() {
		return webhookInfoText;
	}

	public void setWebhookInfoUrl(String webhookInfoUrl) {
		this.webhookInfoUrl = webhookInfoUrl;
	}

	public void setWebhookInfoText(String webhookInfoText) {
		this.webhookInfoText = webhookInfoText;
	}

	public void setWebhookShowFurtherReading(Boolean webhookShowFurtherReading) {
		this.webhookShowFurtherReading = webhookShowFurtherReading;
	}

	public Boolean getWebhookShowFurtherReading() {
		return defaultIfNull(webhookShowFurtherReading, Boolean.TRUE);
	}

	public Integer getHttpConnectionTimeout() {
		return defaultIfNull(httpConnectionTimeout, HTTP_CONNECT_TIMEOUT_DEFAULT);
	}

	public void setHttpConnectionTimeout(Integer httpConnectionTimeout) {
		this.httpConnectionTimeout = httpConnectionTimeout;
	}

	public Integer getHttpResponseTimeout() {
		return defaultIfNull(this.httpResponseTimeout, HTTP_RESPONSE_TIMEOUT_DEFAULT);
	}

	public void setHttpResponseTimeout(Integer httpResponseimeout) {
		this.httpResponseTimeout = httpResponseimeout;
	}

	public boolean useThreadedExecutor() {
		return defaultIfNull(this.useThreadedExecutor, Boolean.TRUE);
	}

	public boolean useThreadedExecutorIsDefined() {
		return this.useThreadedExecutor != null;
	}

	public void setThreadPoolExecutor(boolean threadPoolSender) {
		this.useThreadedExecutor = threadPoolSender;
	}

	public boolean useDedicatedThreadPool() {
		return defaultIfNull(this.dedicatedThreadPool, WEBHOOK_DEDICATED_EXECUTOR);
	}

	public int getMinPoolSize() {
		return defaultIfNull(this.minPoolSize, WEB_HOOK_MIN_THREADS);
	}
	public int getMaxPoolSize() {
		return defaultIfNull(this.maxPoolSize, WEB_HOOK_MAX_THREADS);
	}
	public int getQueueSize() {
		return defaultIfNull(this.queueSize, WEB_HOOK_THREAD_QUEUE_SIZE);
	}

	public int getReportStatisticsFrequency() {
		return defaultIfNull(this.reportStatisticsFrequencyDays, STATISTICS_REPORTING_DAYS_DEFAULT);
	}

	public void setReportStatisticsFrequency(Integer statisticsFrequencyDays) {
		this.reportStatisticsFrequencyDays = statisticsFrequencyDays;
	}

	public Element getStatisticsAsElement() {
		if (this.assembleStatistics != null || this.reportStatistics != null || this.reportStatisticsFrequencyDays != null) {
			Element statistics = new Element("statistics");
			if (this.assembleStatistics != null) {
				statistics.setAttribute("enabled", Boolean.toString(isAssembleStatistics()));
			}
			if (this.reportStatistics != null || this.reportStatisticsFrequencyDays != null) {
				Element reporting = new Element("reporting");
				if (this.reportStatistics != null) {
					reporting.setAttribute("enabled", Boolean.toString(isReportStatisticsEnabled()));
				}
				if (this.reportStatisticsFrequencyDays != null) {
					reporting.setAttribute("frequency", String.valueOf(this.getReportStatisticsFrequency()));
				}
				statistics.addContent(reporting);
			}
			return statistics;
		}
		return null;
	}

	/**
	 *     &lt;dedicatedThreadPool enabled="true" minPoolSize="10" maxPoolSize="20" queueSize="1000"/&gt;
	 * @return the above as an XML element
	 */
	public Element getDedicatedThreadPoolAsElement() {
		if(this.dedicatedThreadPool != null || this.minPoolSize != null || this.maxPoolSize != null || this.queueSize != null) {
			Element dedicatedThreadPoolElement = new Element(WebHookMainSettings.ATTRIBUTENAME_DEDICATED_THREAD_POOL);
			if(this.dedicatedThreadPool != null) {
				dedicatedThreadPoolElement.setAttribute(WebHookMainSettings.ATTRIBUTENAME_ENABLED, Boolean.toString(this.dedicatedThreadPool));
			}
			if(this.minPoolSize != null) {
				dedicatedThreadPoolElement.setAttribute(WebHookMainSettings.ATTRIBUTENAME_MIN_POOL_SIZE, Integer.toString(this.minPoolSize));
			}
			if(this.maxPoolSize != null) {
				dedicatedThreadPoolElement.setAttribute(WebHookMainSettings.ATTRIBUTENAME_MAX_POOL_SIZE, Integer.toString(this.maxPoolSize));
			}
			if(this.queueSize != null) {
				dedicatedThreadPoolElement.setAttribute(WebHookMainSettings.ATTRIBUTENAME_QUEUE_SIZE, Integer.toString(this.queueSize));
			}
			return dedicatedThreadPoolElement;
		}
		return null;
	}

}
