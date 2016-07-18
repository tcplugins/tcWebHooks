package webhook.teamcity.payload.template.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;

public class XmlToHtmlPrettyPrintingRenderer implements WebHookStringRenderer {
	
	
	HtmlRenderer htmlr = new HtmlRenderer();

	@Override
	public String render(String uglyXmlString) throws WebHookHtmlRendererException {
		try {
			StringWriter sw = new StringWriter();  
			Document doc = DocumentHelper.parseText(uglyXmlString);  
			OutputFormat format = OutputFormat.createPrettyPrint();  
			XMLWriter xw = new XMLWriter(sw, format);  
			xw.write(doc);
			return "<pre><code clas=\"xml\">" + htmlr.render(sw.toString()) + "</code></pre>";
		} catch (DocumentException ex){
			throw new WebHookHtmlRendererException(ex);
		} catch (IOException e) {
			throw new WebHookHtmlRendererException(e);
		}

	}
	
	@Override
	public String render(Map<String, String[]> input) throws WebHookHtmlRendererException {
		throw new WebHookHtmlRendererException("Not expecting a Map<String,String[]>.");
	}

}
