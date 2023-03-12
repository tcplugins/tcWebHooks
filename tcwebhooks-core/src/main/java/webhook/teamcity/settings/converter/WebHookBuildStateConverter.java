package webhook.teamcity.settings.converter;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;

public class WebHookBuildStateConverter {

	private WebHookBuildStateConverter(){}

	public static BuildState convert(Integer oldState){
		BuildState newStates = new BuildState();

		// Set changes loaded based on started.
		newStates.setEnabled(BuildStateEnum.CHANGES_LOADED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_ADDED_TO_QUEUE, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));

		newStates.setEnabled(BuildStateEnum.BUILD_STARTED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));
		newStates.setEnabled(BuildStateEnum.BEFORE_BUILD_FINISHED, OldStyleBuildState.enabled(OldStyleBuildState.BEFORE_BUILD_FINISHED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_INTERRUPTED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_INTERRUPTED, oldState));
		newStates.setEnabled(BuildStateEnum.RESPONSIBILITY_CHANGED, OldStyleBuildState.enabled(OldStyleBuildState.RESPONSIBILITY_CHANGED, oldState));

		// We don't support CHANGED_STATUS any more. It was too confusing.

		// BUILD_FINISHED has now been made more specific, so enable them all to start with, and users can turn them off.
		newStates.setEnabled(BuildStateEnum.BUILD_FINISHED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_SUCCESSFUL, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_FAILED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));

		newStates.setEnabled(BuildStateEnum.BUILD_PINNED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));
		newStates.setEnabled(BuildStateEnum.BUILD_UNPINNED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));
		newStates.setEnabled(BuildStateEnum.TESTS_MUTED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));
		newStates.setEnabled(BuildStateEnum.TESTS_UNMUTED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_STARTED, oldState));
		newStates.setEnabled(BuildStateEnum.SERVICE_MESSAGE_RECEIVED, OldStyleBuildState.enabled(OldStyleBuildState.BUILD_FINISHED, oldState));
		return newStates;
	}
}
