package webhook.teamcity.testing;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.AgentRestrictor;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.BuildEstimates;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.BuildTypeNotFoundException;
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
	
	private SBuildType buildType;
	private TriggeredBy triggeredBy = new TestingTriggeredBy();

	public TestingSQueuedBuild(SBuild sBuild) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentRestrictor getAgentRestrictor() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuild getSequenceBuild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SBuildAgent> getCompatibleAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompatibilityResult getAgentCompatibility(AgentDescription agentDescription) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuildType getBuildType() throws BuildTypeNotFoundException {
		return this.buildType;
	}

	@Override
	public List<SBuildAgent> getCanRunOnAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<SBuildAgent, CompatibilityResult> getCompatibilityMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuildAgent getBuildAgent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BuildEstimates getBuildEstimates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeFromQueue(User user, String comment) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRequestor() {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRawTriggeredBy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SUser getUser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isTriggeredByUser() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Date getTriggeredDate() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, String> getParameters() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
