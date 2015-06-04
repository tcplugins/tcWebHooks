package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * <?xml version="1.0" encoding="UTF-8"?>
<settings>
  <webhooks enabled="true">
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
  </webhooks>
</settings>


 */

@XmlRootElement(name = "webhooks")
public class ProjectWebhooks {
	private Boolean enabled;
	
	private List<ProjectWebhook> webhooks = new ArrayList<ProjectWebhook>();
	
	@XmlAttribute
	public Boolean getEnabled() {
		return enabled;
	}

	@XmlElement(name = "webhook")
	public List<ProjectWebhook> getWebhooks() {
		return webhooks;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void addWebhook(ProjectWebhook webhook){
		this.webhooks.add(webhook);
	}
	
}
