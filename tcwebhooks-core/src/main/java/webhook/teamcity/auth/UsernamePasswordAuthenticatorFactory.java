package webhook.teamcity.auth;


public class UsernamePasswordAuthenticatorFactory implements WebHookAuthenticatorFactory{

		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
	
		public UsernamePasswordAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			myProvider = provider;
		}
		
		@Override
		public void register(){
			myProvider.registerAuthType(this);
		}
		
		@Override
		public String getName() {
			return "userpass";
		}

		@Override
		public WebHookAuthenticator getAuthenticatorInstance() {
			return new UsernamePasswordAuthenticator();
		}

}

