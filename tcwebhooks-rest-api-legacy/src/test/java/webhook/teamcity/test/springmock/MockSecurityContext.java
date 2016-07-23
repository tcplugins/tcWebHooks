package webhook.teamcity.test.springmock;

import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.SecurityContext;

public class MockSecurityContext implements SecurityContext {

	@Override
	public AuthorityHolder getAuthorityHolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
