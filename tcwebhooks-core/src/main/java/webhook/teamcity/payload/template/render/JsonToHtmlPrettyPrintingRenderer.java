package webhook.teamcity.payload.template.render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonToHtmlPrettyPrintingRenderer implements WebHookStringRenderer {

	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	JsonParser jp = new JsonParser();
	HtmlRenderer htmlr = new HtmlRenderer();

	@Override
	public String render(String uglyJSONString) throws WebHookHtmlRendererException {
		JsonElement je;
		try {
			je = jp.parse(uglyJSONString);
			return htmlr.render(gson.toJson(je).replaceAll("\\\\u003c", "<").replaceAll("\\\\u003e", ">"));
		} catch (Exception e){
			throw new WebHookHtmlRendererException(e);
		}
	}

}
