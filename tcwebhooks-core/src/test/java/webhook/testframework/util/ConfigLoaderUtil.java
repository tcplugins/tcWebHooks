package webhook.testframework.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
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
		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(0));
	}
	
	/**
	 * Gets the nth {@link WebHookConfig} from the file. 
	 * 1 based (not zero based) 
	 * @param itemNumber (1 based)
	 * @param plugin XML file from which to load the configuration
	 * @return WebHookConfig assembled from the configuration in the plugin XML file.
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static WebHookConfig getSpecificWebHookInConfig(int itemNumber, File f) throws JDOMException, IOException{
		Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(f);
		return new WebHookConfig((Element) fileAsElement.getChild("webhooks").getChildren("webhook").get(itemNumber -1));
	}
	
	public static List<SProjectFeatureDescriptor> getListOfProjectFeatures(File file) throws JDOMException, IOException{
	    Element fileAsElement = ConfigLoaderUtil.getFullConfigElement(file);
	    List<SProjectFeatureDescriptor> features = new ArrayList<>();
	    if (fileAsElement.getChild("project-extensions") != null) {
	        Element projectExtensions = fileAsElement.getChild("project-extensions"); 
	        projectExtensions.getChildren("extension").forEach(c -> {
	            Element c1 = (Element)c;
	            features.add(new FeatureDescriptor(
	                    c1.getAttribute("type").getValue(),
	                    toParameters(c1.getChildren("parameters")),
	                    "test",
	                    c1.getAttribute("id").getValue())
	                   );
	        });
	    }
	    return features;
	}
	
	@Data @AllArgsConstructor
	public static class FeatureDescriptor implements SProjectFeatureDescriptor {

        String type;
        Map<String, String> parameters;
        String projectId;
        String id;
        
        
	}
	
	public static Map<String,String> toParameters(List<Element> elements) {
	    System.out.println(elements.get(0).toString());
	    List<Element> params = elements.get(0).getChildren("param");
	    return params.stream().collect(
	            Collectors.toMap(e -> ((Element) e).getAttribute("name").getValue(), 
	                    e -> ((Element) e).getAttribute("value").getValue()));
	}
}
