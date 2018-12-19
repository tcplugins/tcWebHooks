package webhook.teamcity.payload.variableresolver.velocity;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
public class VelocityNowDirective extends Directive {
	
	@Override
    public String getName() {
        return "now";
    }
	@Override
    public int getType() {
        return LINE;
    }
	@Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
        //setting default params
        String datePattern = null;
        //reading params
        if (node.jjtGetChild(0) != null) {
        	datePattern = String.valueOf(node.jjtGetChild(0).value(context));
        }
        //format the date and write result to writer
		SimpleDateFormat format = new SimpleDateFormat(datePattern);
        writer.write(format.format(new Date()));
        return true;
    }
} 