package webhook.teamcity.payload.variableresolver.velocity;

import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.settings.secure.WebHookSecretResolver;

public class WebHookVelocityVariableMessageBuilder implements VariableMessageBuilder {

	private static final String PACKAGE = "webhook.teamcity.payload.variableresolver.velocity.";
	Context resolver;
	VelocityEngine ve ;
	
	public static WebHookVelocityVariableMessageBuilder create(Context resolver, WebHookSecretResolver webHookSecretResolver){
		WebHookVelocityVariableMessageBuilder builder = new WebHookVelocityVariableMessageBuilder();
		builder.ve = new VelocityEngine();
		
		builder.ve.setProperty("userdirective", PACKAGE + "VelocitySanitiseDirective, "
											  + PACKAGE + "VelocitySanitizeDirective, "
											  + PACKAGE + "VelocityEscapeJsonDirective, "
											  + PACKAGE + "VelocityCapitaliseDirective, "
											  + PACKAGE + "VelocityCapitalizeDirective, "
											  + PACKAGE + "VelocityNowDirective, "
											  + PACKAGE + "VelocitySubStringDirective, "
											  + PACKAGE + "VelocityToJsonDirective,"
											  + PACKAGE + "VelocitySecureDirective");
		
		builder.ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
	    	      "org.apache.velocity.runtime.log.Log4JLogChute" );

	    builder.ve.setProperty("runtime.log.logsystem.log4j.logger", "webhook.teamcity.Loggers");
	    builder.ve.setApplicationAttribute("webhook.teamcity.settings.secure.WebHookSecretResolver", webHookSecretResolver);
	    
		builder.ve.init();
		builder.resolver = resolver;
		return builder;
	}

	@Override
	public String build(String template) {
		StringWriter swParse1 =  new StringWriter();
		this.ve.evaluate(resolver, swParse1, "WebHookVelocityVariableMessageBuilder", template);
		StringWriter swParse2 =  new StringWriter();
		this.ve.evaluate(resolver, swParse2, "WebHookVelocityVariableMessageBuilder", swParse1.toString());
		return swParse2.toString();
	}

}
