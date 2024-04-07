package webhook.teamcity.test.springmock;

import org.jdom.Element;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.settings.WebHookProjectSettings;

public class MockProjectSettingsManager implements ProjectSettingsManager {
	
	WebHookProjectSettings webHookProjectSettings = new WebHookProjectSettings();

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
		return webHookProjectSettings;
	}

	@Override
	public void writeTo(Element target, String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFrom(Element rootElement, String projectId) {
		webHookProjectSettings.readFrom(rootElement);
	}

	@Override
	public void clearProjectSettings(String projectInternalId) {
		// TODO Auto-generated method stub

	}

}
