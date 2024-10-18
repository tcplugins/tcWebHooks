package webhook.teamcity.test.springmock;

import jetbrains.buildServer.serverSide.ProjectsModelListener;
import jetbrains.buildServer.util.EventDispatcher;

public class MockEventDispatcher extends EventDispatcher<ProjectsModelListener> {

	protected MockEventDispatcher(Class<ProjectsModelListener> listenerClass) {
		super(listenerClass);
	}


}
