package webhook.teamcity.payload.template.render;

import java.util.Map;

public class KotlinDslToHtmlPrettyPrintingRenderer implements WebHookStringRenderer {

    HtmlRenderer htmlr = new HtmlRenderer();

    @Override
    public String render(String uglyKotlinString) throws WebHookHtmlRendererException {
        return  "<pre><code class=\"language-kotlin\">" + htmlr.render(uglyKotlinString) + "</code></pre>";
    }
    
    @Override
    public String render(Map<String, String[]> input) throws WebHookHtmlRendererException {
        throw new WebHookHtmlRendererException("Not expecting a Map<String,String[]>.");
    }

}
