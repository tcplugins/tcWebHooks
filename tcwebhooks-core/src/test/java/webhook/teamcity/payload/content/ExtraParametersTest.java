package webhook.teamcity.payload.content;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import jetbrains.buildServer.parameters.ProcessingResult;
import jetbrains.buildServer.parameters.ValueResolver;
import webhook.teamcity.settings.project.WebHookParameterModel;

public class ExtraParametersTest {

	@Test
	public void testThatForcedResolutionOnlyUpdatesTheCorrectItem() {
		ExtraParameters extraParameters = new ExtraParameters();
		extraParameters.add(new WebHookParameterModel(
				"test_01", // ID
				ExtraParameters.PROJECT, // Context
				"param01", // Name
				"test.param.01", // Value
				false, // Not secure
				ExtraParameters.INCLUDED_IN_LEGACY_PAYLOADS, 
				true,  // Force resolve
				ExtraParameters.TEMPLATE_ENGINE_TYPE));
		
		extraParameters.add(new WebHookParameterModel(
				"test_02", // ID
				ExtraParameters.WEBHOOK, // Context
				"param01", // Name
				"test.param.01", // Value
				false, // Not secure
				ExtraParameters.INCLUDED_IN_LEGACY_PAYLOADS, 
				ExtraParameters.FORCE_RESOLVE_TEAMCITY_VARIABLE,  // Don't Force resolve
				ExtraParameters.TEMPLATE_ENGINE_TYPE));
		
		
		ValueResolver valueResolver = Mockito.mock(ValueResolver.class);
		Mockito.when(valueResolver.resolveWithDetails(ArgumentMatchers.any())).thenReturn(Collections.singletonMap("param01", new ProcessingResult() {
			
			@Override
			public boolean isModified() {
				return true;
			}
			
			@Override
			public boolean isFullyResolved() {
				return true;
			}
			
			@Override
			public String getResult() {
				return "fooBar";
			}
		}));
		extraParameters.forceResolveVariables(valueResolver );
		assertEquals("fooBar", extraParameters.getProjectParameters().get(0).getValue());
		assertEquals("test.param.01", extraParameters.getWebHookParameters().get(0).getValue());
	}

}
