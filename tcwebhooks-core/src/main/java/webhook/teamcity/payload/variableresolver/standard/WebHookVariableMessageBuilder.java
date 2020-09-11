package webhook.teamcity.payload.variableresolver.standard;

import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHookVariableMessageBuilder implements VariableMessageBuilder {
	static final String VAR_START = "${";
	static final String VAR_END = "}";
	
	VariableResolver resolver;
	TemplateMatcher matcher;
	
	public static WebHookVariableMessageBuilder create(VariableResolver resolver){
		WebHookVariableMessageBuilder builder = new WebHookVariableMessageBuilder();
		builder.resolver = resolver;
		builder.matcher = new TemplateMatcher(VAR_START, VAR_END);
		return builder;
	}

	@Override
	public String build(String template) {
		return matcher.replace(matcher.replace(template, resolver),resolver);
	}
	
}
