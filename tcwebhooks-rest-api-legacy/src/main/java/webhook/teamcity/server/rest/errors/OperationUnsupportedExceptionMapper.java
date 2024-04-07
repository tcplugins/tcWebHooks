package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;
import webhook.teamcity.exception.OperationUnsupportedException;
import webhook.teamcity.server.rest.model.template.ErrorResult;

@Provider
public class OperationUnsupportedExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<OperationUnsupportedException> {

  public Response toResponse(OperationUnsupportedException exception) {
	Response.ResponseBuilder builder = Response.status(501);
	final ErrorResult errorResult = new ErrorResult();
	errorResult.addError("error", exception.getMessage());
	builder.entity(errorResult);
	return builder.build();
  }

}