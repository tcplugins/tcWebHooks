package webhook.teamcity.payload.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This is a VariableResolver for the TemplateMatcher
 * <p>
 * It resolves the values of variables from javaBean objects using
 * org.apache.commons.beanutils.PropertyUtils
 *
 * @author NetWolfUK
 */

public class WebHooksBeanUtilsVariableResolver implements VariableResolver {


    Object bean;
    Map<String, ExtraParametersMap> extraAndTeamCityProperties;

    public WebHooksBeanUtilsVariableResolver(Object javaBean, Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
        this.bean = javaBean;
        this.extraAndTeamCityProperties = extraAndTeamCityProperties;
    }

    @Override
    public String resolve(String variableName) {
        String value = "UNRESOLVED";

        // if variable is a date formating variable. eg. now() or now(dateformatAsString)
        if (variableName.startsWith("now(") && variableName.endsWith(")")) {
            try {
                String datePattern = variableName.substring("now(".length(), variableName.length() - ")".length());
                SimpleDateFormat format = new SimpleDateFormat(datePattern);
                return format.format(new Date());
            } catch (NullPointerException npe) {
                // do nothing and let the logic below handle it.
            } catch (IllegalArgumentException iae) {
                // do nothing and let the logic below handle it.
            }
        }

        if (variableName.startsWith("escapejson(") && variableName.endsWith(")")) {
            try {
                String dirtyString = variableName.substring("escapejson(".length(), variableName.length() - ")".length());
                for (String keyName : this.extraAndTeamCityProperties.keySet()) {
                    if (extraAndTeamCityProperties.get(keyName).containsKey(dirtyString)) {
                        return StringEscapeUtils.escapeJson(extraAndTeamCityProperties.get(keyName).get(dirtyString));
                    }
                }
                return StringEscapeUtils.escapeJson((String) PropertyUtils.getProperty(bean, dirtyString).toString());
                // do nothing and let the logic below handle it.
            } catch (NullPointerException npe) {
            } catch (IllegalArgumentException iae) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }

        if ((variableName.startsWith("sanitise(") || variableName.startsWith("sanitize(")) && variableName.endsWith(")")) {
            try {
                String dirtyString = variableName.substring("sanitise(".length(), variableName.length() - ")".length());
                for (String keyName : this.extraAndTeamCityProperties.keySet()) {
                    if (extraAndTeamCityProperties.get(keyName).containsKey(dirtyString)) {
                        return StringSanitiser.sanitise(extraAndTeamCityProperties.get(keyName).get(dirtyString));
                    }
                }
                return StringSanitiser.sanitise((String) PropertyUtils.getProperty(bean, dirtyString).toString());

                // do nothing and let the logic below handle it.
            } catch (NullPointerException npe) {
            } catch (IllegalArgumentException iae) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }

        try {
            // Try getting it from properties passed in first.
            for (String keyName : this.extraAndTeamCityProperties.keySet()) {
                if (extraAndTeamCityProperties.get(keyName).containsKey(variableName)) {
                    value = extraAndTeamCityProperties.get(keyName).get(variableName);
                }
            }

            // Or override it from the PayloadContent if it exists.
            try {
                value = (String) PropertyUtils.getProperty(bean, variableName).toString();
            } catch (NullPointerException npe) {
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
