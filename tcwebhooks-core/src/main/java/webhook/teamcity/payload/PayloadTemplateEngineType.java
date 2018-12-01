package webhook.teamcity.payload;

import lombok.Getter;

@Getter
public enum PayloadTemplateEngineType {
	
	STANDARD ("${", "}"),
	VELOCITY ("#", ")");
	
	String variablePrefix;
	String variableSuffix;
	
	PayloadTemplateEngineType(String prefix, String suffix) {
		this.variablePrefix = prefix;
		this.variableSuffix = suffix;
	}

}
