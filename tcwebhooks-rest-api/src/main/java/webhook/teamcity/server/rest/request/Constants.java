package webhook.teamcity.server.rest.request;
public class Constants {
  public static final String DEFAULT_PAGE_ITEMS_COUNT = "100";
  public static final int DEFAULT_PAGE_ITEMS_COUNT_INT = 100;

  public static final String API_URL = "/app/rest/webhooks";

  public static final String BIND_PATH_PROPERTY_NAME = "api.path";
  public static final String ORIGINAL_REQUEST_URI_HEADER_NAME = "original-request-uri";

  public static final String EXTERNAL_APPLICATION_WADL_NAME = "/application.wadl"; //name that user requests will use
  public static final String JERSEY_APPLICATION_WADL_NAME = "/application.wadl";
}