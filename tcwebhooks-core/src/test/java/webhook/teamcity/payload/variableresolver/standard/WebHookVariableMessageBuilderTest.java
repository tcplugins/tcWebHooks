package webhook.teamcity.payload.variableresolver.standard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHookVariableMessageBuilderTest {

	@Test
	public void testBuild() {
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.put("myString", "${buildId} is in project ${projectId}");
		VariableResolver resolver = new WebHooksBeanUtilsVariableResolver(
				null, 
				new WebHookPayloadContent.SimpleSerialiser(),
				new JavaBean("bt01", "project01"),
				extraParameters,
				null
			);
		WebHookVariableMessageBuilder builder = WebHookVariableMessageBuilder.create(resolver);
		
		assertEquals("bt01", builder.build("${buildId}"));
		assertEquals("project01", builder.build("${projectId}"));
		assertEquals("bt01 is in project project01", builder.build("${myString}"));
		
	}

	@Data @AllArgsConstructor
	public class JavaBean {
		
		private String buildId;
		private String projectId;
	}
}
