package webhook.teamcity.payload.template.render;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

public class WwwFormUrlEncodedToHtmlPrettyPrintingRenderer implements WebHookStringRenderer {
	
	

	@Override
	public String render(String formEncodedString) throws WebHookHtmlRendererException {
		
		Map<String, String> keyValues = extractKeyValues(formEncodedString);
	    return renderToHtml(keyValues);

	}

	@Override
	public String render(Map<String, String[]> requestParameterMap) throws WebHookHtmlRendererException {
		
		HtmlRenderer htmlr = new HtmlRenderer();
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"tableWrapper\"><table class=\"settings\"><thead>");
		sb.append("<tr>");
		sb.append("<th>Key</th><th>Value</th></tr>");
		sb.append("</thead><tbody>");
		
		for (String key : requestParameterMap.keySet()){
			if (requestParameterMap.get(key).length == 0){
				sb.append("<tr><td>")
				  .append(htmlr.render(key))
				  .append("</td><td>&nbsp;</td></tr>");
			} else {
				for (int i = 0; i < requestParameterMap.get(key).length; i++) {
					sb.append("<tr><td>")
					  .append(htmlr.render(key))
					  .append("</td><td>")
					  .append(htmlr.render(requestParameterMap.get(key)[i]))
					  .append("</td></tr>");
				}
			}

		}

	    sb.append("</tbody></table></div>");
	    
	    return sb.toString();
	}
	
	protected String renderToHtml(Map<String, String> keyValues) {
		
		HtmlRenderer htmlr = new HtmlRenderer();
		
		StringBuilder sb = new StringBuilder();
	    sb.append("<table class=\"settings\"><thead>");
	    sb.append("<tr>");
	    sb.append("<th>Key</th><th>Value</th></tr>");
	    sb.append("</thead><tbody>");
	    
	    for (String key : keyValues.keySet()){
	    	sb.append("<tr><td>")
	    	  .append(htmlr.render(key))
	    	  .append("</td><td>")
	    	  .append(htmlr.render(keyValues.get(key)))
	    	  .append("</td></tr>");
	    }
	    
	    sb.append("</tbody></table>");
	    
	    return sb.toString();
	}

	protected Map<String, String> extractKeyValues(String formEncodedString) throws WebHookHtmlRendererException {
		Map<String,String> keyValues= new TreeMap<>();

		try {
		    String[] pairs = formEncodedString.split("\\&");
		    for (int i = 0; i < pairs.length; i++) {
		      String[] fields = pairs[i].split("=");
		      String name = URLDecoder.decode(fields[0], "UTF-8");
		      String value;
		      try {
		    	  value = URLDecoder.decode(fields[1], "UTF-8");
		      } catch (ArrayIndexOutOfBoundsException arrayEx){
		    	  value = "";
		      }
		      keyValues.put(name, value);
		    }
		} catch (UnsupportedEncodingException uee){
			throw new WebHookHtmlRendererException(uee);
		}
		return keyValues;
	}

}
