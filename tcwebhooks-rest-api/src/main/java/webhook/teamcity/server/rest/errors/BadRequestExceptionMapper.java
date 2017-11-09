package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;
import webhook.teamcity.server.rest.model.template.ErrorResult;

@Provider
public class BadRequestExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<BadRequestException> {

  public Response toResponse(BadRequestException exception) {
	Response.ResponseBuilder builder = Response.status(400);
	if (exception.getResult() != null) {
		builder.entity(exception.getResult());
	} else {
		final ErrorResult errorResult = new ErrorResult();
		errorResult.addError("error", exception.getMessage());
		builder.entity(errorResult);
	}
	return builder.build();
  }

}