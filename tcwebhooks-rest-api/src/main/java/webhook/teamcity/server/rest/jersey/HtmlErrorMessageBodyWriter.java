package webhook.teamcity.server.rest.jersey;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import webhook.teamcity.server.rest.model.template.ErrorResult;

@Provider
@Produces("text/html")
public class HtmlErrorMessageBodyWriter implements MessageBodyWriter<ErrorResult> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ErrorResult.class;
    }

    @Override
    public long getSize(ErrorResult errorResult, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
    	//
    	// Jersey 1.x requires a content length.
    	//
        return buildOutput(errorResult).length();
    }

    @Override
    public void writeTo(ErrorResult errorResult, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream out) throws IOException, WebApplicationException {

        Writer writer = new PrintWriter(out);
        writer.write(buildOutput(errorResult));
        writer.flush();
        writer.close();
    }
    
    private String buildOutput(ErrorResult errorResult) {
    	StringBuilder stringBuilder = new StringBuilder("<html>\n").append("<body><h2>Errors</h2><ul>");
        for (Entry<String,String> entry : errorResult.getErrors().entrySet()) {
        	stringBuilder.append("<li>").append(entry.getKey()).append(" : ").append(entry.getValue()).append("</li>");
        }
        stringBuilder.append("</ul></body></html>");
        return stringBuilder.toString();
    }
}