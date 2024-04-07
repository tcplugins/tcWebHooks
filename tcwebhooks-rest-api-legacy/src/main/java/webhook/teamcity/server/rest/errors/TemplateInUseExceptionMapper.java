package webhook.teamcity.server.rest.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.jersey.ExceptionMapperUtil;

@Provider
public class TemplateInUseExceptionMapper  extends ExceptionMapperUtil implements ExceptionMapper<TemplateInUseException> {

	public Response toResponse(TemplateInUseException exception) {
		Response.ResponseBuilder builder = Response.status(409);
		builder.entity(exception.getResult());
		return builder.build();
	}
}

