/**
 * 
 */
package webhook.teamcity.payload.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.template.render.JsonToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHooksBeanUtilsVariableResolver;

public class WebHookPayloadJsonTemplate extends WebHookPayloadGeneric implements WebHookPayload, WebHookContentObjectSerialiser {
	
	public static final String FORMAT_SHORT_NAME = "jsonTemplate";
	Integer rank = 101;
	String charset = "UTF-8";
	
	Gson gson = new GsonBuilder().create();
	
	public WebHookPayloadJsonTemplate(WebHookPayloadManager manager){
		super(manager);
	}

	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "JSON";
	}

	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	public String getFormatToolTipText() {
		return "Send a JSON payload with content from a template";
	}
	
	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent){
		VariableMessageBuilder builder = VariableMessageBuilder.create(webHookTemplateContent.getTemplateText(), new WebHooksBeanUtilsVariableResolver(this, content, content.getAllParameters()));
		return builder.build();
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

	@Override
	public Object serialiseObject(Object object) {
		if (object instanceof String || object instanceof Boolean || object instanceof Integer || object instanceof Long) {
			return object;
		}
		return gson.toJson(object);
	}
}
