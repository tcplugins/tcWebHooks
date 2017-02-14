package webhook.teamcity.extension.bean.template;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import webhook.teamcity.payload.convertor.ExtraParametersMapToJsonConvertor;

public class TemplateRenderingBeanJsonSerialiser {
    private TemplateRenderingBeanJsonSerialiser() {
    }

    public static String serialise(TemplateRenderingBean templateRendering) {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new ExtraParametersMapToJsonConvertor());
        xstream.alias("templatesOutput", TemplateRenderingBean.class);
        /* For some reason, the items are coming back as "@name" and "@value"
         * so strip those out with a regex.
         */
        return xstream.toXML(templateRendering);
    }

}
