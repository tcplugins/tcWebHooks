package webhook.teamcity.payload.variableresolver.velocity;

import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;

public class WebHookVelocityVariableMessageBuilder implements VariableMessageBuilder {

	private static final String PACKAGE = "webhook.teamcity.payload.variableresolver.velocity.";
	String template;
	Context resolver;
	StringWriter sw;
	VelocityEngine ve ;
	
	public static WebHookVelocityVariableMessageBuilder create(final String template, Context resolver){
		WebHookVelocityVariableMessageBuilder builder = new WebHookVelocityVariableMessageBuilder();
		builder.ve = new VelocityEngine();
		
		builder.ve.setProperty("userdirective", PACKAGE + "VelocitySanitiseDirective, "
											  + PACKAGE + "VelocitySanitizeDirective, "
											  + PACKAGE + "VelocityEscapeJsonDirective, "
											  + PACKAGE + "VelocityCapitaliseDirective, "
											  + PACKAGE + "VelocityCapitalizeDirective, "
											  + PACKAGE + "VelocityNowDirective, "
											  + PACKAGE + "VelocitySubStringDirective");
		
		builder.ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
	    	      "org.apache.velocity.runtime.log.Log4JLogChute" );

	    builder.ve.setProperty("runtime.log.logsystem.log4j.logger", "webhook.teamcity.Loggers");
	    
		builder.ve.init();
		builder.sw =  new StringWriter();
		builder.template = template;
		builder.resolver = resolver;
		return builder;
	}

	public String build(){
	    
	    this.ve.evaluate(resolver, sw, "WebHookVelocityVariableMessageBuilder", template);
	    return sw.toString();
	}
}
