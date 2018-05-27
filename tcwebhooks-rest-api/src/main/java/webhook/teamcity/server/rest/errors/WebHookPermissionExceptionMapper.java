package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;

@Provider
public class WebHookPermissionExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<WebHookPermissionException> {

  public Response toResponse(WebHookPermissionException exception) {
	Response.ResponseBuilder builder = Response.status(403);
	builder.entity(exception.getMessage());
	return builder.build();
  }

}