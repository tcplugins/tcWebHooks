package webhook.teamcity.payload.format;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.WwwFormUrlEncodedToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;


public class WebHookPayloadNameValuePairs extends WebHookPayloadGeneric implements WebHookPayload {

	public static final String FORMAT_SHORT_NAME = "nvpairs";
	public static final String FORMAT_CONTENT_TYPE = "application/x-www-form-urlencoded";

	public WebHookPayloadNameValuePairs(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager) {
		super(manager, variableResolverManager);
	}

	Integer rank = 100;
	String charset = "UTF-8";

	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}

	public void register(){
		myManager.registerPayloadFormat(this);
	}

	public String getFormatDescription() {
		return "Name Value Pairs";
	}

	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	public String getFormatToolTipText() {
		return "Send the payload as a set of normal Name/Value Pairs";
	}

	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent){
		StringBuilder returnString = new StringBuilder();

		cleanContextContent(content);

		Map<String, String> contentMap = null;
		try {
			 contentMap = BeanUtils.describe(content);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			Loggers.SERVER.debug("It was not possible to convert 'content' into a bean", ex );
		}

		if (contentMap != null && contentMap.size() > 0){
			appendToBuilder(returnString, new ExtraParameters(contentMap), "content");
		}


		if (content != null && content.getExtraParameters(this.myVariableResolverFactory) != null  && !content.getExtraParameters(this.myVariableResolverFactory).isEmpty()){
			appendToBuilder(returnString, content.getExtraParameters(myVariableResolverFactory), "extra");
		}

		Loggers.SERVER.debug(this.getClass().getSimpleName() + ": payload is " + returnString.toString());
		if (returnString.length() > 0){
			return returnString.toString().substring(1);
		} else {
			return returnString.toString();
		}
	}

	private void appendToBuilder(StringBuilder stringBuilder, ExtraParameters inputMap, String logType) {
		for(Map.Entry<String, String> entry : inputMap.entrySet())
		{
			String pair = "&";
			try {
				if (entry.getKey() != null){
					pair += URLEncoder.encode(entry.getKey(), this.charset);
					if (entry.getValue() != null){
						pair += "=" + URLEncoder.encode(entry.getValue(), this.charset);
					} else {
						pair += "=" + URLEncoder.encode("null", this.charset);
					}
				}
			} catch (UnsupportedEncodingException | ClassCastException ex ) {
				pair = "";
				Loggers.SERVER.debug("failed to encode '" + logType + "' parameter '" + entry.getKey() + "' to URL format. Value has been converted to an empty string.", ex );
			}
			stringBuilder.append(pair);
		}
	}

	public String getContentType() {
		return FORMAT_CONTENT_TYPE;
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
		return new WwwFormUrlEncodedToHtmlPrettyPrintingRenderer();
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.LEGACY;
	}

}
