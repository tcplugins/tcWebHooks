package webhook.teamcity.payload.variableresolver;

public interface VariableMessageBuilder {

	public abstract String build(String template);
	public abstract void addWebHookPayload(String webHookContent);
	
}
