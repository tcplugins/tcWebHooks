package webhook.teamcity.server.rest.model.template;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data @XmlRootElement
public class TemplateValidationResult implements Serializable {
	
	private static final long serialVersionUID = 6224688951409513197L;
	private boolean isErrored = false;
	private Map<String, String> errors = new LinkedHashMap<>();
	
	public void addError(String fieldname, String errorMessage) {
		this.errors.put(fieldname, errorMessage);
	}

}
