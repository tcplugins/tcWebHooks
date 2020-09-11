package webhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContent.SimpleSerialiser;
import webhook.teamcity.payload.util.VariableMessageBuilderTestBase;
import webhook.teamcity.payload.variableresolver.VariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolver;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.secure.WebHookSecretResolver;
import webhook.teamcity.settings.secure.WebHookSecretResolverNoOpImpl;

public class WebHookImplFiltersTest extends VariableMessageBuilderTestBase {
	
	TestingWebHookFactory factory = new TestingWebHookFactory();
	VariableResolverFactory variableResolverFactory = new WebHooksBeanUtilsVariableResolverFactory();
	WebHookSecretResolver webHookSecretResolver = new WebHookSecretResolverNoOpImpl();

	@Test
	public void testCheckSingleFilterPasses() {
		
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("teamcity", "^.+eam.+$", true));
		assertTrue(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("", webHook.getDisabledReason());
		assertTrue(webHook.isEnabled());
	}
	
	@Test
	public void testCheckSingleFilterFails() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("something", "^.+eam.+$", true));
		assertFalse(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals( "Filter mismatch: something (something) does not match using regex ^.+eam.+$", webHook.getDisabledReason());
		assertFalse(webHook.isEnabled());
	}

	@Test
	public void testCheckSingleFilterPassesFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^12.+$", true));
		assertTrue(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("", webHook.getDisabledReason());
		assertTrue(webHook.isEnabled());
	}
	
	@Test
	public void testCheckSingleFilterFailsFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^.+1234.+$", true));
		assertFalse(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("Filter mismatch: ${buildNumber} (123) does not match using regex ^.+1234.+$", webHook.getDisabledReason());
		assertFalse(webHook.isEnabled());
	}
	
	@Test
	public void testCheckTwoFiltersFailWithGoodAndBadFilterFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${projectName}", "^Test\\s+Project$", true));
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^.+1234.+$", true));
		assertFalse(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("Filter mismatch: ${buildNumber} (123) does not match using regex ^.+1234.+$", webHook.getDisabledReason());
		assertFalse(webHook.isEnabled());
	}
	
	@Test
	public void testCheckTwoFiltersFailWithTwoBadFiltersFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${projectName}", "^Incorrect Project Name$", true));
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^.+1234.+$", true));
		assertFalse(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("Filter mismatch: ${projectName} (Test Project) does not match using regex ^Incorrect Project Name$", webHook.getDisabledReason());
		assertFalse(webHook.isEnabled());
	}
	
	@Test
	public void testCheckTwoFiltersPassWithTwoGoodFiltersFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${projectName}", "^[Tt]est [Pp]roject$", true));
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^\\d+$", true));
		System.out.println(webHook.getDisabledReason());
		assertTrue(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("", webHook.getDisabledReason());
		assertTrue(webHook.isEnabled());
	}
	
	@Test
	public void testCheckTwoFiltersPassWithOneBadDisabledAndOneGoodEnabledFilterFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(), content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("${projectName}", "^[Tt]est [Pp]roject$", true));
		webHook.addFilter(WebHookFilterConfig.create("${buildNumber}", "^1234567890$", false));
		System.out.println(webHook.getDisabledReason());
		assertTrue(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("", webHook.getDisabledReason());
		assertTrue(webHook.isEnabled());
	}
	
	@Test
	public void testCheckFilterPassWithoutBeginAndStartCharsFilterFromBean() {
		
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(content.getProject(), new SimpleSerialiser(),content, allProperties, webHookSecretResolver); 
		WebHook webHook = factory.getWebHook();
		webHook.setEnabled(true);
		webHook.addFilter(WebHookFilterConfig.create("Some big long string", ".+ong.+", true));
		System.out.println(webHook.getDisabledReason());
		assertTrue(webHook.checkFilters(variableResolverFactory.createVariableMessageBuilder(resolver)));
		assertEquals("", webHook.getDisabledReason());
		assertTrue(webHook.isEnabled());
	}

}
