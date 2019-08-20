package webhook.teamcity.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import webhook.teamcity.Loggers;

public class WebHookAuthenticatorProvider {

	HashMap<String, WebHookAuthenticatorFactory> types = new HashMap<>();

	public WebHookAuthenticatorProvider(){
		Loggers.SERVER.info("WebHookAuthenticatorProvider :: Starting");
	}

	public void registerAuthType(WebHookAuthenticatorFactory authType){
		Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering authentication type "
				+ authType.getName());
		types.put(authType.getName(),authType);
		Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Authenticator list is " + this.types.size() + " items long.");
		for (String auth : this.types.keySet()){
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Authenticator Name: " + auth);
		}
	}

	public WebHookAuthenticator getAuthenticator(String typeName){
		if (types.containsKey(typeName)){
			return types.get(typeName).getAuthenticatorInstance();
		}
		return null;
	}

	public boolean areAllRequiredParametersPresent(WebHookAuthConfig webHookAuthConfig){
		return types.get(webHookAuthConfig.getType()).areAllRequiredParametersPresent(webHookAuthConfig);
	}

	public String getDescription(String typeName){
		if (types.containsKey(typeName)){
			return types.get(typeName).getDescription();
		}
		return null;
	}

	public List<WebHookAuthenticationParameter> getAuthenticationParameters(String typeName){
		if (types.containsKey(typeName)){
			return types.get(typeName).getParameterList();
		}
		return Collections.emptyList();
	}

	public Boolean isRegisteredType(String type){
		return types.containsKey(type);
	}

	public Set<String> getRegisteredTypes(){
		return types.keySet();
	}


}