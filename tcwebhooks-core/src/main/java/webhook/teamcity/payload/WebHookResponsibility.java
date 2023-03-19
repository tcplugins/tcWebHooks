package webhook.teamcity.payload;

import java.util.Collection;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.tests.TestName;
import lombok.Builder;
import lombok.Getter;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.content.WebHookResponsibilityEntry;

/**
 * A serialisable version of WebHookResponsibilityHolder
 * so that it can be added to the payload.
 *
 */
@Builder @Getter
public class WebHookResponsibility {
	
	private ResponsibilityEntry.State state;
	private WebHookResponsibilityEntry responsibilityEntryOld;
	private WebHookResponsibilityEntry responsibilityEntryNew;
	private Boolean isUserAction;
	private String projectId;
	private String buildTypeId;
	private TestNameResponsibilityEntry testNameResponsibilityEntry;
	private Collection<TestName> testNames;
	private Collection<WebHookBuildProblemInfo> buildProblems;
	
	public static WebHookResponsibility build(WebHookResponsibilityHolder wrh) {
		return
				builder()
				.state(wrh.getState())
				.responsibilityEntryOld(WebHookResponsibilityEntry.build(wrh.getResponsibilityEntryOld()))
				.responsibilityEntryNew(WebHookResponsibilityEntry.build(wrh.getResponsibilityEntryNew()))
				.isUserAction(wrh.getIsUserAction())
				.projectId(wrh.getSProject() != null ? wrh.getSProject().getExternalId() : null)
				.buildTypeId(wrh.getSBuildType() != null ? wrh.getSBuildType().getExternalId() : null)
				.testNameResponsibilityEntry(wrh.getTestNameResponsibilityEntry())
				.testNames(wrh.getTestNames())
				.buildProblems(WebHookBuildProblemInfo.buildCollection(wrh.getBuildProblems()))
				.build();
	}

}
