package webhook.teamcity.settings;

import org.jdom.Element;
import webhook.WebHookProxyConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebHookMainConfig {
    private String webhookInfoUrl = null;
    private String webhookInfoText = null;
    private Boolean webhookShowFurtherReading = true;
    private Integer proxyPort = null;
    private String proxyHost = null;
    private String proxyUsername = null;
    private String proxyPassword = null;
    private Boolean proxyShortNames = false;
    private List<String> noProxyUrls;
    private List<Pattern> noProxyPatterns;

    public final String SINGLE_HOST_REGEX = "^[^./~`'\"]+(?:/.*)?$";
    public final String HOSTNAME_ONLY_REGEX = "^([^/]+)(?:/.*)?$";
    private Pattern singleHostPattern, hostnameOnlyPattern;

    public WebHookMainConfig() {
        noProxyUrls = new ArrayList<>();
        noProxyPatterns = new ArrayList<>();
        singleHostPattern = Pattern.compile(SINGLE_HOST_REGEX);
        hostnameOnlyPattern = Pattern.compile(HOSTNAME_ONLY_REGEX);
    }

    public String getProxyListasString() {
        return " host:" + this.proxyHost + " port: " + this.proxyPort;
    }

    private Pattern generatePatternFromURL(String noProxyUrl) {
        if (this.stripProtocolFromUrl(noProxyUrl).startsWith(".")) {
            return Pattern.compile("^.+" + Pattern.quote(noProxyUrl), Pattern.UNICODE_CASE);
        } else if (this.stripProtocolFromUrl(noProxyUrl).endsWith(".")) {
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
        if (this.matchProxyForURL(url)) {
            if (this.proxyPassword != null && this.proxyPassword.length() > 0
                    && this.proxyUsername != null && this.proxyUsername.length() > 0) {
                return new WebHookProxyConfig(this.proxyHost, this.proxyPort, this.proxyUsername, this.proxyPassword);
            } else {
                return new WebHookProxyConfig(this.proxyHost, this.proxyPort);
            }
        } else {
            return null;
        }
    }

    public String stripProtocolFromUrl(String url) {
        String tmpURL = url;
        if (tmpURL.length() > "https://".length()
                && tmpURL.substring(0, "https://".length()).equalsIgnoreCase("https://")) {
            tmpURL = tmpURL.substring("https://".length());
        } else if (tmpURL.length() > "http://".length()
                && tmpURL.substring(0, "http://".length()).equalsIgnoreCase("http://")) {
            tmpURL = tmpURL.substring("http://".length());
        }
        return tmpURL;
    }

    public String getHostNameFromUrl(String url) {
        Matcher m = hostnameOnlyPattern.matcher(this.stripProtocolFromUrl(url));
        while (m.find()) {
            String s = m.group(1);
            return s;
        }
        return "";
    }

    public boolean isUrlShortName(String url) {
        return singleHostPattern.matcher(stripProtocolFromUrl(url)).find();
    }

    public boolean matchProxyForURL(String url) {
        if ((this.proxyHost == null)
                || (this.proxyHost.length() == 0)
                || (this.proxyPort == null)
                || (!(this.proxyPort > 0))) {
            /* If we don't have all the components of a proxy
			 * configured, don't proxy the URL. 
			 */
            return false;
        } else if (this.proxyShortNames == false && this.isUrlShortName(url)) {
			/* If the hostname part of the URL does not contain a dot, and we have proxyShortNames unset
			 * then don't proxy the URL. 
			 */
            return false;
        } else {
			/* Else loop around the patterns matching the URL and don't 
			 * proxy the URL if we have a match.
			 */
            for (Iterator<Pattern> noProxyPattern = noProxyPatterns.iterator(); noProxyPattern.hasNext(); ) {
                Pattern tempPat = noProxyPattern.next();
                if (tempPat.matcher(this.getHostNameFromUrl(url)).find()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Element getInfoUrlAsElement() {
		/*
			<info url="http://acme.com/" text="Using WebHooks in Acme Inc." />
		 */
        if (this.webhookInfoUrl != null && this.webhookInfoUrl.length() > 0) {
            Element e = new Element("info");
            e.setAttribute("url", webhookInfoUrl);
            if (this.webhookInfoText != null && this.webhookInfoText.length() > 0) {
                e.setAttribute("text", webhookInfoText);
            } else {
                e.setAttribute("text", webhookInfoUrl);
            }
            e.setAttribute("show-reading", webhookShowFurtherReading.toString());

            return e;
        }
        return null;
    }

    private Element getNoProxyAsElement(String noProxyUurl) {
        Element e = new Element("noproxy");
        e.setAttribute("url", noProxyUurl);
        return e;
    }

    public Element getProxyAsElement() {
		/*
    		  <proxy host="myproxy.mycompany.com" port="8080" >
      			<noproxy url=".mycompany.com" />
      			<noproxy url="192.168.0." />
    		  </proxy>
		 */
        if (this.getProxyHost() == null || this.getProxyPort() == null) {
            return null;
        }
        Element el = new Element("proxy");
        el.setAttribute("host", this.getProxyHost());
        el.setAttribute("port", String.valueOf(this.getProxyPort()));
        if (this.proxyPassword != null && this.proxyPassword.length() > 0
                && this.proxyUsername != null && this.proxyUsername.length() > 0) {
            el.setAttribute("username", this.getProxyUsername());
            el.setAttribute("password", this.getProxyPassword());

        }

        if (this.noProxyUrls.size() > 0) {
            for (Iterator<String> i = this.noProxyUrls.iterator(); i.hasNext(); ) {
                el.addContent(this.getNoProxyAsElement(i.next()));
            }
        }
        return el;
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
        return webhookShowFurtherReading;
    }


}