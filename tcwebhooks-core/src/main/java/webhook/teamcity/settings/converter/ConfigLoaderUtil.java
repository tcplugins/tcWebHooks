package webhook.teamcity.settings.converter;
import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import webhook.teamcity.settings.WebHookProjectSettings;

public class ConfigLoaderUtil {
	private ConfigLoaderUtil(){}
	

	public static WebHookProjectSettings getAllWebHooksInConfig(File f) throws JDOMException, IOException {
		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
		Element e = fileAsElement.getChild("webhooks");
		if (e != null) {
			WebHookProjectSettings webHookProjectSettings = new WebHookProjectSettings();
			webHookProjectSettings.readFrom(e);
			return webHookProjectSettings;
		} else {
			return null;
		}
	}
	
	private static Element getFullConfigElement(File file) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		Document doc = builder.build(file);
		return doc.getRootElement();
	}
//	public static WebHookConfig getFirstWebHookInConfig(File f) throws JDOMException, IOException{
//		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
//		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(0));
//	}
//	
//	public static WebHookConfig getSpecificWebHookInConfig(int itemNumber, File f) throws JDOMException, IOException{
//		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
//		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(itemNumber -1));
//	}
	
}
