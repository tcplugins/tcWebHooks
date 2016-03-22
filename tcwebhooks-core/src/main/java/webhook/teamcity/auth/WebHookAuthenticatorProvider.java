package webhook.teamcity.auth;

import java.util.HashMap;
import java.util.Set;

import webhook.teamcity.Loggers;

public class WebHookAuthenticatorProvider {
	
	HashMap<String, WebHookAuthenticatorFactory> types = new HashMap<String,WebHookAuthenticatorFactory>();
	
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
	
	public Boolean isRegisteredType(String type){
		return types.containsKey(type);
	}
	
	public Set<String> getRegisteredTypes(){
		return types.keySet();
	}	
	
	
}