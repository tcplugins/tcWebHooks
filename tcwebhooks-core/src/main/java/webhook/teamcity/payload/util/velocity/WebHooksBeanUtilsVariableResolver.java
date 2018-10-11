package webhook.teamcity.payload.util.velocity;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
	Map<String, ExtraParametersMap> extraAndTeamCityProperties;
	
	public WebHooksBeanUtilsVariableResolver(Object javaBean, Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
		this.bean = javaBean;
		this.extraAndTeamCityProperties = extraAndTeamCityProperties;
	}
	
	@Override
	public String resolve(String variableName) {
		String value = "UNRESOLVED";
		
		try {
			// Try getting it from properties passed in first.
			for (String keyName : this.extraAndTeamCityProperties.keySet()){
				if (extraAndTeamCityProperties.get(keyName).containsKey(variableName)){
					value = extraAndTeamCityProperties.get(keyName).get(variableName);
				}
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
	public Object get(String variableName) {
		if (containsKey(variableName)){
			return resolve(variableName); 
		}
		return null;
	}

	@Override
	public Object[] getKeys() {
		// TODO Auto-generated method stub
		throw new EeekException("getKeys called");
		//return null;
	}

	@Override
	public Object put(String key, Object value) {
		// TODO Auto-generated method stub
		throw new EeekException("put called with key: " + key);
		//return null;
	}

	@Override
	public Object remove(Object arg0) {
		// TODO Auto-generated method stub
		throw new EeekException("remove called");
		//return null;
	}

	public static class EeekException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public EeekException(String message) {
			super(message);
		}
	}
	
	public static class DateWrapper {
		public String now(String datePattern){
			try {
				//String datePattern = variableName.substring("now(".length(), variableName.length() - ")".length());
				SimpleDateFormat format = new SimpleDateFormat(datePattern);
				return format.format(new Date());
			} catch (NullPointerException npe){
				// do nothing and let the logic below handle it.
			} catch (IllegalArgumentException iae){
				// do nothing and let the logic below handle it.
			}
			return "";
		}
	}
	
	
/*	public static class StringSanitiserWrapper {
		
		private ExtraParametersMap teamcityProperties;
		private Object bean;

		public StringSanitiserWrapper(Object javaBean, ExtraParametersMap teamcityProperties) {
			this.bean = javaBean;
			this.teamcityProperties = teamcityProperties;
		}
		
		public String sanitise(String dirtyString){
*/			
/*			if (teamcityProperties.containsKey(dirtyString)){
				return StringSanitiser.sanitise(teamcityProperties.get(dirtyString));
			} else {
				try {
					return StringSanitiser.sanitise((String) PropertyUtils.getProperty(bean, dirtyString).toString());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
*//*			return StringSanitiser.sanitise(dirtyString);
		}
	}*/
}
