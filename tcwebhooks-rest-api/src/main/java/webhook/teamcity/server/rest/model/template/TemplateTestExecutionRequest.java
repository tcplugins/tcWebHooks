package webhook.teamcity.server.rest.model.template;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class TemplateTestExecutionRequest {

	String format;
	String templateText;
	String branchTemplateText;
	boolean useTemplateTextForBranch;
	String buildId;
	String projectExternalId;
	String url;
	String webhookId;
	String buildStateName;
}
