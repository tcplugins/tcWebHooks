package webhook;

@SuppressWarnings("serial")
public class WebHookParameterReferenceException extends Exception {
	private final String key;

	public WebHookParameterReferenceException(String key){
		super();
		this.key = key;
	}
	
	public String getKey(){
		return this.key;
	}
}
