package webhook.teamcity.extension.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class WebHookTestHistoryItem {
	
	String dateTime;
	ErrorStatus error;
	String trackingId;
	String url;
	String executionTime;
	int statusCode;
	
	String statusReason;
	
	@Getter @NoArgsConstructor @AllArgsConstructor
	public static class ErrorStatus {
		String message;
		int errorCode;
	}
	
}