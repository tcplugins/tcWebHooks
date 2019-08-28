package webhook.teamcity.testing;

import java.util.Date;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.users.User;

public class TestingResponsibilityEntry implements ResponsibilityEntry {
	
	private State state;
	private User testingUser;

	public TestingResponsibilityEntry(String mockUserName, State state) {
		this.state = state;
		this.testingUser = new TestingMockUser(mockUserName);
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public User getResponsibleUser() {
		return this.testingUser;
	}

	@Override
	public User getReporterUser() {
		return this.testingUser;
	}

	@Override
	public Date getTimestamp() {
		return new Date();
	}

	@Override
	public String getComment() {
		return "Testing comment";
	}

	@Override
	public RemoveMethod getRemoveMethod() {
		return RemoveMethod.WHEN_FIXED;
	}

}
