package webhook.teamcity.payload.util;


import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.TreeMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class BeanUtilsTest {
	
	private ExtraParameters extraParameters;

	@Test
	public void testBeanUtils() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		extraParameters = new ExtraParameters(new TreeMap<String, String>());
		WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters);
		WebHookPayloadContent content = framework.getWebHookContent();
		String buildFullName = (String) PropertyUtils.getProperty(content, "buildFullName");
		assertTrue(buildFullName.equals("Test Project :: Test Build"));
		
	}

}
