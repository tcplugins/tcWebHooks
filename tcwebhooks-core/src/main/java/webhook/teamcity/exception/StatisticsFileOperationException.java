package webhook.teamcity.exception;

public class StatisticsFileOperationException extends Exception {

	public StatisticsFileOperationException(String message, Throwable e) {
		super(message, e);
	}
	
	public StatisticsFileOperationException(String message) {
		super(message);
	}

}
