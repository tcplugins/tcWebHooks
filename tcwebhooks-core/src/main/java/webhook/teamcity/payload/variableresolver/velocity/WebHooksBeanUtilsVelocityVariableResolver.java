package webhook.teamcity.payload.variableresolver.velocity;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.generic.DateTool;

import jetbrains.buildServer.serverSide.SProject;
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
	private static final Logger LOG = Logger.getInstance(WebHooksBeanUtilsVelocityVariableResolver.class.getName());

	
	private static final String SECURE = "secure(";
	private static final String SUFFIX = ")";
	private Object bean;
	private VelocityContext velocityContext = new VelocityContext();
	private Map<String,String> nameMappings = new HashMap<>();
	private SProject sProject;
	private WebHookSecretResolver webHookSecretResolver;
	private ExtraParameters extraParameters;
	
	public WebHooksBeanUtilsVelocityVariableResolver(
			SProject sProject,
			WebHookContentObjectSerialiser webhookPayload, 
			Object javaBean, 
			ExtraParameters extraAndTeamCityProperties,
			WebHookSecretResolver webHookSecretResolver) {
		this.sProject = sProject;
		this.bean = javaBean;
		this.webHookSecretResolver = webHookSecretResolver;
		this.extraParameters = extraAndTeamCityProperties;
		
		for (Map.Entry<String,String> entry : extraAndTeamCityProperties.asMap().entrySet()) {
			String newKey = entry.getKey().replace(".", "_");
			velocityContext.put(newKey, entry.getValue());
			nameMappings.put(newKey, entry.getKey());
		}
		
		try {
			Map<String, Object> beanProperties = PropertyUtils.describe(bean);
			for (Entry<String, Object> property : beanProperties.entrySet()) {
				velocityContext.put(property.getKey(), property.getValue());
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOG.debug(this.getClass().getSimpleName() + " :: " + e.getClass() + " thrown populating context from bean"); 
			LOG.debug(e);
		} 
		
		if (!velocityContext.containsKey("dateTool")) {
			velocityContext.put("dateTool", new DateTool());
		} else {
			LOG.warn("WebHooksBeanUtilsVelocityVariableResolver :: Unable to add 'dateTool' to Velocity context. An item of that name already exists");
		}
		if (!velocityContext.containsKey("jsonTool")) {
		    velocityContext.put("jsonTool", new VelocityJsonTool());
		} else {
		    LOG.warn("WebHooksBeanUtilsVelocityVariableResolver :: Unable to add 'jsonTool' to Velocity context. An item of that name already exists");
		}
		if (!velocityContext.containsKey("nullUtils")) {
			velocityContext.put("nullUtils", new VelocityNullUtils());
		} else {
			LOG.warn("WebHooksBeanUtilsVelocityVariableResolver :: Unable to add 'nullUtils' to Velocity context. An item of that name already exists");
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
		LOG.debug(String.format("WebHooksBeanUtilsVelocityVariableResolver :: Value requested from Velocity context. 'key=%s'", key));
		if (nameMappings.containsKey(key)) {
			// Call extraParameters.get(), so that accessing secure parameters is logged.
			// Resolve any underscore names to dot names via the nameMappings.
			extraParameters.get(nameMappings.get(key));  
		}
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