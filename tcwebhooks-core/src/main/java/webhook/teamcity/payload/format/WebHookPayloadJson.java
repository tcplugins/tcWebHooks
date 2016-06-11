/**
 * 
 */
package webhook.teamcity.payload.format;

import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;
import webhook.teamcity.payload.template.render.JsonToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class WebHookPayloadJson extends WebHookPayloadGeneric implements WebHookPayload {
	
	Integer rank = 100;
	String charset = "UTF-8";
	
	public WebHookPayloadJson(WebHookPayloadManager manager){
		super(manager);
	}

	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "JSON";
	}

	public String getFormatShortName() {
		return "json";
	}

	public String getFormatToolTipText() {
		return "Send the payload formatted in JSON";
	}
	
	@Override
	protected String getStatusAsString(WebHookPayloadContent content,WebHookTemplateContent webHookTemplate){

		if (content.getExtraParameters().containsKey("showAllTeamCityParameters") && content.getExtraParameters().get("showAllTeamCityParameters").equalsIgnoreCase("true")){
			// let teamcity values through
		} else {
			//content.te
		}
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new ExtraParametersMapToJsonConvertor());
        xstream.alias("build", WebHookPayloadContent.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
		return xstream.toXML(content).replaceAll("\"@(name|value)\": \"(.*)\"", "\"$1\": \"$2\"");

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

	@Override
	public WebHookStringRenderer getWebHookStringRenderer() {
		return new JsonToHtmlPrettyPrintingRenderer();
	}
	
	

}
