package webhook;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;


public class WebHookProxyConfig {
    private Credentials creds = null;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private String proxyUsername = null;
    private String proxyPassword = null;

    public WebHookProxyConfig(String hostname, Integer port, String username, String password) {
        this.proxyUsername = username;
        this.proxyPassword = password;
        this.proxyHost = hostname;
        this.proxyPort = port;
        this.setCreds();
    }

    public WebHookProxyConfig(String hostname, Integer port) {
        this.proxyHost = hostname;
        this.proxyPort = port;
    }

    private void setCreds() {
        if (this.proxyUsername != null && this.proxyUsername.length() > 0
                && this.proxyPassword != null && this.proxyPassword.length() > 0) {
            this.creds = new UsernamePasswordCredentials(this.proxyUsername, this.proxyPassword);
        }
    }

    public Credentials getCreds() {
        return this.creds;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

}
