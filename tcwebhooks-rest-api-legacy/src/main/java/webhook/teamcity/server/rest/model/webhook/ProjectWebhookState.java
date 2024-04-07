package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/*
 * <state type="buildStarted" enabled="true" />
 */

@XmlRootElement (name="state")
@XmlType (name = "state",propOrder = { "type", "enabled" })
public class ProjectWebhookState {
	
	@XmlAttribute
	public String type; 
	
	@XmlAttribute
	Boolean enabled;
	
}
