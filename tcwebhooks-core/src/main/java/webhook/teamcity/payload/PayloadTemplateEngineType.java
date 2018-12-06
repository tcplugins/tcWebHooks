package webhook.teamcity.payload;

import lombok.Getter;

@Getter
public enum PayloadTemplateEngineType {
	
	LEGACY	 ("${", "}", false),
	STANDARD ("${", "}", true),
	VELOCITY ("#", ")" , true);
	
	String variablePrefix;
	String variableSuffix;
	boolean isTemplated;
	
	PayloadTemplateEngineType(String prefix, String suffix, boolean isTemplated) {
		this.variablePrefix = prefix;
		this.variableSuffix = suffix;
		this.isTemplated = isTemplated;
	}

}
