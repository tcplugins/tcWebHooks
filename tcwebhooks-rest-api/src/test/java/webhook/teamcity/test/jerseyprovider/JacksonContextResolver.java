package webhook.teamcity.test.jerseyprovider;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {

    private ObjectMapper prettyPrintObjectMapper;
    private UriInfo uriInfoContext;

    public JacksonContextResolver(@Context UriInfo uriInfoContext) throws Exception {
        this.uriInfoContext = uriInfoContext;

        this.prettyPrintObjectMapper = new ObjectMapper();
        this.prettyPrintObjectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {

        try {
            MultivaluedMap<String, String> queryParameters = uriInfoContext.getQueryParameters();
            if(queryParameters.containsKey("pretty")) {
                return prettyPrintObjectMapper;
            }

        } catch(Exception e) {
            // protect from invalid access to uriInfoContext.getQueryParameters()
        }

        return null; // use default mapper
    }
}