package webhook.teamcity.test.springmock;

import webhook.teamcity.WebHookSettingsEventHandler;
import webhook.teamcity.WebHookSettingsEventType;

public class MockingWebHookSettingsEventHandler implements WebHookSettingsEventHandler {

	@Override
	public void handleEvent(WebHookSettingsEventType eventType, String projectInternalId) {
		// TODO Auto-generated method stub
	}

}
