package webhook.teamcity.payload.util.velocity;
import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import webhook.teamcity.payload.util.VariableMessageBuilder;

public class VelocityVariableMessageBuilderImpl implements VariableMessageBuilder {
	String template;
	Context resolver;
	StringWriter sw;
	VelocityEngine ve ;
	
	public static VelocityVariableMessageBuilderImpl create(final String template, Context resolver) {

		VelocityVariableMessageBuilderImpl builder = new VelocityVariableMessageBuilderImpl();
		
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

	@Override
	public String build(){
	    
		sw =  new StringWriter();
	    this.ve.evaluate(resolver, sw, "VariableMessageBuilder", template);
	    return sw.toString();
	}
	
}