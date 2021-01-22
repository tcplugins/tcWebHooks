package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;

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
        <state type="buildTagged" enabled="false" />
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType (name = "webhooks",propOrder = { "count", "projectId", "enabled", "webhooks" })
@Getter @Setter
public class ProjectWebhooks {
	
	@XmlAttribute
	Integer count;
	
	@XmlAttribute
	Boolean enabled;

	@XmlAttribute
	String projectId;
	
	@XmlElementWrapper
	List<ProjectWebhook> webhooks = new ArrayList<>();
	
	public void addWebhook(ProjectWebhook webhook){
		this.webhooks.add(webhook);
	}
	
}
