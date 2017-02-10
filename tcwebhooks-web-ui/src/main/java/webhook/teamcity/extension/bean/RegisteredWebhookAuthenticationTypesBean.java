package webhook.teamcity.extension.bean;

import lombok.Getter;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisteredWebhookAuthenticationTypesBean {

    Map<String, SimpleAuthType> authenticators = new LinkedHashMap<String, SimpleAuthType>();

    public static RegisteredWebhookAuthenticationTypesBean build(WebHookAuthenticatorProvider provider) {
        RegisteredWebhookAuthenticationTypesBean bean = new RegisteredWebhookAuthenticationTypesBean();
        for (String authenticatorName : provider.getRegisteredTypes()) {
            if (provider.getAuthenticationParameters(authenticatorName) != null) {
                bean.authenticators.put(authenticatorName, new SimpleAuthType(provider.getDescription(authenticatorName), provider.getAuthenticationParameters(authenticatorName)));
            }
        }
        return bean;
    }

    public Map<String, SimpleAuthType> getAuthenticators() {
        return authenticators;
    }

    @Getter
    public static class SimpleAuthType {

        private String description;
        private List<WebHookAuthenticationParameter> parameters = new ArrayList<WebHookAuthenticationParameter>();

        public SimpleAuthType(String description, List<WebHookAuthenticationParameter> params) {
            this.description = description;
            this.parameters.addAll(params);
        }
    }

}
