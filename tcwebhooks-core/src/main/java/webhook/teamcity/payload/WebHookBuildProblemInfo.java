package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WebHookBuildProblemInfo implements BuildProblemInfo {

	int id;
	String projectId;
	String buildProblemDescription;

	public static Collection<WebHookBuildProblemInfo> buildCollection(Collection<BuildProblemInfo> buildProblems) {
		if (buildProblems == null) {
			return Collections.emptyList();
		}
		Collection<WebHookBuildProblemInfo> newBuildProblems = new ArrayList<>();
		buildProblems.forEach((BuildProblemInfo info) ->
			newBuildProblems.add(
					WebHookBuildProblemInfo
							.builder()
							.id(info.getId())
							.projectId(info.getProjectId())
							.buildProblemDescription(info.getBuildProblemDescription())
							.build())

		);
		return newBuildProblems;
	}
}
