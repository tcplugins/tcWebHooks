package webhook.teamcity.payload.util;


import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;

public class BeanUtilsTest {

    private ExtraParametersMap extraParameters;
    private ExtraParametersMap teamcityProperties;

    @Test
    public void testBeanUtils() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        extraParameters = new ExtraParametersMap(new TreeMap<String, String>());
        teamcityProperties = new ExtraParametersMap(new TreeMap<String, String>());
        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties);
        WebHookPayloadContent content = framework.getWebHookContent();
        String buildFullName = (String) PropertyUtils.getProperty(content, "buildFullName");
        assertTrue(buildFullName.equals("Test Project :: Test Build"));

    }

}
