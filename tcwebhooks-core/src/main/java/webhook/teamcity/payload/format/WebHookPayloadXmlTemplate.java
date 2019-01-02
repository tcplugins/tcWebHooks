package webhook.teamcity.payload.format;

import com.thoughtworks.xstream.XStream;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.XmlToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadXmlTemplate extends WebHookPayloadGeneric implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "xmlTemplate";
	public static final String FORMAT_CONTENT_TYPE = "application/xml";

	Integer rank = 101;
	String charset = "UTF-8";

	XStream xstream = new XStream();

	public WebHookPayloadXmlTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager){
		super(manager, variableResolverManager);
		xstream.setMode(XStream.NO_REFERENCES);
	}

	@Override
	public void register(){
		myManager.registerPayloadFormat(this);
	}

	@Override
	public String getFormatDescription() {
		return "XML Standard template";
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatToolTipText() {
		return "Send an XML payload with content from a standard template";
	}

	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent){
		VariableMessageBuilder builder = this.myVariableResolverFactory.createVariableMessageBuilder(webHookTemplateContent.getTemplateText(), this.myVariableResolverFactory.buildVariableResolver(this, content, content.getAllParameters()));
		return builder.build();
	}

	@Override
	public String getContentType() {
		return FORMAT_CONTENT_TYPE;
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
		return new XmlToHtmlPrettyPrintingRenderer();
	}

	@Override
	public Object serialiseObject(Object object) {
		if (object instanceof String || object instanceof Boolean || object instanceof Integer || object instanceof Long) {
			return object;
		}
		return xstream.toXML(object);
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.STANDARD;
	}

}
