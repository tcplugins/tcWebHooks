package webhook.teamcity.payload.template.render;

import java.util.Map;

public class XmlToHtmlPrettyPrintingRenderer implements WebHookStringRenderer {
	
	
	HtmlRenderer htmlr = new HtmlRenderer();

	@Override
	public String render(String uglyXmlString) throws WebHookHtmlRendererException {
		return  "<pre><code class=\"xml\">" + htmlr.render(uglyXmlString) + "</code></pre>";
	}
	
	@Override
	public String render(Map<String, String[]> input) throws WebHookHtmlRendererException {
		throw new WebHookHtmlRendererException("Not expecting a Map<String,String[]>.");
	}

}
