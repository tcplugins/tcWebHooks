package webhook;

import java.util.Date;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.impl.EnglishReasonPhraseCatalog;

import lombok.Data;
import lombok.NoArgsConstructor;
import webhook.teamcity.BuildStateEnum;

@Data @NoArgsConstructor
public class WebHookExecutionStats {
	
	UUID trackingId = UUID.randomUUID();
	Date initTimeStamp  = new Date();
	Date requestStartedTimeStamp;
	long preExecutionTime;
	Date requestCompletedTimeStamp;
	long requestExecutionTime;
	Date requestTeardownTimeStamp;
	long requestTeardownTime;
	String url;
	Integer statusCode;
	String statusReason;
	Header[] responseHeaders;
	boolean errored = false;
	boolean enabled = true;
	BuildStateEnum buildState;
	
	public WebHookExecutionStats(String url) {
		this.url = url;
	}

	public long getPreExecutionTime() {
		if (requestStartedTimeStamp == null) {
			return -1;
		}
		return requestStartedTimeStamp.getTime() - initTimeStamp.getTime();
	}
	
	public long getRequestExecutionTime() {
		if (requestStartedTimeStamp == null || requestCompletedTimeStamp == null) {
			return -1;
		}
		return requestCompletedTimeStamp.getTime() - requestStartedTimeStamp.getTime();
	}
	
	public long getRequestTeardownTime() {
		if (requestStartedTimeStamp == null || requestCompletedTimeStamp == null) {
			return -1;
		}
		return requestTeardownTimeStamp.getTime() - requestCompletedTimeStamp.getTime();
	}
	
	public long getTotalExecutionTime() {
		if (requestStartedTimeStamp == null) {
			return -1;
		}
		return requestTeardownTimeStamp.getTime() - initTimeStamp.getTime();
	}
	
	public void setRequestStarting() {
		this.requestStartedTimeStamp = new Date();
	}
	
	public void setRequestCompleted(int status) {
		this.requestCompletedTimeStamp = new Date();
		this.statusCode = status;
	}
	
	public void setRequestCompleted(int status, String statusReason) {
		this.requestCompletedTimeStamp = new Date();
		this.statusCode = status;
		if (statusReason == null || statusReason.isEmpty() ) {
			this.statusReason = EnglishReasonPhraseCatalog.INSTANCE.getReason(status, null);
		} else {
			this.statusReason = statusReason;
		}
	}
	
	public void setTeardownCompleted() {
		this.requestTeardownTimeStamp = new Date();
	}
	
	public String getTrackingIdAsString() {
		return this.trackingId.toString();
	}

	public void setResponseHeaders(Header[] allHeaders) {
		// TODO Auto-generated method stub
		
	}
}
