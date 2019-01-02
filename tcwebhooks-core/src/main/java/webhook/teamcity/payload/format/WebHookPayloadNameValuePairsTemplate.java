package webhook.teamcity.payload.format;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.WwwFormUrlEncodedToHtmlPrettyPrintingRenderer;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadNameValuePairsTemplate extends WebHookPayloadGeneric implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "nvpairsTemplate";
	public static final String FORMAT_CONTENT_TYPE = "application/x-www-form-urlencoded";

	Integer rank = 101;
	String charset = "UTF-8";

	public WebHookPayloadNameValuePairsTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager){
		super(manager, variableResolverManager);
	}

	@Override
	public void register(){
		myManager.registerPayloadFormat(this);
	}

	@Override
	public String getFormatDescription() {
		return "Name Value Pairs - urlencoded Standard template";
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a x-www-form-urlencoded payload with content from a standard template";
	}

	@Override
	protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent){
		VariableMessageBuilder builder = this.myVariableResolverFactory.createVariableMessageBuilder(webHookTemplateContent.getTemplateText(), this.myVariableResolverFactory.buildVariableResolver(this, content, content.getAllParameters()));
		try {
			return URLEncodedUtils.format(parseToNvPairs(builder.build()), Charset.forName(getCharset()));
		} catch (Exception ex) {
			throw new WebHookPayloadContentAssemblyException("Failed to parse template input. Check the input is correct");
		}
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
		return new WwwFormUrlEncodedToHtmlPrettyPrintingRenderer();
	}

	@Override
	public Object serialiseObject(Object object) {
		return object;
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.STANDARD;
	}

	public List<NameValuePair> parseToNvPairs(String s) {
		List<NameValuePair> nvPairs = new ArrayList<>();
		for (String line : s.split("\n")) {
			if (! line.trim().isEmpty()) {
				String[] item = line.trim().split("=", 2);
				nvPairs.add(new BasicNameValuePair(item[0], item[1]));
			}
		}
	    return nvPairs;
	}
}
