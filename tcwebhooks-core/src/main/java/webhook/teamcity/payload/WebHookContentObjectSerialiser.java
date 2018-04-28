package webhook.teamcity.payload;

public interface WebHookContentObjectSerialiser {
	
	public abstract Object serialiseObject(Object object);

}
