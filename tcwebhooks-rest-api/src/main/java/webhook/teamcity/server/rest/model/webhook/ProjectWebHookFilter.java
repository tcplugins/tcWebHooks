package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.settings.WebHookFilterConfig;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)

public class ProjectWebHookFilter  {
	
	@XmlAttribute
	private Integer id;
	private String value;
	private String regex;
	@XmlAttribute
	private boolean enabled;
	@XmlAttribute
	private String href;

	public static ProjectWebHookFilter copy(WebHookFilterConfig config, int id, String href){
		ProjectWebHookFilter t = new ProjectWebHookFilter();
		t.setId(id);
		t.setValue(config.getValue());
		t.setRegex(config.getRegex());
		t.setEnabled(config.isEnabled());
		t.setHref(href);
		return t;
	}
}
