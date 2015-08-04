package webhook.teamcity.extension;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static webhook.teamcity.extension.WebHookAjaxEditPageController.BUILD_FAILED;
import static webhook.teamcity.extension.WebHookAjaxEditPageController.BUILD_SUCCESSFUL;
import static webhook.teamcity.extension.WebHookAjaxEditPageController.checkAndAddBuildState;
import static webhook.teamcity.extension.WebHookAjaxEditPageController.checkAndAddBuildStateIfEitherSet;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.SProject;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSProject;
import webhook.teamcity.payload.WebHookTemplateResolver;

public class WebHookAjaxEditPageControllerTest {
	
	final static String paramFormat = "payloadFormat";
	final static String paramTemplate = "payloadTemplate";
	
	HttpServletRequest requestSuccessOnAndFailureOn;
	HttpServletRequest requestSuccessOnAndFailureOff;
	HttpServletRequest requestSuccessOffAndFailureOn;
	HttpServletRequest requestSuccessOffAndFailureOff;
	BuildState states;
	WebHookTemplateResolver templateResolver;
	SProject project;
	
	@Before
	public void setup(){
		states = new BuildState();

		requestSuccessOnAndFailureOff = mock(HttpServletRequest.class);
		when(requestSuccessOnAndFailureOff.getParameter(BUILD_SUCCESSFUL)).thenReturn("on");
		when(requestSuccessOnAndFailureOff.getParameter(paramFormat)).thenReturn(paramFormat);
		when(requestSuccessOnAndFailureOff.getParameter(paramTemplate)).thenReturn(paramTemplate);
		
		requestSuccessOffAndFailureOn = mock(HttpServletRequest.class);
		when(requestSuccessOffAndFailureOn.getParameter(BUILD_FAILED)).thenReturn("on");
		when(requestSuccessOffAndFailureOn.getParameter(paramFormat)).thenReturn(paramFormat);
		when(requestSuccessOffAndFailureOn.getParameter(paramTemplate)).thenReturn(paramTemplate);
		
		requestSuccessOnAndFailureOn = mock(HttpServletRequest.class);
		when(requestSuccessOnAndFailureOn.getParameter(BUILD_SUCCESSFUL)).thenReturn("on");
		when(requestSuccessOnAndFailureOn.getParameter(BUILD_FAILED)).thenReturn("on");
		when(requestSuccessOnAndFailureOn.getParameter(paramFormat)).thenReturn(paramFormat);
		when(requestSuccessOnAndFailureOn.getParameter(paramTemplate)).thenReturn(paramTemplate);
		
		requestSuccessOffAndFailureOff = mock(HttpServletRequest.class);
		
		templateResolver = mock(WebHookTemplateResolver.class);
		project = mock(SProject.class);
		
		// We tell Mockito that any BuildStateEnum is OK for "when-ing".
		// That way it doesn't matter what state is passed in, our mocked object will return true. 
		for (BuildStateEnum state : BuildStateEnum.values()){
			when(templateResolver.templateSupportsFormatAndState(state, project, paramFormat, paramTemplate)).thenReturn(true);
		}
		
	}
	
	@Test
	
	/** 
	 * The problem with the logic is that enabling the BUILD_SUCCESSFUL and BUILD_FAILED settings 
	 * also enable BUILD_FINISHED triggering. (See the logic in WebHookAjexEditPageController#doHandle)
	 * 
	 * However, the last one wins, so we should really do an OR on it. (See next four tests)
	 */
	public void testCheckAndAddBuildState() {
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
		assertTrue(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FINISHED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FINISHED));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_FAILED);
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FINISHED));
	}
	
	

	@Test
	public void testCheckAndAddBuildStateIfEitherSet01() {
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
		assertTrue(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FINISHED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FINISHED));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_FAILED);
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));

		/* Use checkAndAddBuildStateIfEitherSet so that either one or the other need to be set, not the last one */
		
		checkAndAddBuildStateIfEitherSet(requestSuccessOnAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL, BUILD_FAILED);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FINISHED));
	}
	
	@Test
	public void testCheckAndAddBuildStateIfEitherSet02() {
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		checkAndAddBuildState(templateResolver, project, requestSuccessOffAndFailureOn, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOffAndFailureOn, states, BuildStateEnum.BUILD_FAILED, BUILD_FAILED);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FAILED));
		
		/* Use checkAndAddBuildStateIfEitherSet so that either one or the other need to be set, not the last one */
		
		checkAndAddBuildStateIfEitherSet(requestSuccessOffAndFailureOn, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL, BUILD_FAILED);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FINISHED));
	}

	@Test
	public void testCheckAndAddBuildStateIfEitherSet03() {
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		checkAndAddBuildState(templateResolver, project, requestSuccessOffAndFailureOff, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOffAndFailureOff, states, BuildStateEnum.BUILD_FAILED, BUILD_FAILED);
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		
		/* Use checkAndAddBuildStateIfEitherSet so that either one or the other need to be set, not the last one */
		
		checkAndAddBuildStateIfEitherSet(requestSuccessOffAndFailureOff, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL, BUILD_FAILED);
		assertFalse(states.enabled(BuildStateEnum.BUILD_FINISHED));
	}
	
	@Test
	public void testCheckAndAddBuildStateIfEitherSet04() {
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOn, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
		assertTrue(states.enabled(BuildStateEnum.BUILD_SUCCESSFUL));
		
		assertFalse(states.enabled(BuildStateEnum.BUILD_FAILED));
		checkAndAddBuildState(templateResolver, project, requestSuccessOnAndFailureOn, states, BuildStateEnum.BUILD_FAILED, BUILD_FAILED);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FAILED));
		
		/* Use checkAndAddBuildStateIfEitherSet so that either one or the other need to be set, not the last one */
		
		checkAndAddBuildStateIfEitherSet(requestSuccessOnAndFailureOn, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL, BUILD_FAILED);
		assertTrue(states.enabled(BuildStateEnum.BUILD_FINISHED));
	}
	
}
