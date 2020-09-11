package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterModel;

public class ExtraParameters extends ArrayList<WebHookParameterModel> {

	public static final String TEAMCITY = "teamcity";
	public static final String WEBHOOK = "webhook";
	public static final String PROJECT = "project";
	private static final boolean INCLUDED_IN_LEGACY_PAYLOADS = true;
	private static final String TEMPLATE_ENGINE_TYPE = PayloadTemplateEngineType.STANDARD.toString();
	private static final long serialVersionUID = -2947332186712049416L;

	public ExtraParameters() {
		super();
	}
	public ExtraParameters(List<WebHookParameterModel> items){
		super(items);
	}
		
	public ExtraParameters(Map<String, String> extraParameters) {
		addAll("none", extraParameters, true);
	}

	public Set<Entry<String, String>> entrySet() {
		return getEntriesAsSet();
	}
	
	public Set<Entry<String, String>> getEntriesAsSet(){
		Map<String, String> map = new TreeMap<>();
		for (WebHookParameter param : this) {
			if (Boolean.TRUE.equals(param.getIncludedInLegacyPayloads())) {
				map.put(param.getName(), param.getValue());
			}
		}
		return map.entrySet();
	}
	
	public ExtraParameters addAll(String key, Map<String,String> paramsMap) {
		return addAll(key, paramsMap, INCLUDED_IN_LEGACY_PAYLOADS);
	}
	public ExtraParameters addAll(String context, Map<String,String> paramsMap, boolean includeInLegacyPayload) {
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			add(new WebHookParameterModel(
					context + "-" + entry.getKey(),
					context,
					entry.getKey(),
					entry.getValue(),
					false,
					includeInLegacyPayload,
					TEMPLATE_ENGINE_TYPE
					)
				);
		}
		return this;
	}

	public void put(String key, String value) {
		put("none", key, value);
		
	}
	
	public void put(String context, String key, String value) {
		WebHookParameterModel previous = getActual(context, key);
		if (previous != null) {
			this.remove(previous);
			Loggers.SERVER.debug("ExtraParameters :: Removed existing WebHookParameter: " + previous.getContext() + " : " + previous.getName() + " : " + previous.getValue());
		}
		WebHookParameterModel newHookParameterModel = new WebHookParameterModel(context,
				context,
				key,
				value,
				false,
				INCLUDED_IN_LEGACY_PAYLOADS,
				TEMPLATE_ENGINE_TYPE);
		add(newHookParameterModel);
		Loggers.SERVER.debug("ExtraParameters :: Added WebHookParameter: " + newHookParameterModel.getContext() + " : " + newHookParameterModel.getName() + " : " + newHookParameterModel.getValue());
	}

	public void putAll(String context, Map<String, String> paramMap) {
		for (Map.Entry<String, String> param : paramMap.entrySet()) {
			put(context, param.getKey(), param.getValue());
		}
	}
	
	public void putAll(String context, List<WebHookParameter> webHookParameters) {
		for (WebHookParameter parameter : webHookParameters) {
			WebHookParameterModel previous = getActual(context, parameter.getName());
			if (previous != null) {
				this.remove(previous);
				Loggers.SERVER.debug("ExtraParameters :: Removed existing WebHookParameter: " + previous.getContext() + " : " + previous.getName() + " : " + previous.getValue());
			}
			WebHookParameterModel newHookParameterModel = new WebHookParameterModel(context,
					context,
					parameter.getName(),
					parameter.getValue(),
					parameter.getSecure(),
					parameter.getIncludedInLegacyPayloads(),
					parameter.getTemplateEngine());
			add(newHookParameterModel);
			Loggers.SERVER.debug("ExtraParameters :: Added WebHookParameter: " + newHookParameterModel.getContext() + " : " + newHookParameterModel.getName() + " : " + newHookParameterModel.getValue());
		}
	}
	
	private WebHookParameterModel getActual(String context, String key) {
		for (WebHookParameterModel webHookParameter : this) {
			if (webHookParameter.getContext().equalsIgnoreCase(context) 
					&& webHookParameter.getName().equals(key)) {
				return webHookParameter;
			}
		}
		return null;
	}
	public boolean containsKey(String key) {
		for (WebHookParameter param : this) {
			if (key.equals(param.getName())) {
				return true;
			}
		}
		return false;
	}

	public String get(String key) {
		if (getWebHookParameters().containsKey(key)) {
			return getParameter(WEBHOOK, key);
		} else if (getProjectParameters().containsKey(key)) {
			return getParameter(PROJECT, key);
		} else if (getTeamcityParameters().containsKey(key)) {
			return getParameter(TEAMCITY, key);
		} else {
			for (WebHookParameter param : this) {
				if (key.equals(param.getName())) {
					return param.getValue();
				}
			}
			return null;
		}
	}
	
	public ExtraParameters getTeamcityParameters() {
		return getProperties(TEAMCITY);
	}
	
	public ExtraParameters getWebHookParameters() {
		return getProperties(WEBHOOK);
	}
	
	public ExtraParameters getProjectParameters() {
		return getProperties(PROJECT);
	}
	
	public String getParameter(String context, String key) {
		for (WebHookParameterModel webHookParameter : this) {
			if (webHookParameter.getContext().equalsIgnoreCase(context) 
					&& webHookParameter.getName().equals(key)) {
				return webHookParameter.getValue();
			}
		}
		return null;
	}
	public ExtraParameters getProperties(String context) {
		ExtraParameters parameters = new ExtraParameters();
		for (WebHookParameterModel webHookParameter : this) {
			if (webHookParameter.getContext().equalsIgnoreCase(context)) {
				parameters.add(webHookParameter);
			}
		}
		return parameters;
	}
	
	public SortedMap<String,String> asMap() {
		SortedMap<String,String> map = new TreeMap<>();
		for (WebHookParameterModel webHookParameter : this) {
			map.put(webHookParameter.getName(), webHookParameter.getValue());
		}
		return map;
	}
	
	public void resolveParameters(Map<String,VariableMessageBuilder> variableMessageBuilders) {
		for (WebHookParameter param : this) {
			if (param.getTemplateEngine() == null) {
				param.setTemplateEngine(TEMPLATE_ENGINE_TYPE);
			}
			if (param.getName() != null && variableMessageBuilders.containsKey(param.getTemplateEngine())) {
				String newName = variableMessageBuilders.get(param.getTemplateEngine()).build(param.getName());
				if (newName != null && !newName.equals(param.getName())) {
					param.setName(newName);
				}
			}
			if (param.getValue() != null && variableMessageBuilders.containsKey(param.getTemplateEngine())) {
				String newValue = variableMessageBuilders.get(param.getTemplateEngine()).build(param.getValue());
				if (newValue != null && !newValue.equals(param.getValue())) {
					param.setValue(newValue);
				}
			}
		}
	}
}
