package webhook.teamcity.auth;


public class NTLMAuthenticatorFactory implements WebHookAuthenticatorFactory{

		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
	
		public NTLMAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			myProvider = provider;
		}
		
		@Override
		public void register(){
			myProvider.registerAuthType(this);
		}
		
		@Override
		public String getName() {
			return "ntlm";
		}

		@Override
		public WebHookAuthenticator getAuthenticatorInstance() {
			return new NTLMAuthenticator();
		}

}

