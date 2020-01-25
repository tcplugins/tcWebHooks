package webhook.teamcity.exception;

import lombok.Getter;

public class NonExistantProjectException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	@Getter
	private final String projectID;

	public NonExistantProjectException(String message, String projectId) {
		super(message);
		this.projectID = projectId;
	}
}

