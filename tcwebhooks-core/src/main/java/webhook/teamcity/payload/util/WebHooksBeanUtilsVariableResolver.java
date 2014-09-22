package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;

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
		try {
			// Try getting it from teamcity first.
			if (teamcityProperties.containsKey(variableName)){
				value = (String) teamcityProperties.get(variableName);
			}
			
			// Or override it from the PayloadContent if it exists.
			
			value = (String) PropertyUtils.getProperty(bean, variableName);
			
		} catch (IllegalAccessException e) {
			Loggers.SERVER.warn(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		} catch (InvocationTargetException e) {
			Loggers.SERVER.warn(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		} catch (NoSuchMethodException e) {
			Loggers.SERVER.warn(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown when trying to resolve value for " + variableName); 
			Loggers.SERVER.debug(e);
		}
		return value;
		
	}

}
