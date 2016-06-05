package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;

import webhook.teamcity.Loggers;
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
	
	
	Object bean;
	ExtraParametersMap extraAndTeamCityProperties;
	
	public WebHooksBeanUtilsVariableResolver(Object javaBean, ExtraParametersMap extraAndTeamCityProperties) {
		this.bean = javaBean;
		this.extraAndTeamCityProperties = extraAndTeamCityProperties;
	}
	
	@Override
	public String resolve(String variableName) {
		String value = "UNRESOLVED";
		
		// if variable is a date formating variable. eg. now() or now(dateformatAsString)
		if (variableName.startsWith("now(") && variableName.endsWith(")")){
			try {
				String datePattern = variableName.substring("now(".length(), variableName.length() - ")".length());
				SimpleDateFormat format = new SimpleDateFormat(datePattern);
				return format.format(new Date());
			} catch (NullPointerException npe){
				// do nothing and let the logic below handle it.
			} catch (IllegalArgumentException iae){
				// do nothing and let the logic below handle it.
			}
		}
		
		if (variableName.startsWith("escapejson(") && variableName.endsWith(")")){
			try {
				String dirtyString = variableName.substring("escapejson(".length(), variableName.length() - ")".length());
				if (extraAndTeamCityProperties.containsKey(dirtyString)){
					return StringEscapeUtils.escapeJson(extraAndTeamCityProperties.get(dirtyString));
				} else {
					return StringEscapeUtils.escapeJson((String) PropertyUtils.getProperty(bean, dirtyString).toString());
				}

			// do nothing and let the logic below handle it.
			} catch (NullPointerException npe){
			} catch (IllegalArgumentException iae){
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (NoSuchMethodException e) {
			}			
		}
		
		if ((variableName.startsWith("sanitise(") || variableName.startsWith("sanitize(")) && variableName.endsWith(")")){
			try {
				String dirtyString = variableName.substring("sanitise(".length(), variableName.length() - ")".length());
				if (extraAndTeamCityProperties.containsKey(dirtyString)){
					return StringSanitiser.sanitise(extraAndTeamCityProperties.get(dirtyString));
				} else {
					return StringSanitiser.sanitise((String) PropertyUtils.getProperty(bean, dirtyString).toString());
				}

			// do nothing and let the logic below handle it.
			} catch (NullPointerException npe){
			} catch (IllegalArgumentException iae){
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (NoSuchMethodException e) {
			}
		}
		
		try {
			// Try getting it from properties passed in first.
			if (extraAndTeamCityProperties != null && extraAndTeamCityProperties.containsKey(variableName)){
				value = (String) extraAndTeamCityProperties.get(variableName);
			}
			
			// Or override it from the PayloadContent if it exists.
			try {
				value = (String) PropertyUtils.getProperty(bean, variableName).toString();
			} catch (NullPointerException npe){
				value = (String) PropertyUtils.getProperty(bean, variableName);
			}
			
		} catch (IllegalAccessException e) {
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		} catch (InvocationTargetException e) {
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		} catch (NoSuchMethodException e) {
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		}
		return value;
		
	}

}
