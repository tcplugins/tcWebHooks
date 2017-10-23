package webhook.teamcity.server.rest.errors;

@SuppressWarnings("serial")
public class TemplatePermissionException extends RuntimeException {

public TemplatePermissionException(String message) {
    super(message);
  }

  public TemplatePermissionException(String message, Throwable cause) {
    super(message, cause);
  }

}