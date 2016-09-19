package webhook.teamcity.extension.bean.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class RegisteredWebHookTemplateBeanGsonSerialiser {

	private RegisteredWebHookTemplateBeanGsonSerialiser(){}
	
	public static String serialise(RegisteredWebHookTemplateBean templates){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
//		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
//        xstream.setMode(XStream.NO_REFERENCES);
//        xstream.alias("webhookTemplates", RegisteredWebHookTemplateBean.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
		return gson.toJson(templates);
	}

}
