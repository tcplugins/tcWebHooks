package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.parameters.ProcessingResult;
import jetbrains.buildServer.parameters.ValueResolver;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterModel;

public class ExtraParameters extends ArrayList<WebHookParameterModel> {

	public static final String TEAMCITY = "teamcity";
	public static final String WEBHOOK = "webhook";
	public static final String PROJECT = "project";
	protected static final boolean INCLUDED_IN_LEGACY_PAYLOADS = true;
	protected static final boolean FORCE_RESOLVE_TEAMCITY_VARIABLE = false;
	protected static final String TEMPLATE_ENGINE_TYPE = PayloadTemplateEngineType.STANDARD.toString();
	private static final long serialVersionUID = -2947332186712049416L;
	
	private boolean secureValueAccessed = false;

	public ExtraParameters() {
		super();
	}
	public ExtraParameters(List<WebHookParameterModel> items){
		super(items);
	}
		
	public ExtraParameters(Map<String, String> extraParameters) {
		addAll("none", extraParameters, INCLUDED_IN_LEGACY_PAYLOADS, FORCE_RESOLVE_TEAMCITY_VARIABLE);
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
		return addAll(key, paramsMap, INCLUDED_IN_LEGACY_PAYLOADS, FORCE_RESOLVE_TEAMCITY_VARIABLE);
	}
	public ExtraParameters addAll(String context, Map<String,String> paramsMap, boolean includeInLegacyPayload, boolean forceResolveTeamCityVariable) {
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			add(new WebHookParameterModel(
					context + "-" + entry.getKey(),
					context,
					entry.getKey(),
					entry.getValue(),
					false,
					includeInLegacyPayload,
					forceResolveTeamCityVariable,
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
			Loggers.SERVER.debug("WebHookExtraParameters :: Removed existing WebHookParameter: " + previous.getContext() + " : " + previous.getName());
		}
		WebHookParameterModel newHookParameterModel = new WebHookParameterModel(context,
				context,
				key,
				value,
				false,
				INCLUDED_IN_LEGACY_PAYLOADS,
				FORCE_RESOLVE_TEAMCITY_VARIABLE,
				TEMPLATE_ENGINE_TYPE);
		add(newHookParameterModel);
		Loggers.SERVER.debug("WebHookExtraParameters :: Added WebHookParameter: " + newHookParameterModel.getContext() + " : " + newHookParameterModel.getName());
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
				Loggers.SERVER.debug("WebHookExtraParameters :: Removed existing WebHookParameter: " + previous.getContext() + " : " + previous.getName());
			}
			WebHookParameterModel newHookParameterModel = new WebHookParameterModel(context,
					context,
					parameter.getName(),
					parameter.getValue(),
					parameter.getSecure(),
					parameter.getIncludedInLegacyPayloads(),
					parameter.getForceResolveTeamCityVariable(),
					parameter.getTemplateEngine());
			add(newHookParameterModel);
			Loggers.SERVER.debug("WebHookExtraParameters :: Added WebHookParameter: " + previous.getContext() + " : " + previous.getName());
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
					if (Boolean.TRUE.equals(param.getSecure())) {
						this.secureValueAccessed = true;
						Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Secure value accessed for '%s' : '%s'", "no_context", key));
					}
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
				if (Boolean.TRUE.equals(webHookParameter.getSecure())) {
					this.secureValueAccessed = true;
					Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Secure value accessed for '%s' : '%s'", context, key));
				}
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
	public void forceResolveVariables(ValueResolver valueResolver) {
		Map<String, String> variablesToResolve = getForceResolvableVariables();
		if (!variablesToResolve.isEmpty()) {
			Map<String, ProcessingResult> resolvedMap = valueResolver.resolveWithDetails(variablesToResolve);
			for ( Entry<String, ProcessingResult> e: resolvedMap.entrySet()) {
				Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Processing resolver result for '%s'", e.getKey()));
				if (e.getValue().isModified()) {
					Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Value was modified for '%s'. New value is '%s'", e.getKey(), e.getValue().getResult()));
					
					WebHookParameterModel param = getActual(PROJECT, e.getKey()); // We only support forced update on PROJECT Parameters
					if (param != null && Boolean.TRUE.equals(param.getForceResolveTeamCityVariable())) {
						Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Found parameter with match value. Name is '%s'. New value is '%s'", param.getName(), e.getValue().getResult()));
						param.setValue(e.getValue().getResult());
						Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Found parameter: '%s'", param.toString()));
					}
				} else {
					Loggers.SERVER.debug(String.format("WebHookExtraParameters :: Value was not modified for '%s'.", e.getKey()));
				}
			}
		} else {
			Loggers.SERVER.debug("WebHookExtraParameters :: No force resolver parameters found. Resolving will be skipped.");
		}
		
	}
	protected Map<String, String> getForceResolvableVariables() {
		Map<String, String> variablesToResolve = new TreeMap<>();
		for ( WebHookParameterModel param : getProjectParameters()) { // Only PROJECT parameters are foreResolvable.
			if (Boolean.TRUE.equals(param.getForceResolveTeamCityVariable())) {
				Loggers.SERVER.debug(String.format("WebHookExtraParameters:: Parameter '%s' is marked as forcedResolvable. %s", param.getName(), param.toString()));
				variablesToResolve.put(param.getName(), param.getValue());
			}
		}
		return variablesToResolve;
	}
	
	public boolean wasSecureValueAccessed() {
		return this.secureValueAccessed;
	}
	
	public List<WebHookParameter> getAll() {
		List<WebHookParameter> all = new ArrayList<>();
		for (WebHookParameterModel model : this) {
			all.add(model);
		}
		return all;
	}
}
