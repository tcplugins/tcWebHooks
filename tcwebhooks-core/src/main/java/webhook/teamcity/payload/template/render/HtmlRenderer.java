package webhook.teamcity.payload.template.render;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.Map;

public class HtmlRenderer implements WebHookStringRenderer {

    @Override
    public String render(String input) {
        return StringEscapeUtils.escapeHtml(input);
    }


    @Override
    public String render(Map<String, String[]> input) throws WebHookHtmlRendererException {
        throw new WebHookHtmlRendererException("Not expecting a Map<String,String[]>.");
    }

}
