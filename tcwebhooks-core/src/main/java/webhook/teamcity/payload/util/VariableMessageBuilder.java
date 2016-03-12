package webhook.teamcity.payload.util;

import java.io.StringWriter;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class VariableMessageBuilder {
	String template;
	Context resolver;
	StringWriter sw;
	VelocityEngine ve ;
	
	public static VariableMessageBuilder create(final String template, Context resolver){
		
		
//	    ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
//	    	      "org.apache.velocity.runtime.log.Log4JLogChute" );
//
//	    	    ve.setProperty("runtime.log.logsystem.log4j.logger",
//	    	                    LOGGER_NAME);
		
		
		VariableMessageBuilder builder = new VariableMessageBuilder();
		
		builder.ve = new VelocityEngine();
		builder.sw =  new StringWriter();
		builder.template = template;
		builder.resolver = resolver;
		return builder;
	}

	public String build(){
	    
	    Velocity.evaluate(resolver, sw, "logTag", template);
	    return sw.toString();
	}
	
}
