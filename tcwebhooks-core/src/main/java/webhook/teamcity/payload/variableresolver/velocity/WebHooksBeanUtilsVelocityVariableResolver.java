package webhook.teamcity.payload.variableresolver.velocity;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.variableresolver.VariableResolver;
import webhook.teamcity.settings.secure.WebHookSecretResolver;

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
	
	
	private static final String SECURE = "secure(";
	private static final String SUFFIX = ")";
	private Object bean;
	private VelocityContext velocityContext = new VelocityContext();
	private SProject sProject;
	private WebHookSecretResolver webHookSecretResolver;
	
	public WebHooksBeanUtilsVelocityVariableResolver(
			SProject sProject,
			WebHookContentObjectSerialiser webhookPayload, 
			Object javaBean, 
			ExtraParameters extraAndTeamCityProperties,
			WebHookSecretResolver webHookSecretResolver) {
		this.sProject = sProject;
		this.bean = javaBean;
		this.webHookSecretResolver = webHookSecretResolver;
		
		for (Map.Entry<String,String> entry : extraAndTeamCityProperties.asMap().entrySet()) {
			velocityContext.put(entry.getKey().replaceAll("\\.", "_"), entry.getValue());
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
		if (!velocityContext.containsKey("nullUtils")) {
			velocityContext.put("nullUtils", new VelocityNullUtils());
		} else {
			Loggers.SERVER.warn("WebHooksBeanUtilsVelocityVariableResolver :: Unable to add 'nullUtils' to Velocity context. An item of that name already exists");
		}
		
	}
	
	@Override
	public String resolve(String variableName) {
		if (variableName.startsWith(SECURE)  && variableName.endsWith(SUFFIX)&& StringUtils.isNotBlank(variableName.substring(SECURE.length()))) {
			String secret = this.webHookSecretResolver.getSecret(this.sProject, variableName.substring(SECURE.length()));
			if (secret != null) {
				return secret;
			}
		}
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
	public boolean containsKey(String key) {
		return this.velocityContext.containsKey(key);
	}

	@Override
	public String[] getKeys() {
		return this.velocityContext.getKeys();
	}

	@Override
	public Object remove(String key) {
		return this.velocityContext.remove(key);
	}

}