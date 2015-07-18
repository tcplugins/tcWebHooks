package webhook.teamcity.payload.content;

import jetbrains.buildServer.serverSide.Branch;

public class WebHooksBranchImpl implements Branch {
	
	String displayName;
	String name;
	boolean isDefaultBranch;
	
	public WebHooksBranchImpl(Branch branch) {
		this.displayName = branch.getDisplayName();
		this.name = branch.getName();
		this.isDefaultBranch = branch.isDefaultBranch();
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isDefaultBranch() {
		return this.isDefaultBranch;
	}

}
