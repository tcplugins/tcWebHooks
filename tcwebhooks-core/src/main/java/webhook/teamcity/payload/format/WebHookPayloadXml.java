package webhook.teamcity.payload.format;

import com.thoughtworks.xstream.XStream;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.convertor.ExtraParametersMapToXmlConvertor;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.XmlToHtmlPrettyPrintingRenderer;

public class WebHookPayloadXml extends WebHookPayloadGeneric {

    private Integer rank = 100;

    public WebHookPayloadXml(WebHookPayloadManager wpm) {
        super(wpm);
    }

    public void register() {
        myManager.registerPayloadFormat(this);
    }

    public String getCharset() {
        return "UTF-8";
    }

    public String getContentType() {
        return "text/xml";
    }

    public String getFormatDescription() {
        return "XML";
    }

    public String getFormatShortName() {
        return "xml";
    }

    public String getFormatToolTipText() {
        return "Send the payload formatted in XML";
    }


    public Integer getRank() {
        return this.rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    protected String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplateContent) {
        XStream xstream = new XStream();
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new ExtraParametersMapToXmlConvertor());
        xstream.alias("build", WebHookPayloadContent.class);
        return xstream.toXML(content);
    }

    @Override
    public WebHookStringRenderer getWebHookStringRenderer() {
        return new XmlToHtmlPrettyPrintingRenderer();
    }

}
