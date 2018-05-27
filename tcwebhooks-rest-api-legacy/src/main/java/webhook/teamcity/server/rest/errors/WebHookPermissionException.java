package webhook.teamcity.server.rest.errors;

@SuppressWarnings("serial")
public class WebHookPermissionException extends RuntimeException {

public WebHookPermissionException(String message) {
    super(message);
  }

  public WebHookPermissionException(String message, Throwable cause) {
    super(message, cause);
  }

}