package webhook.teamcity.payload.variableresolver.velocity;
import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.settings.secure.WebHookSecretResolver;

public class VelocitySecureDirective extends Directive {
	
	@Override
    public String getName() {
        return "secure";
    }
	@Override
    public int getType() {
        return LINE;
    }
	@Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
		
		WebHookSecretResolver webHookSecretResolver = (WebHookSecretResolver) this.rsvc.getApplicationAttribute("webhook.teamcity.settings.secure.WebHookSecretResolver");
		if (webHookSecretResolver == null) {
			return false;
		}
		
		if (node.jjtGetChild(0) != null) {
			String token = String.valueOf(node.jjtGetChild(0).value(context));

			if (context.get("project") instanceof SProject) {
				SProject project = (SProject) context.get("project");
			
				String secureToken = webHookSecretResolver.getSecret(project, token);
				if (secureToken != null) {
					writer.write(secureToken); // Write out the resolved token.
					return true;
				}
			}
			writer.write(token); // Or just write out the original token.
        }
        return true;
    }
} 