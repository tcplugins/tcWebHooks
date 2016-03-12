package webhook.teamcity.payload.util;
import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class VelocitySanitiseDirective extends Directive {
	
	@Override
    public String getName() {
        return "sanitise";
    }

	@Override
    public int getType() {
        return LINE;
    }

	@Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) 
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        //setting default params
        String dirtyString = null;

        //reading params
        if (node.jjtGetChild(0) != null) {
        	dirtyString = String.valueOf(node.jjtGetChild(0).value(context));
        }

        //sanitse the string and write result to writer
        writer.write(StringSanitiser.sanitise(dirtyString));

        return true;

    }

}