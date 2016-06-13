package webhook.teamcity.payload.util;

import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

public class VariableMessageBuilder {
	String template;
	Context resolver;
	StringWriter sw;
	VelocityEngine ve ;
	
	public static VariableMessageBuilder create(final String template, Context resolver){
		
		
		
		
		VariableMessageBuilder builder = new VariableMessageBuilder();
		
		builder.ve = new VelocityEngine();
		
		builder.ve.setProperty("userdirective", "webhook.teamcity.payload.util.VelocitySanitiseDirective, "
											  + "webhook.teamcity.payload.util.VelocitySanitizeDirective, "
											  + "webhook.teamcity.payload.util.VelocityEscapeJsonDirective, "
											  + "webhook.teamcity.payload.util.VelocityNowDirective");
		
		builder.ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
	    	      "org.apache.velocity.runtime.log.Log4JLogChute" );

	    builder.ve.setProperty("runtime.log.logsystem.log4j.logger", "webhook.teamcity.Loggers");
	    
		builder.ve.init();
		builder.template = template;
		builder.resolver = resolver;
		return builder;
	}

	public String build(){
	    
		sw =  new StringWriter();
	    this.ve.evaluate(resolver, sw, "VariableMessageBuilder", template);
	    return sw.toString();
	}
	
}
