package webhook.teamcity.payload.template.render;

import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

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
			return htmlr.render(sw.toString());
		} catch (DocumentException ex){
			throw new WebHookHtmlRendererException(ex);
		} catch (IOException e) {
			throw new WebHookHtmlRendererException(e);
		}

	}

}
