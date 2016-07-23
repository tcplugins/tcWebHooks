package webhook.teamcity.test.springmock;

import org.jdom.Element;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

public class MockProjectSettingsManager implements ProjectSettingsManager {

	@Override
	public void registerSettingsFactory(String serviceName,
			ProjectSettingsFactory factory) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public ProjectSettingsFactory unregisterSettingsFactory(String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectSettings getSettings(String projectId, String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeTo(Element target, String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFrom(Element rootElement, String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearProjectSettings(String projectInternalId) {
		// TODO Auto-generated method stub

	}

}
