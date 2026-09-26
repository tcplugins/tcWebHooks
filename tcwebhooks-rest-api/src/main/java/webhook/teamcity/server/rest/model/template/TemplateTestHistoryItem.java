package webhook.teamcity.server.rest.model.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "historyItem")
@ApiModel(description = "Execution result details returned after running a webhook template test.")
@Data @XmlAccessorType(XmlAccessType.FIELD)
public class TemplateTestHistoryItem {
	
	@XmlElement
	@ApiModelProperty(value = "Execution timestamp in server timezone.", example = "2026-09-28T10:23:45Z")
	String dateTime;
	
	@XmlElement(name = "error")
	@ApiModelProperty(value = "Optional error details when execution failed.")
	ErrorStatus error;

	@XmlAttribute 
	@ApiModelProperty(value = "Tracking id for correlating this execution in logs/history.", example = "WH-20260928-0001")
	String trackingId;
	
	@XmlElement
	@ApiModelProperty(value = "Webhook URL used during execution.", example = "https://hooks.example.com/teamcity")
	String url;
	
	@XmlElement
	@ApiModelProperty(value = "Total execution duration.", example = "153 ms")
	String executionTime;
	
	@XmlElement
	@ApiModelProperty(value = "HTTP status code returned by the remote endpoint.", example = "200")
	int statusCode;
	
	@XmlElement
	@ApiModelProperty(value = "HTTP reason phrase returned by the remote endpoint.", example = "OK")
	String statusReason;
	
	@Getter @NoArgsConstructor @AllArgsConstructor
	@ApiModel(description = "Error payload returned when webhook execution fails.")
	public static class ErrorStatus {
		@ApiModelProperty(value = "Error message from the failed execution.", example = "Connect timed out")
		String message;
		@ApiModelProperty(value = "Plugin-specific error code.", example = "1001")
		int errorCode;
	}
	
}
