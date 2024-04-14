package webhook.teamcity;

public interface WebHookSettingsEventHandler {
    public void handleEvent(WebHookSettingsEventType eventType, String projectInternalId);
}
