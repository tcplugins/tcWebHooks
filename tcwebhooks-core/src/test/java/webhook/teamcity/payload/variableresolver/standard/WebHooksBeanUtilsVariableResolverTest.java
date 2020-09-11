package webhook.teamcity.payload.variableresolver.standard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHooksBeanUtilsVariableResolverTest {

	@Test
	public void testResolve() {
		
		WebHookContentObjectSerialiser webHookContentObjectSerialiser = new WebHookPayloadContent.SimpleSerialiser();
		JavaBean javaBean = new JavaBean("bt123", "project01");
		
		ExtraParameters extraParameters = new ExtraParameters();
		VariableResolver variableResolver = new WebHooksBeanUtilsVariableResolver(null, webHookContentObjectSerialiser, javaBean, extraParameters, null);
		assertEquals("bt123", variableResolver.resolve("buildId"));
		assertEquals("project01", variableResolver.resolve("projectId"));
	}

	@Data @AllArgsConstructor
	public class JavaBean {
		
		private String buildId;
		private String projectId;
	}
}
