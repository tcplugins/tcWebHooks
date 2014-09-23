/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.Collection;
import java.util.SortedMap;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.tests.TestName;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class WebHookPayloadTailoredJson extends WebHookPayloadGeneric implements WebHookPayload {
	
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
		return "tailoredjson";
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
