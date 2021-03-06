package webhook.teamcity.server.rest.model.webhook;

import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.settings.WebHookHeaderConfig;

@Getter @Setter
public class ProjectWebHookHeader extends WebHookHeaderConfig {
	
	private int id;
	
	String href;

	public static ProjectWebHookHeader copy(WebHookHeaderConfig header, Integer count, String href) {
		ProjectWebHookHeader t = new ProjectWebHookHeader();
		t.setId(count);
		t.setName(header.getName());
		t.setValue(header.getValue());
		t.setHref(href);
		return t;
	}

}
