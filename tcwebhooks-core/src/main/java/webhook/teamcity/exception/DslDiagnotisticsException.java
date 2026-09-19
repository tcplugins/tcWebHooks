package webhook.teamcity.exception;

import lombok.Getter;

public class DslDiagnotisticsException extends Exception {
	private static final long serialVersionUID = 1L;
	
	@Getter
	private final String path;

	public DslDiagnotisticsException(String message, String path) {
		super(message);
		this.path = path;
	}
}

