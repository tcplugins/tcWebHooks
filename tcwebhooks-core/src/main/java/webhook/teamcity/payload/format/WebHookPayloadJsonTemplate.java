package webhook.teamcity.payload.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.convertor.SuperclassExclusionStrategy;
import webhook.teamcity.payload.template.render.JsonToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadJsonTemplate extends WebHookPayloadGeneric implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "jsonTemplate";
	Integer rank = 101;
	String charset = "UTF-8";

	Gson gson = new GsonBuilder()
			.addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
			.addSerializationExclusionStrategy(new SuperclassExclusionStrategy())
			.create();

	public WebHookPayloadJsonTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager){
		super(manager, variableResolverManager);
	}

	@Override
	public void register(){
		myManager.registerPayloadFormat(this);
	}

	@Override
	public String getFormatDescription() {
		return "JSON Standard template";
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a JSON payload with content from a standard template";
	}

	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent){
		VariableMessageBuilder builder = this.myVariableResolverFactory.createVariableMessageBuilder(webHookTemplateContent.getTemplateText(), this.myVariableResolverFactory.buildVariableResolver(this, content, content.getAllParameters()));
		return builder.build();
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public Integer getRank() {
		return this.rank;
	}

	@Override
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@Override
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

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.STANDARD;
	}
}
