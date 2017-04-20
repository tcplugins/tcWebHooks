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
		return requestStartedTimeStamp.getTime() - initTimeStamp.getTime();
	}
	
	public long getRequestExecutionTime() {
		return requestCompletedTimeStamp.getTime() - requestStartedTimeStamp.getTime();
	}
	
	public long getRequestTeardownTime() {
		return requestTeardownTimeStamp.getTime() - requestCompletedTimeStamp.getTime();
	}
	
	public long getTotalExecutionTime() {
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
