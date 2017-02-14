package webhook.teamcity.auth;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class UsernamePasswordAuthenticator implements WebHookAuthenticator {

    protected static final String REALM = "realm";
    protected static final String PASSWORD = "password";
    protected static final String USERNAME = "username";
    WebHookAuthenticatorProvider myProvider;
    WebHookAuthConfig config;

    @Override
    public void addAuthentication(PostMethod method, HttpClient client, String url) {
        if (config.parameters.containsKey(USERNAME) && config.parameters.containsKey(PASSWORD)) {
            URI uri = URI.create(url);
            AuthScope scope;
            if (config.parameters.containsKey(REALM)) {
                scope = new AuthScope(uri.getHost(), uri.getPort(), config.parameters.get(REALM));
            } else {
                scope = new AuthScope(uri.getHost(), uri.getPort());
            }
            Credentials creds = new UsernamePasswordCredentials(config.parameters.get(USERNAME), config.parameters.get(PASSWORD));
            client.getState().setCredentials(scope, creds);
            client.getParams().setAuthenticationPreemptive(config.preemptive);
        }
    }

    @Override
    @NotNull
    public WebHookAuthConfig getWebHookAuthConfig() {
        return config;
    }

    @Override
    public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig) {
        this.config = authenticationConfig;

    }

}

