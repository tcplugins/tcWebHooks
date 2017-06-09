package webhook.teamcity.server.rest.model.template;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data @XmlRootElement
public class TemplateValidationResult {
	
	boolean isErrored = false;
	Map<String, String> errors = new LinkedHashMap<>();
	
	public void addError(String fieldname, String errorMessage) {
		this.errors.put(fieldname, errorMessage);
	}

}
