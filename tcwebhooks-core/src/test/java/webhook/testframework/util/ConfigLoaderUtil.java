package webhook.testframework.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import webhook.teamcity.settings.WebHookConfig;

public class ConfigLoaderUtil {
	private ConfigLoaderUtil(){}
	
	public static Element getFullConfigElement(File file) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		Document doc = builder.build(file);
		return doc.getRootElement();
	}

	public static WebHookConfig getFirstWebHookInConfig(File f) throws JDOMException, IOException{
		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
		assertTrue("One and only one webhook expected when loading test config from file : " + f.getName(), fileAsElement.getChild("webhooks").getChildren("webhook").size() == 1);
		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(0));
	}
	
}
