package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="parameter")
public class ProjectWebhookParameter {

	private String name;
	private String value;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public String getValue() {
		return value;
	}
}
