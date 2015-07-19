/**
 * 
 */
package webhook.teamcity.payload.format;

import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public class WebHookPayloadTailoredJson extends WebHookPayloadGeneric implements WebHookPayload {
	
	public static final String FORMAT_SHORT_NAME = "tailoredjson";
	Integer rank = 101;
	String charset = "UTF-8";
	
	public WebHookPayloadTailoredJson(WebHookPayloadManager manager){
		super(manager);
	}

	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "Tailored JSON in body";
	}

	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	public String getFormatToolTipText() {
		return "Send a JSON payload with content specified by parameter named 'body'";
	}
	
	protected String getStatusAsString(WebHookPayloadContent content){

		return content.getExtraParameters().get("body");

	}

	public String getContentType() {
		return "application/json";
	}

	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getCharset() {
		return this.charset;
	}

}
