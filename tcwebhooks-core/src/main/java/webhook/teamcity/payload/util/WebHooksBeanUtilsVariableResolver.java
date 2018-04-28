package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * This is a VariableResolver for the TemplateMatcher
 * 
 * It resolves the values of variables from javaBean objects using 
 * org.apache.commons.beanutils.PropertyUtils 
 * 
 * @author NetWolfUK
 *
 */

public class WebHooksBeanUtilsVariableResolver implements VariableResolver {
	
	
	private static final String CAPITALISE = "capitalise(";
	private static final String CAPITALIZE = "capitalize(";
	private static final String ESCAPEJSON = "escapejson(";
	private static final String NOW = "now(";
	private static final String SANITISE = "sanitise(";
	private static final String SANITIZE = "sanitize(";
	private static final String SUBSTR = "substr(";
	private static final String SUFFIX = ")";
	private final Object bean;
	private final Map<String, ExtraParametersMap> extraAndTeamCityProperties;
	private final WebHookContentObjectSerialiser webhookPayload;
	
	public WebHooksBeanUtilsVariableResolver(WebHookContentObjectSerialiser webhookPayload, Object javaBean, Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
		this.webhookPayload = webhookPayload;
		this.bean = javaBean;
		this.extraAndTeamCityProperties = extraAndTeamCityProperties;
	}
	
	@Override
	public String resolve(String variableName) {
		String value = "UNRESOLVED";
		
		// if variable is a date formating variable. eg. now() or now(dateformatAsString)
		if (variableName.startsWith(NOW) && variableName.endsWith(SUFFIX)){
			try {
				String datePattern = variableName.substring(NOW.length(), variableName.length() - SUFFIX.length());
				SimpleDateFormat format = new SimpleDateFormat(datePattern);
				return format.format(new Date());
			} catch (NullPointerException | IllegalArgumentException e){
				// do nothing and let the logic below handle it.
			}
		}
		
		if (variableName.startsWith(ESCAPEJSON) && variableName.endsWith(SUFFIX)){
			try {
				String dirtyString = variableName.substring(ESCAPEJSON.length(), variableName.length() - SUFFIX.length());
				for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
					if (entry.getValue().containsKey(dirtyString)){
						return StringEscapeUtils.escapeJson(entry.getValue().get(dirtyString));
					}
				}
				return StringEscapeUtils.escapeJson((String) getProperty(bean, dirtyString).toString());
			} catch (NullPointerException | IllegalArgumentException | 
					 IllegalAccessException | InvocationTargetException | NoSuchMethodException e) 
			{
				// do nothing and let the logic below handle it.
			}			
		}
		
		if ((variableName.startsWith(CAPITALISE)|| variableName.startsWith(CAPITALIZE)) && variableName.endsWith(SUFFIX)){
			try {
				String dirtyString = variableName.substring(CAPITALISE.length(), variableName.length() - SUFFIX.length());
				for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
					if (entry.getValue().containsKey(dirtyString)){
						return StringUtils.capitaliseAllWords(entry.getValue().get(dirtyString));
					}
				}
				return StringUtils.capitaliseAllWords((String) getProperty(bean, dirtyString).toString());
			} catch (NullPointerException | IllegalArgumentException | 
					 IllegalAccessException | InvocationTargetException | NoSuchMethodException e) 
			{
				// do nothing and let the logic below handle it.
			}			
		}
		
		if (variableName.startsWith(SUBSTR) && variableName.endsWith(SUFFIX)){
			try {
				String[] subStringOptions = variableName.substring(SUBSTR.length(), variableName.length() - SUFFIX.length()).split(",");
				String varName = subStringOptions[0];
				for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
					if (entry.getValue().containsKey(varName)){
						return StringUtils.subString(
									entry.getValue().get(varName),
									Integer.valueOf(subStringOptions[1]),
									Integer.valueOf(subStringOptions[2]),
									Integer.valueOf(subStringOptions[3])
								);
					}
				}
				return StringUtils.subString(
								getProperty(bean, varName).toString(), 
								Integer.valueOf(subStringOptions[1]), 
								Integer.valueOf(subStringOptions[2]), 
								Integer.valueOf(subStringOptions[3])
							);
			} catch (NullPointerException | IllegalArgumentException | 
					IllegalAccessException | InvocationTargetException | NoSuchMethodException e) 
			{
				// do nothing and let the logic below handle it.
			}			
		}
		
		if ((variableName.startsWith("capitaliseFirst(")|| variableName.startsWith("capitalizeFirst(")) && variableName.endsWith(SUFFIX)){
			try {
				String dirtyString = variableName.substring(CAPITALISE.length(), variableName.length() - SUFFIX.length());
				for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
					if (entry.getValue().containsKey(dirtyString)){
						return StringUtils.capitaliseFirstWord(entry.getValue().get(dirtyString));
					}
				}
				return StringUtils.capitaliseFirstWord((String) PropertyUtils.getProperty(bean, dirtyString).toString());
			} catch (NullPointerException | IllegalArgumentException | 
					IllegalAccessException | InvocationTargetException | NoSuchMethodException e) 
			{
				// do nothing and let the logic below handle it.
			}			
		}
		
		if ((variableName.startsWith(SANITISE) || variableName.startsWith(SANITIZE)) && variableName.endsWith(SUFFIX)){
			try {
				String dirtyString = variableName.substring(SANITISE.length(), variableName.length() - SUFFIX.length());
				for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
					if (entry.getValue().containsKey(dirtyString)){
						return StringSanitiser.sanitise(entry.getValue().get(dirtyString));
					}
				}
				return StringSanitiser.sanitise((String) getProperty(bean, dirtyString).toString());

			} catch (NullPointerException | IllegalArgumentException | 
					 IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				// do nothing and let the logic below handle it.
			}
		}
		
		try {
			// Try getting it from properties passed in first.
			for (Entry<String, ExtraParametersMap> entry : this.extraAndTeamCityProperties.entrySet()){
				if (entry.getValue().containsKey(variableName)){
					value = entry.getValue().get(variableName);
				}
			}			
			
			// Or override it from the PayloadContent if it exists.
			try {
				value = getProperty(bean, variableName).toString();
			} catch (NullPointerException npe){
				value = (String) getProperty(bean, variableName);
			}
			
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
		}
		return value;
		
	}
	
	private String getProperty(Object bean, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (PropertyUtils.getProperty(bean, propertyName) == null) {
			return null;
		}
		return this.webhookPayload.serialiseObject(PropertyUtils.getProperty(bean, propertyName)).toString();
	}

}
