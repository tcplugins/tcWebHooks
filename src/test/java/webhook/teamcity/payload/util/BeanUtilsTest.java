package webhook.teamcity.payload.util;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class BeanUtilsTest {
	
	private ExtraParametersMap extraParameters;

	@Test
	public void testBeanUtils() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		extraParameters = new ExtraParametersMap(new TreeMap<String, String>());
		WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters);
		WebHookPayloadContent content = framework.getWebHookContent();
		String buildFullName = (String) PropertyUtils.getProperty(content, "buildFullName");
		assertTrue(buildFullName.equals("Test Project :: Test Build"));
		
	}

}
