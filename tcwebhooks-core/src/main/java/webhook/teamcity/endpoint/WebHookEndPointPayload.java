package webhook.teamcity.endpoint;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class WebHookEndPointPayload {

	Date date;
	String contentType;
	String payload;
	String prettyPayload;
	String hash;
	String url;
	@Builder.Default boolean parseFailure = false;
	Map<String, String> headers;
	Map<String, String[]> parameters;
	@Builder.Default Random random = new Random(); //NOSONAR We are not using this value for anything secure
	
	public WebHookEndPointPayload generateHash(){
		int min = 1000000;
		int max = 1000000000;
		Integer rand = min + (random.nextInt() * ((max - min) + 1));
		hash = rand.toString();
		return this;
	}
	
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
