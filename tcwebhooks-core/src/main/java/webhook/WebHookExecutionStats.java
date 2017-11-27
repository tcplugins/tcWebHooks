package webhook;

import java.util.Date;

import org.apache.commons.httpclient.Header;

import lombok.Data;

@Data
public class WebHookExecutionStats {
	
	Date initTimeStamp  = new Date();
	Date requestStartedTimeStamp;
	long preExecutionTime;
	Date requestCompletedTimeStamp;
	long requestExecutionTime;
	Date requestTeardownTimeStamp;
	long requestTeardownTime;
	String url;
	Integer httpStatusCode;
	Header[] headers;

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
		this.httpStatusCode = status;
	}
	
	public void setTeardownCompleted() {
		this.requestTeardownTimeStamp = new Date();
	}
	
}
