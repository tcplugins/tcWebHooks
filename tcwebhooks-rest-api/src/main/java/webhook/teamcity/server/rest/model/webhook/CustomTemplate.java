package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * 		<custom-template type="buildStatusHtml" template="${branchDisplayName} ${projectId}" enabled="true"/>
 */

@XmlRootElement(name="custom-template")
public class CustomTemplate {
	private String type;
	private String template;
	private Boolean enabled;
	
	@XmlAttribute
	public String getType() {
		return type;
	}
	
	@XmlAttribute
	public String getTemplate() {
		return template;
	}
	
	@XmlAttribute
	public Boolean getEnabled() {
		return enabled;
	}
	
}
