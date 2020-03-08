package webhook.teamcity.payload.variableresolver.velocity;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.variableresolver.VariableResolver;

/**
 * This is a VariableResolver for the TemplateMatcher
 * 
 * It resolves the values of variables from javaBean objects using 
 * org.apache.commons.beanutils.PropertyUtils 
 * 
 * @author NetWolfUK
 *
 */

public class WebHooksBeanUtilsVelocityVariableResolver implements VariableResolver, Context {
	
	
	Object bean;
	Map<String, ExtraParametersMap> extraAndTeamCityProperties;
	VelocityContext velocityContext = new VelocityContext();
	
	@SuppressWarnings("unchecked")
	public WebHooksBeanUtilsVelocityVariableResolver(Object javaBean, Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
		this.bean = javaBean;
		this.extraAndTeamCityProperties = extraAndTeamCityProperties;
		
		for (String keyName : this.extraAndTeamCityProperties.keySet()){
			for (Map.Entry<String,String> entry : extraAndTeamCityProperties.get(keyName).entrySet()) {
				velocityContext.put(entry.getKey().replaceAll("\\.", "_"), entry.getValue());
			}
		}
		
		
		try {
			Map<String, Object> beanProperties = PropertyUtils.describe(bean);
			for (Entry<String, Object> property : beanProperties.entrySet()) {
				velocityContext.put(property.getKey(), property.getValue());
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown populating context from bean"); 
			Loggers.SERVER.debug(e);
		} 
		
		if (!velocityContext.containsKey("jsonTool")) {
			velocityContext.put("jsonTool", new VelocityJsonTool());
		} else {
			Loggers.SERVER.warn("WebHooksBeanUtilsVelocityVariableResolver :: Unable to add 'jsonTool' to Velocity context. An item of that name already exists");
		}
		
	}
	
	@Override
	public String resolve(String variableName) {
		return (String)get(variableName);
	}

	@Override
	public Object put(String key, Object value) {
		return this.velocityContext.put(key, value);
	}

	@Override
	public Object get(String key) {
		return this.velocityContext.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return this.velocityContext.containsKey(key);
	}

	@Override
	public Object[] getKeys() {
		return this.velocityContext.getKeys();
	}

	@Override
	public Object remove(Object key) {
		return this.velocityContext.remove(key);
	}

}