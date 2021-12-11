package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.settings.WebHookHeaderConfig;

@Getter @Setter
@XmlAccessorType(XmlAccessType.FIELD)

public class ProjectWebHookHeader {
	
	@XmlAttribute
	private int id;
	private String name;
	private String value;

	@XmlAttribute
	private String href;

	public static ProjectWebHookHeader copy(WebHookHeaderConfig header, Integer count, String href) {
		ProjectWebHookHeader t = new ProjectWebHookHeader();
		t.setId(count);
		t.setName(header.getName());
		t.setValue(header.getValue());
		t.setHref(href);
		return t;
	}

}
