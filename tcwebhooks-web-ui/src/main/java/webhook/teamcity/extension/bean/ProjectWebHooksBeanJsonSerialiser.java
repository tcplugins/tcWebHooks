package webhook.teamcity.extension.bean;

import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class ProjectWebHooksBeanJsonSerialiser {
    private ProjectWebHooksBeanJsonSerialiser(){}
	
	public static String serialise(TemplatesAndProjectWebHooksBean project){
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new ExtraParametersMapToJsonConvertor());
        xstream.alias("templatesAndWebhooks", TemplatesAndProjectWebHooksBean.class);
        xstream.alias("webhookTemplates", RegisteredWebHookTemplateBean.class);
        xstream.alias("projectWebhookConfig", ProjectWebHooksBean.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
		return xstream.toXML(project);
	}

}
