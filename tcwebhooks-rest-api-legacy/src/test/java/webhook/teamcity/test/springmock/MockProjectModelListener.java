package webhook.teamcity.test.springmock;

import jetbrains.buildServer.serverSide.ProjectsModelListener;
import jetbrains.buildServer.serverSide.ProjectsModelListenerAdapter;

public class MockProjectModelListener extends ProjectsModelListenerAdapter implements ProjectsModelListener {
	public MockProjectModelListener() {
		super();
	}

}
