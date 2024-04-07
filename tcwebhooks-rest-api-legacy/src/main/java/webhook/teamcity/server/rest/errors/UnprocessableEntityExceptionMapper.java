package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;

@Provider
public class UnprocessableEntityExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<UnprocessableEntityException> {

  public Response toResponse(UnprocessableEntityException exception) {
	Response.ResponseBuilder builder = Response.status(422);
	builder.entity(exception.getResult());
	return builder.build();
  }

}