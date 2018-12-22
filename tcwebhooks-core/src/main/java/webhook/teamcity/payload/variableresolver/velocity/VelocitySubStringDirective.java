package webhook.teamcity.payload.variableresolver.velocity;
import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import webhook.teamcity.payload.util.StringUtils;
public class VelocitySubStringDirective extends Directive {
	
	@Override
    public String getName() {
        return "substr";
    }
	@Override
    public int getType() {
        return LINE;
    }
	@Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
        //setting default params
        String text = null;
        int startIndex = -1;
        int endIndex = -1;
        int minLength = -1;
        
        //reading params
        if (node.jjtGetChild(0) != null) {
        	text = String.valueOf(node.jjtGetChild(0).value(context));
        }

        if (node.jjtGetChild(1) != null) {
        	startIndex = (Integer)node.jjtGetChild(1).value(context);
        }

        if (node.jjtGetChild(2) != null) {
        	endIndex = (Integer)node.jjtGetChild(2).value(context);
        }

        if (node.jjtGetChild(3) != null) {
        	minLength = (Integer)node.jjtGetChild(3).value(context);
        }
        
        // check if we read params ok, and return false if any issues.
        if (text == null || text.equals("null") || startIndex == -1 || endIndex == -1 || minLength == -1) {
        	return false; // Failed to render.
        }
        
        writer.write(StringUtils.subString(text, startIndex, endIndex, minLength));
        return true;
    }
} 

