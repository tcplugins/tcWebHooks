package webhook.teamcity.extension.bean.template;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class RegisteredWebHookTemplateBeanJsonSerialiser {

	private RegisteredWebHookTemplateBeanJsonSerialiser(){}
	
	public static String serialise(RegisteredWebHookTemplateBean templates){
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("webhookTemplates", RegisteredWebHookTemplateBean.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
		return xstream.toXML(templates);
	}

}
