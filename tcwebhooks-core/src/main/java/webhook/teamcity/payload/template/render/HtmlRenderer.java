package webhook.teamcity.payload.template.render;

import org.apache.commons.lang.StringEscapeUtils;

public class HtmlRenderer implements WebHookStringRenderer {

	@Override
	public String render(String input) {
		return StringEscapeUtils.escapeHtml(input);
	}

}
