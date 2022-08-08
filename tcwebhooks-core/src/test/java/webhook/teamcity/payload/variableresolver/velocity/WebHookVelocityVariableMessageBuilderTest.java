package webhook.teamcity.payload.variableresolver.velocity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.velocity.context.Context;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.settings.project.WebHookParameterModel;
import webhook.teamcity.settings.secure.WebHookSecretResolver;

public class WebHookVelocityVariableMessageBuilderTest {

	@Test
	public void testBuild() {
		SProject sProject = Mockito.mock(SProject.class);
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.put("myString", "${buildId} is in project ${projectId}");
		Context resolver = new WebHooksBeanUtilsVelocityVariableResolver(
				null, 
				new WebHookPayloadContent.SimpleSerialiser(),
				new JavaBean("bt01", "project01", sProject),
				extraParameters,
				null
			);
		WebHookVelocityVariableMessageBuilder builder = WebHookVelocityVariableMessageBuilder.create(resolver, null);
		
		assertEquals("bt01", builder.build("${buildId}"));
		assertEquals("project01", builder.build("${projectId}"));
		assertEquals("bt01 is in project project01", builder.build("${myString}"));
		
	}
	
	@Test
	public void testBuildSecure() {
		
		WebHookSecretResolver webHookSecretResolver = Mockito.mock(WebHookSecretResolver.class);
		SProject sProject = Mockito.mock(SProject.class);
		Mockito.when(webHookSecretResolver.getSecret(ArgumentMatchers.any(), ArgumentMatchers.eq("abc123"))).thenReturn("myPass");
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.put("myString", "${buildId} is in project ${projectId}");
		extraParameters.put("myKey", "abc123");
		Context resolver = new WebHooksBeanUtilsVelocityVariableResolver(
				sProject, 
				new WebHookPayloadContent.SimpleSerialiser(),
				new JavaBean("bt01", "project01", sProject),
				extraParameters,
				null
				);
		WebHookVelocityVariableMessageBuilder builder = WebHookVelocityVariableMessageBuilder.create(resolver, webHookSecretResolver);
		
		assertEquals("myPass", builder.build("#secure($myKey)"));
		
	}
	
	@Test
	public void testAccessingSecureParameter() {
		
		SProject sProject = Mockito.mock(SProject.class);
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.put("myString", "${buildId} is in project ${projectId}");
		extraParameters.put("myKey", "abc123");
		extraParameters.add(new WebHookParameterModel("1", "project", "my.Secret.Url", "http://some.secret.com/place", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, PayloadTemplateEngineType.VELOCITY.toString()));
		Context resolver = new WebHooksBeanUtilsVelocityVariableResolver(
				sProject, 
				new WebHookPayloadContent.SimpleSerialiser(),
				new JavaBean("bt01", "project01", sProject),
				extraParameters,
				null
				);
		WebHookVelocityVariableMessageBuilder builder = WebHookVelocityVariableMessageBuilder.create(resolver, null);
		assertFalse(extraParameters.wasSecureValueAccessed());
		assertEquals("bt01 is in project project01-http://some.secret.com/place", builder.build("${myString}-${my_Secret_Url}"));
		assertTrue(extraParameters.wasSecureValueAccessed());
	}

	@Data @AllArgsConstructor
	public class JavaBean {
		
		private String buildId;
		private String projectId;
		private SProject project;
	}
}
