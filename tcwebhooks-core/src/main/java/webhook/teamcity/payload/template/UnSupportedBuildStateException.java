package webhook.teamcity.payload.template;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentResolutionException;

@Getter
public class UnSupportedBuildStateException extends WebHookContentResolutionException {

	private static final int ERROR_CODE = 902;

	private static final long serialVersionUID = -9034464700540385492L;
	
	private final BuildStateEnum buildState;
	private final String nonBranchOrBranch;
	private final String templateId;
	private final String templateDescription;
	private final String projectId;
	private final Set<BuildStateEnum> supportedBuildStates;

	public UnSupportedBuildStateException(BuildStateEnum buildState, String nonBranchOrBranch, String templateId, String templateDescription,
			Set<BuildStateEnum> supportedBranchBuildStates) {
		super("Template '" + templateId + "' does not support build state '" + buildState.getShortName() + "'", ERROR_CODE);
		this.nonBranchOrBranch = nonBranchOrBranch;
		this.buildState = buildState;
		this.templateId = templateId;
		this.templateDescription = templateDescription;
		this.projectId = "";
		this.supportedBuildStates = supportedBranchBuildStates;
	}
	
	public UnSupportedBuildStateException(BuildStateEnum buildState, String nonBranchOrBranch, String projectId, String templateId ) {
		super("Template '" + templateId + "' does not support build state '" + buildState.getShortName() + "'", ERROR_CODE);
		this.nonBranchOrBranch = nonBranchOrBranch;
		this.buildState = buildState;
		this.templateId = templateId;
		this.projectId = projectId;
		this.templateDescription = "";
		this.supportedBuildStates = new HashSet<>();
	}

}
