package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import webhook.teamcity.settings.WebHookConfig;

/*
	<webhook url="http://localhost/test" enabled="true" format="nvpairs">
	<states>
	  <state type="buildStarted" enabled="true" />
	  <state type="beforeBuildFinish" enabled="true" />
	  <state type="buildFinished" enabled="true" />
	  <state type="buildBroken" enabled="false" />
	  <state type="buildInterrupted" enabled="true" />
	  <state type="buildSuccessful" enabled="true" />
	  <state type="buildFixed" enabled="false" />
	  <state type="buildFailed" enabled="true" />
	  <state type="responsibilityChanged" enabled="true" />
	</states>
	 <parameters>
	  <param name="color" value="red" />
	  <param name="notify" value="1" />
	</parameters>
	<custom-templates>
		<custom-template type="buildStatusHtml" template="${branchDisplayName} ${projectId}" enabled="true"/>
	</custom-templates>
	</webhook>
*/
@XmlRootElement(name = "webhook")
public class ProjectWebhook {
	private String url;
	private Boolean enabled;
	private String format;
	
	private List<ProjectWebhookState> states = new ArrayList<ProjectWebhookState>();
	private List<ProjectWebhookParameter> parameters = new ArrayList<ProjectWebhookParameter>();
	private List<CustomTemplate> customTemplates = new ArrayList<CustomTemplate>();
	
	public ProjectWebhook(WebHookConfig config) {
		// TODO Auto-generated constructor stub
	}

	@XmlAttribute
	public String getUrl() {
		return url;
	}
	
	@XmlAttribute
	public Boolean getEnabled() {
		return enabled;
	}
	
	@XmlAttribute
	public String getFormat() {
		return format;
	}
	
	@XmlElement
	public List<ProjectWebhookState> getStates() {
		return states;
	}

	@XmlElement(name = "parameters")
	public List<ProjectWebhookParameter> getParameters() {
		return parameters;
	}
	
	
	@XmlElement(name = "custom-templates")
	public List<CustomTemplate> getCustomTemplates() {
		return customTemplates;
	}

}
