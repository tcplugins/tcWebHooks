package webhook.teamcity.payload.variableresolver.standard;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.util.StringSanitiser;
import webhook.teamcity.payload.util.StringUtils;
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

public class WebHooksBeanUtilsVariableResolver implements VariableResolver {

	private static final List<String> HIDDEN_FIELDS = Arrays.asList("build", "project", "buildType");
	private static final String CAPITALISE = "capitalise(";
	private static final String CAPITALIZE = "capitalize(";
	private static final String ESCAPEJSON = "escapejson(";
	private static final String NOW = "now(";
	private static final String SANITISE = "sanitise(";
	private static final String SANITIZE = "sanitize(";
	private static final String SECURE = "secure(";
	private static final String SUBSTR = "substr(";
	private static final String SUFFIX = ")";
	private final SProject sProject;
	private final Object bean;
	private final ExtraParameters extraParameters;
	private final WebHookContentObjectSerialiser webhookPayload;
	private final WebHookSecretResolver webHookSecretResolver;

	public WebHooksBeanUtilsVariableResolver(
			SProject sProject,
			WebHookContentObjectSerialiser webhookPayload, 
			Object javaBean, 
			ExtraParameters extraAndTeamCityProperties,
			WebHookSecretResolver webHookSecretResolver) {
		this.sProject = sProject;
		this.webhookPayload = webhookPayload;
		this.bean = javaBean;
		this.extraParameters = extraAndTeamCityProperties;
		this.webHookSecretResolver = webHookSecretResolver;
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
				if (extraParameters.containsKey(dirtyString)){
					return StringEscapeUtils.escapeJson(extraParameters.get(dirtyString));
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
				if (extraParameters.containsKey(dirtyString)){
					return StringUtils.capitaliseAllWords(extraParameters.get(dirtyString));
				}
				return StringUtils.capitaliseAllWords((String) getProperty(bean, dirtyString));
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
				if (extraParameters.containsKey(varName)){
					return StringUtils.subString(
							this.extraParameters.get(varName),
								Integer.valueOf(subStringOptions[1]),
								Integer.valueOf(subStringOptions[2]),
								Integer.valueOf(subStringOptions[3])
							);
				}
				return StringUtils.subString(
								getProperty(bean, varName),
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
				if (extraParameters.containsKey(dirtyString)){
					return StringUtils.capitaliseFirstWord(extraParameters.get(dirtyString));
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
				if (extraParameters.containsKey(dirtyString)){
					return StringSanitiser.sanitise(extraParameters.get(dirtyString));
				}
				return StringSanitiser.sanitise((String) getProperty(bean, dirtyString));

			} catch (NullPointerException | IllegalArgumentException |
					 IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				// do nothing and let the logic below handle it.
			}
		}
		
		if (variableName.startsWith(SECURE) && variableName.endsWith(SUFFIX)){
			String tokenString = variableName.substring(SECURE.length(), variableName.length() - SUFFIX.length());
			String resolvedString = this.webHookSecretResolver.getSecret(sProject, tokenString);
			if (resolvedString != null){
				return resolvedString;
			}
		}

		try {
			// Try getting it from properties passed in first.
			if (extraParameters.containsKey(variableName)){
				value = extraParameters.get(variableName);
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
		if (PropertyUtils.getProperty(bean, propertyName) == null || HIDDEN_FIELDS.contains(propertyName)) {
			return null;
		}
		return this.webhookPayload.serialiseObject(PropertyUtils.getProperty(bean, propertyName)).toString();
	}

}
