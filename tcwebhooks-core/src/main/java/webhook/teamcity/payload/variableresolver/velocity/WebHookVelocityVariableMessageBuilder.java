package webhook.teamcity.payload.variableresolver.velocity;

import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.impl.SimpleLoggerFactory;

import webhook.teamcity.WebHookTemplateParsingException;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.settings.secure.WebHookSecretResolver;

public class WebHookVelocityVariableMessageBuilder implements VariableMessageBuilder {

	private static final String PACKAGE = "webhook.teamcity.payload.variableresolver.velocity.";
	Context resolver;
	VelocityEngine ve ;

	public static WebHookVelocityVariableMessageBuilder create(Context resolver, WebHookSecretResolver webHookSecretResolver){
		WebHookVelocityVariableMessageBuilder builder = new WebHookVelocityVariableMessageBuilder();
		builder.ve = new VelocityEngine();

		builder.ve.setProperty(RuntimeConstants.CUSTOM_DIRECTIVES, PACKAGE + "VelocitySanitiseDirective, "
											+ PACKAGE + "VelocitySanitizeDirective, "
											+ PACKAGE + "VelocityEscapeJsonDirective, "
											+ PACKAGE + "VelocityCapitaliseDirective, "
											+ PACKAGE + "VelocityCapitalizeDirective, "
											+ PACKAGE + "VelocityNowDirective, "
											+ PACKAGE + "VelocitySubStringDirective, "
											+ PACKAGE + "VelocityToJsonDirective,"
											+ PACKAGE + "VelocityHmacDirective,"
											+ PACKAGE + "VelocitySecureDirective");


		builder.ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, new SimpleLoggerFactory().getLogger("jetbrains.buildServer.SERVER"));
		builder.ve.setApplicationAttribute("webhook.teamcity.settings.secure.WebHookSecretResolver", webHookSecretResolver);

		builder.ve.init();
		builder.resolver = resolver;
		return builder;
	}

	@Override
	public String build(String template) {
		try {
			StringWriter swParse1 =  new StringWriter();
			this.ve.evaluate(this.resolver, swParse1, "WebHookVelocityVariableMessageBuilder", template);
			StringWriter swParse2 =  new StringWriter();
			this.ve.evaluate(this.resolver, swParse2, "WebHookVelocityVariableMessageBuilder", swParse1.toString());
			return swParse2.toString();
		} catch (ParseErrorException ex) {
			throw new WebHookTemplateParsingException(ex.getMessage());
		}
	}

	@Override
	public void addWebHookPayload(String webHookPayload) {
		this.resolver.put("webhookPayload", webHookPayload);
	}

}
