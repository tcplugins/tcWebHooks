package webhook.teamcity.payload.format;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.User;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.velocity.WebHooksBeanUtilsVelocityVariableResolverFactory;

public class WebHookPayloadJsonVelocityTemplateTest {

	WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
	WebHookPayloadJsonVelocityTemplate whp;
	WebHookPayloadManager whpm;
	ResponsibilityEntry responsibilityEntryOld;
	ResponsibilityEntry responsibilityEntryNew;

	@Before
	public void setup() {

		User user = mock(User.class);
		when(user.getDescriptiveName()).thenReturn("Fred", "Bob");
		responsibilityEntryOld = mock(ResponsibilityEntry.class);
		responsibilityEntryNew = mock(ResponsibilityEntry.class);
		when(responsibilityEntryOld.getResponsibleUser()).thenReturn(user);
		when(responsibilityEntryNew.getResponsibleUser()).thenReturn(user);
		when(responsibilityEntryOld.getComment()).thenReturn("Comment Old");
		when(responsibilityEntryNew.getComment()).thenReturn("Comment New");

		SBuildServer buildServer = mock(SBuildServer.class);
		when(buildServer.getRootUrl()).thenReturn("http://test.url");
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVelocityVariableResolverFactory());
		whp = new WebHookPayloadJsonVelocityTemplate(new WebHookPayloadManager(buildServer), variableResolverManager);
	}

	@Test
	public void testRegister() {

		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadJsonVelocityTemplate whp = new WebHookPayloadJsonVelocityTemplate(wpm, variableResolverManager);
		whp.register();
		assertEquals(whp, wpm.getFormat(whp.getFormatShortName()));
	}

	@Test
	public void testGetContentType() {
		assertEquals(whp.getContentType().toString(), "application/json");
	}

	@Test
	public void testGetRank() {
		assertEquals(Integer.valueOf(101),whp.getRank());
	}

	@Test
	public void testSetRank() {
		whp.setRank(10);
		assertEquals(Integer.valueOf(10), whp.getRank());
	}

	@Test
	public void testGetCharset() {
		assertEquals("UTF-8", whp.getCharset());
	}

	@Test
	public void testGetFormatDescription() {
		assertEquals("JSON Velocity template", whp.getFormatDescription());
	}

	@Test
	public void testGetFormatShortName() {
		assertEquals("jsonVelocityTemplate", whp.getFormatShortName());
	}

	@Test
	public void testGetTemplateEngineType() {
		assertEquals(PayloadTemplateEngineType.VELOCITY, whp.getTemplateEngineType());
	}

	@Test
	public void testGetFormatToolTipText() {
		assertEquals("Send a JSON payload with content from a Velocity template", whp.getFormatToolTipText());
	}

	@Test
	public void testResponsibleChanged() {
		ParametersProvider pp = mock(ParametersProvider.class);
		when(pp.getAll()).thenReturn(new HashMap<String,String>());
		MockSBuildType buildType = new MockSBuildType("mockBuildType", "a description", "bt01");
		buildType.setParametersProvider(pp);
		MockSProject project = new MockSProject("name", "description", "projectId", "projectExternalId", buildType);
		buildType.setProject(project);

		WebHookTemplateContent templateContent = WebHookTemplateContent.create(
				"responsibilityChanged",
				"{ \"testing\": \"${buildTypeId}\" }",
				true,
				""
			);
		assertEquals("{ \"testing\": \"mockBuildType\" }", whp.responsibilityChanged(
				WebHookResponsibilityHolder
					.builder()
					.responsibilityEntryOld(responsibilityEntryOld)
					.responsibilityEntryNew(responsibilityEntryNew)
					.sBuildType(buildType)
					.sProject(project)
					.build(),
				new ExtraParameters(),
				new TreeMap<String,String>(),
				templateContent));

	}
}
