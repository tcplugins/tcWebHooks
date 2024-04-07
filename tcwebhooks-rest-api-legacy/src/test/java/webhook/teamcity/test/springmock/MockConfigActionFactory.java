package webhook.teamcity.test.springmock;

import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.users.SUser;

public class MockConfigActionFactory implements ConfigActionFactory {

	@Override
	public ConfigAction createAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigAction createAction(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigAction createAction(SProject arg0, String arg1) {
		// TODO Auto-generated method stub
		return Mockito.mock(ConfigAction.class);
	}

	@Override
	public ConfigAction createAction(SUser arg0, SProject arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
