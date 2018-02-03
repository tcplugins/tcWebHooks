package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;

@Provider
public class JaxbClassCastExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<JaxbClassCastException> {

  public Response toResponse(JaxbClassCastException exception) {
	Response.ResponseBuilder builder = Response.status(422);
	builder.entity(exception.getResult());
	return builder.build();
  }

}