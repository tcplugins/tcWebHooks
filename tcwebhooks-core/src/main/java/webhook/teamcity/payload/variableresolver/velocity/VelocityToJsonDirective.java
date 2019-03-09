package webhook.teamcity.payload.variableresolver.velocity;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.payload.convertor.SuperclassExclusionStrategy;

public class VelocityToJsonDirective extends Directive {

	Gson gson = new GsonBuilder()
			.addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
			.addSerializationExclusionStrategy(new SuperclassExclusionStrategy())
			.create();

	@Override
	public String getName() {
		return "tojson";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
		Object object = null;
		if (node.jjtGetNumChildren() >= 2 && node.jjtGetChild(0) != null && node.jjtGetChild(1) != null) {
			object = node.jjtGetChild(0).value(context);
			String keyName = String.valueOf(node.jjtGetChild(1).value(context));
			writer.write("\"" + keyName + "\" : " + gson.toJson(object));
			return true;
		} else if (node.jjtGetChild(0) != null) {
			object = node.jjtGetChild(0).value(context);
			writer.write(gson.toJson(object));
			return true;
        }
		return false;
	}
}