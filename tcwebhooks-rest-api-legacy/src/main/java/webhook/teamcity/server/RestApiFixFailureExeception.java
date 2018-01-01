package webhook.teamcity.server;

public class RestApiFixFailureExeception extends Exception {

	private static final long serialVersionUID = -4960062229471431649L;
	
	public RestApiFixFailureExeception(Exception e) {
		super(e);
	}

}
