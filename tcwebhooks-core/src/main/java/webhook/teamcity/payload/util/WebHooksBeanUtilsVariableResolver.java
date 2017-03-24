package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import webhook.teamcity.Loggers;

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
	
	public WebHooksBeanUtilsVariableResolver(Object javaBean) {
		this.bean = javaBean;
	}
	
	@Override
	public String resolve(String variableName) {
		String value = "UNRESOLVED";
		try {
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
