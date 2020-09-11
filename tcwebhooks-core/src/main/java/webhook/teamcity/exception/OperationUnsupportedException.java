package webhook.teamcity.exception;

@SuppressWarnings("serial")
public class OperationUnsupportedException extends RuntimeException {

	public OperationUnsupportedException(String string) {
		super(string);
	}

}
