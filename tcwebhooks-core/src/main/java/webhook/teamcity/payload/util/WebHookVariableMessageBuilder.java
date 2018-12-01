package webhook.teamcity.payload.util;

import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;

public class WebHookVariableMessageBuilder implements VariableMessageBuilder {
	static final String VAR_START = "${";
	static final String VAR_END = "}";
	
	String template;
	VariableResolver resolver;
	TemplateMatcher matcher;
	
	public static WebHookVariableMessageBuilder create(final String template, VariableResolver resolver){
		WebHookVariableMessageBuilder builder = new WebHookVariableMessageBuilder();
		builder.template = template;
		builder.resolver = resolver;
		builder.matcher = new TemplateMatcher(VAR_START, VAR_END);
		return builder;
	}

	public String build(){
		return matcher.replace(template, resolver);
	}
	
}
