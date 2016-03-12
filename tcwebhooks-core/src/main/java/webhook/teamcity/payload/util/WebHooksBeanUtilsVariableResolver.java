package webhook.teamcity.payload.util;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.context.Context;

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

public class WebHooksBeanUtilsVariableResolver implements VariableResolver, Context {
	
	
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
		
		if ((variableName.startsWith("sanitise(") || variableName.startsWith("sanitize(")) && variableName.endsWith(")")){
			try {
				String dirtyString = variableName.substring("sanitise(".length(), variableName.length() - ")".length());
				if (teamcityProperties.containsKey(dirtyString)){
					return StringSanitiser.sanitise(teamcityProperties.get(dirtyString));
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

	@Override
	public boolean containsKey(Object arg0) {
		if (arg0 instanceof String){
			String thing = resolve((String)arg0);
			return (thing != null && !thing.equals("UNRESOLVED"));
		}
		return false;
	}

	@Override
	public Object get(String arg0) {
		if (containsKey(arg0)){
			return resolve(arg0); 
		}
		return null;
	}

	@Override
	public Object[] getKeys() {
		// TODO Auto-generated method stub
		throw new EeekException();
		//return null;
	}

	@Override
	public Object put(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		throw new EeekException();
		//return null;
	}

	@Override
	public Object remove(Object arg0) {
		// TODO Auto-generated method stub
		throw new EeekException();
		//return null;
	}

	public static class EeekException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public EeekException() {
			super();
		}
	}
}
