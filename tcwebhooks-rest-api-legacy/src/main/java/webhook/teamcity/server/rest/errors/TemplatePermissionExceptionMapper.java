package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;

@Provider
public class TemplatePermissionExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<TemplatePermissionException> {

  public Response toResponse(TemplatePermissionException exception) {
	Response.ResponseBuilder builder = Response.status(403);
	builder.entity(exception.getMessage());
	return builder.build();
  }

}