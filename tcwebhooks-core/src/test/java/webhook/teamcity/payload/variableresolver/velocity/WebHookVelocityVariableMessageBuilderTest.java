package webhook.teamcity.payload.variableresolver.velocity;

import static org.junit.Assert.assertEquals;

import org.apache.velocity.context.Context;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
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

	@Data @AllArgsConstructor
	public class JavaBean {
		
		private String buildId;
		private String projectId;
		private SProject project;
	}
}
