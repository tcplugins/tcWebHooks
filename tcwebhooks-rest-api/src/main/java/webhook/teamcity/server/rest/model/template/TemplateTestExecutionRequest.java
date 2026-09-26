package webhook.teamcity.server.rest.model.template;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@ApiModel(description = "Payload for executing or previewing a webhook template against a selected build state.")
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class TemplateTestExecutionRequest {

	@ApiModelProperty(value = "Payload format identifier to test, for example jsontemplate or xmltemplate.", example = "jsontemplate")
	String format;
	@ApiModelProperty(value = "Template body to render for the test run.", example = "{\"text\":\"Build ${buildName} finished\"}")
	String templateText;
	@ApiModelProperty(value = "Optional branch-specific template body.", example = "{\"text\":\"Branch ${branchName}: ${buildName}\"}")
	String branchTemplateText;
	@ApiModelProperty(value = "When true, use templateText for branch notifications as well.", example = "false")
	boolean useTemplateTextForBranch;
	@ApiModelProperty(value = "TeamCity build id used as test context.", example = "12345")
	String buildId;
	@ApiModelProperty(value = "Project external id used to resolve project-level settings.", example = "_Root")
	String projectExternalId;
	@ApiModelProperty(value = "Target webhook URL used for execute (ignored for preview).", example = "https://hooks.example.com/teamcity")
	String url;
	@ApiModelProperty(value = "Webhook identifier for execute tests.", example = "webhook.id:slack-main")
	String webhookId;
	@ApiModelProperty(value = "Build state name to simulate.", example = "buildFinished")
	String buildStateName;
}
