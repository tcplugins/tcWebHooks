/**
 * 
 */
package webhook.teamcity.payload.format;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.template.render.JsonToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadTailoredJson extends WebHookPayloadGeneric implements WebHookPayload {
	
	public static final String FORMAT_SHORT_NAME = "tailoredjson";
	Integer rank = 101;
	String charset = "UTF-8";
	
	public WebHookPayloadTailoredJson(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager){
		super(manager, variableResolverManager);
	}

	@Override
	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	@Override
	public String getFormatDescription() {
		return "Tailored JSON in body";
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a JSON payload with content specified by parameter named 'body'";
	}

	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplate) {
		try {
			return content.getExtraParameters(getVariableResolverFactory()).get("body");
		} catch (NullPointerException npe){
			throw new WebHookPayloadContentAssemblyException("Failure building message content :: Unable to retreive 'body' content.");
		}	
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
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.LEGACY;
	}	
}
