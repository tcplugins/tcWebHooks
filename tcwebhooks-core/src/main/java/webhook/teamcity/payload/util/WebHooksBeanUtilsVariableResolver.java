package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;

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
	ExtraParametersMap teamcityProperties;
	
	public WebHooksBeanUtilsVariableResolver(Object javaBean, ExtraParametersMap teamcityProperties) {
		this.bean = javaBean;
		this.teamcityProperties = teamcityProperties;
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

		
		try {
			// Try getting it from teamcity first.
			if (teamcityProperties != null && teamcityProperties.containsKey(variableName)){
				value = (String) teamcityProperties.get(variableName);
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
