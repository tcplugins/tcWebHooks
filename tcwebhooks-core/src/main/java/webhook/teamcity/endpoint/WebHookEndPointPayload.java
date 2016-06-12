package webhook.teamcity.endpoint;

import java.util.Date;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class WebHookEndPointPayload {

	int Min = 1000000, Max = 1000000000;
	Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
	
	Date date;
	String contentType;
	String payload;
	String prettyPayload;
	String hash = Rand.toString();
	String url;
	boolean parseFailure = false;
	Map<String, String> headers;
	
	public void setPrettyPayload(String pretty){
		this.prettyPayload = pretty;
	}
	
	public void setParseFailure(){
		this.parseFailure = true;
	}
	
	public String getPrettyPrintedPayload(){
		if (prettyPayload != null){
			return prettyPayload;
		}
		return payload;
	}
	
}
