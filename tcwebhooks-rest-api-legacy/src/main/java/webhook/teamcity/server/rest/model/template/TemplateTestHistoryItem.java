package webhook.teamcity.server.rest.model.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
@Data @XmlAccessorType(XmlAccessType.FIELD)
public class TemplateTestHistoryItem {
	
	@XmlElement
	String datetime;
	
	@XmlElement(name = "error")
	ErrorStatus error;

	@XmlAttribute 
	String trackingId;
	
	@XmlElement
	String url;
	
	@XmlElement
	String executionTime;
	
	@XmlElement
	int statusCode;
	
	@XmlElement
	String statusReason;
	
	@Getter @NoArgsConstructor @AllArgsConstructor
	public static class ErrorStatus {
		String message;
		int errorCode;
	}
	
}
