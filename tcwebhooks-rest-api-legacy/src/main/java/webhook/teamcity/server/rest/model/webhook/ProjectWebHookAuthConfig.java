package webhook.teamcity.server.rest.model.webhook;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.auth.WebHookAuthConfig;

@XmlRootElement(name = "authentication")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ProjectWebHookAuthConfig {
	
	public ProjectWebHookAuthConfig(WebHookAuthConfig webHookAuthConfig) {
		this.type = webHookAuthConfig.getType();
		this.preemptive = webHookAuthConfig.getPreemptive();
		this.parameters.putAll(webHookAuthConfig.getParameters());
	}

	private String type = "";
	private Boolean preemptive = true;
	private Map<String,String> parameters = new HashMap<>();
	
	public WebHookAuthConfig toWebHookAuthConfig() {
		WebHookAuthConfig webHookAuthConfig = new WebHookAuthConfig();
		webHookAuthConfig.setType(getType());
		webHookAuthConfig.setPreemptive(getPreemptive());
		webHookAuthConfig.setParameters(getParameters());
		return webHookAuthConfig;
	}
}