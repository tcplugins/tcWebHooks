package webhook.teamcity.server.rest.errors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.intellij.openapi.diagnostic.Logger;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;


@Provider
public class UnprocessableEntityExceptionMapper extends ExceptionMapperUtil implements ExceptionMapper<UnprocessableEntityException> {
  protected static final Logger LOG = Logger.getInstance(UnprocessableEntityExceptionMapper.class.getName());

  public Response toResponse(UnprocessableEntityException exception) {
	Response.ResponseBuilder builder = Response.status(422);
	builder.entity(exception.getResult());
	return builder.build();
  }

}