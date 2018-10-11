package webhook.teamcity.payload.util;

public interface VariableMessageBuilderFactory {
	
	public VariableMessageBuilder build();
	public void register(VariableMessageBuilderManager manager);
	public String getType();

}
