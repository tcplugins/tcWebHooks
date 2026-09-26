package webhook.teamcity.server.rest.model.template;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data @XmlRootElement
@ApiModel(description = "Validation error payload containing field-level problems.")
public class ErrorResult implements Serializable {
	
	private static final long serialVersionUID = -8395102761842280396L;
	@ApiModelProperty(value = "Field-level validation errors keyed by field name.", example = "{\"buildId\":\"must not be blank\"}")
	private Map<String, String> errors = new LinkedHashMap<>();
	
	public ErrorResult addError(String fieldname, String errorMessage) {
		this.errors.put(fieldname, errorMessage);
		return this;
	}
	
	public boolean isErrored() {
		return ! this.errors.isEmpty();
	}

}
