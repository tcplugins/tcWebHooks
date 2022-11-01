package webhook.teamcity.payload.variableresolver.velocity;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
public class VelocityHmacDirective extends Directive {

	@Override
	public String getName() {
		return "hmac";
	}
	@Override
	public int getType() {
		return LINE;
	}
	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
		//setting default params
		String algorithm = null;
		String key = null;
		String data = null;

		//reading params
		if (node.jjtGetChild(0) != null) {
			algorithm = String.valueOf(node.jjtGetChild(0).value(context));
		}

		if (node.jjtGetChild(1) != null) {
			key = String.valueOf(node.jjtGetChild(1).value(context));
		}

		if (node.jjtGetChild(2) != null) {
			data = String.valueOf(node.jjtGetChild(2).value(context));
		}

		// check if we read params ok, and return false if any issues.
		if (algorithm == null || algorithm.equals("null") || key == null || key.equals("null") || data == null || data.equals("null")) {
			return false; // Failed to render.
		}

		writer.write(new HmacUtils("Hmac" + algorithm.toUpperCase(), key).hmacHex(data));
		return true;
	}
}
