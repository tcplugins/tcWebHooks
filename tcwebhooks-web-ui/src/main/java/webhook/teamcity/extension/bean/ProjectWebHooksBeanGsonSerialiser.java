package webhook.teamcity.extension.bean;

import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean.TemplatesAndProjectWebHooksBeanResponseWrapper;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class ProjectWebHooksBeanGsonSerialiser {
    private ProjectWebHooksBeanGsonSerialiser(){}
	
	public static String serialise(TemplatesAndProjectWebHooksBeanResponseWrapper project){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			     .registerTypeAdapter(Id.class, new IdTypeAdapter())
//			     .enableComplexMapKeySerialization()
//			     .serializeNulls()
//			     .setDateFormat(DateFormat.LONG)
//			     .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
//			     .setPrettyPrinting()
//			     .setVersion(1.0)
//			     .create(); XStream(new JsonHierarchicalStreamDriver());
//        gson.setMode(XStream.NO_REFERENCES);
//        gson.registerConverter(new ExtraParametersMapToJsonConvertor());
//        gson.alias("templatesAndWebhooks", TemplatesAndProjectWebHooksBean.class);
//        gson.alias("webhookTemplates", RegisteredWebHookTemplateBean.class);
//        gson.alias("projectWebhookConfig", ProjectWebHooksBean.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
		return gson.toJson(project);
	}

}
