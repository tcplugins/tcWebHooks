package webhook.teamcity.server.rest.model.webhook;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import webhook.teamcity.BuildStateEnum;

/*
 * <state type="buildStarted" enabled="true" />
 */

@XmlRootElement (name="state")
public class ProjectWebhookState {
	
	BuildStateEnum type; 
	Boolean enabled;
	
	@XmlAttribute
	public String getType() {
		return type.getShortName();
	}
	
	public void setType(String type) {
		this.type = BuildStateEnum.findBuildState(type);
	}
	
	@XmlAttribute
	public Boolean getEnabled() {
		return enabled;
	}

}
