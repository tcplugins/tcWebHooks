package webhook.teamcity.testing;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.AgentRestrictor;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.BuildEstimates;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.CompatibilityResult;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.util.filters.Filter;

/**
 * A class used just to represent an {@link SQueuedBuild} when testing
 * a webhook or webhook template.
 * It takes an {@link SBuild} and builds a mocked up {@link SQueuedBuild} instance from it.
 */
public class TestingSQueuedBuild implements SQueuedBuild {

	private SBuild build;
	private SBuildType buildType;
	private TriggeredBy triggeredBy = new TestingTriggeredBy();

	public TestingSQueuedBuild(SBuild sBuild) {
		this.build = sBuild;
		this.buildType = sBuild.getBuildType();
	}

	@Override
	public Date getWhenQueued() {
		return new Date();
	}

	@Override
	public String getItemId() {
		return "testingId";
	}

	@Override
	public String getBuildTypeId() {
		return this.buildType.getExternalId();
	}

	@Override
	public Integer getBuildAgentId() {
		return null;
	}

	@Override
	public AgentRestrictor getAgentRestrictor() {
		return null;
	}

	@Override
	public boolean isPersonal() {
		return false;
	}

	@Override
	public int getOrderNumber() {
		return 0;
	}

	@Override
	public BuildPromotion getBuildPromotion() {
		return this.build.getBuildPromotion();
	}

	@Override
	public SBuild getSequenceBuild() {
		return null;
	}

	@Override
	public Collection<SBuildAgent> getCompatibleAgents() {
		return Collections.emptyList();
	}

	@Override
	public CompatibilityResult getAgentCompatibility(AgentDescription agentDescription) {
		return null;
	}

	@Override
	public SBuildType getBuildType() {
		return this.buildType;
	}

	@Override
	public List<SBuildAgent> getCanRunOnAgents() {
		return Collections.emptyList();
	}

	@Override
	public Map<SBuildAgent, CompatibilityResult> getCompatibilityMap() {
		return null;
	}

	@Override
	public SBuildAgent getBuildAgent() {
		return null;
	}

	@Override
	public BuildEstimates getBuildEstimates() {
		return null;
	}

	@Override
	public void removeFromQueue(User user, String comment) {
		// We are a mock, so we don't handle this case.
	}

	@Override
	public String getRequestor() {
		return null;
	}

	@Override
	public TriggeredBy getTriggeredBy() {
		return this.triggeredBy;
	}

	public class TestingTriggeredBy implements TriggeredBy {

		@Override
		public String getAsString() {
			return "you";
		}

		@Override
		public String getAsString(Filter<String> paramNameFilter) {
			return null;
		}

		@Override
		public String getRawTriggeredBy() {
			return null;
		}

		@Override
		public SUser getUser() {
			return null;
		}

		@Override
		public boolean isTriggeredByUser() {
			return false;
		}

		@Override
		public Date getTriggeredDate() {
			return null;
		}

		@Override
		public Map<String, String> getParameters() {
			return null;
		}

	}

}
