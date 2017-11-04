package webhook.teamcity.server.rest.model.template;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data @XmlRootElement
public class ErrorResult implements Serializable {
	
	private static final long serialVersionUID = -8395102761842280396L;
	private Map<String, String> errors = new LinkedHashMap<>();
	
	public void addError(String fieldname, String errorMessage) {
		this.errors.put(fieldname, errorMessage);
	}
	
	public boolean isErrored() {
		return ! this.errors.isEmpty();
	}

}
